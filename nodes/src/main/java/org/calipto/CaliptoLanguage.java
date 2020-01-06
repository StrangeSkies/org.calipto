package org.calipto;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.calipto.node.RootEvalNode;
import org.calipto.node.builtin.BuiltinNode;
import org.calipto.node.intrinsic.IntrinsicNode;
import org.calipto.reader.CaliptoReader;
import org.calipto.reader.ReadingContext;
import org.calipto.source.CaliptoFileDetector;
import org.calipto.source.SourceScanner;
import org.calipto.type.DataLibrary;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.Scope;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleLanguage.ContextPolicy;
import com.oracle.truffle.api.debug.DebuggerTags;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.instrumentation.ProvidedTags;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;

@TruffleLanguage.Registration(
    id = CaliptoLanguage.ID,
    name = "Calipto",
    defaultMimeType = CaliptoFileDetector.MIME_TYPE,
    characterMimeTypes = CaliptoFileDetector.MIME_TYPE,
    contextPolicy = ContextPolicy.SHARED,
    fileTypeDetectors = CaliptoFileDetector.class)
@ProvidedTags({
    StandardTags.CallTag.class,
    StandardTags.StatementTag.class,
    StandardTags.RootTag.class,
    StandardTags.RootBodyTag.class,
    StandardTags.ExpressionTag.class,
    DebuggerTags.AlwaysHalt.class })
public class CaliptoLanguage extends TruffleLanguage<CaliptoContext> {
  public static volatile int counter;

  public static final String ID = "calipto";

  private final Set<NodeFactory<? extends IntrinsicNode>> intrinsics;
  private final Set<NodeFactory<? extends BuiltinNode>> builtins;

  public CaliptoLanguage() {
    this(Set.of(), Set.of());
  }

  public CaliptoLanguage(
      Collection<? extends NodeFactory<? extends IntrinsicNode>> intrinsics,
      Collection<? extends NodeFactory<? extends BuiltinNode>> builtins) {
    this.intrinsics = Set.copyOf(intrinsics);
    this.builtins = Set.copyOf(builtins);
    counter++;
  }

  @Override
  protected CaliptoContext createContext(Env env) {
    return new CaliptoContext(this, env, intrinsics, builtins);
  }

  @Override
  protected CallTarget parse(ParsingRequest request) throws Exception {
    Source source = request.getSource();
    CaliptoReader reader = new CaliptoReader(getReadingContext(), new SourceScanner(source));

    var evalMain = new RootEvalNode(this, reader);

    return Truffle.getRuntime().createCallTarget(evalMain);
  }

  private ReadingContext getReadingContext() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected boolean isVisible(CaliptoContext context, Object value) {
    return !InteropLibrary.getFactory().getUncached(value).isNull(value);
  }

  @Override
  protected boolean isObjectOfLanguage(Object object) {
    if (DataLibrary.getFactory().getUncached().isData(object)) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  protected String toString(CaliptoContext context, Object value) {
    return toString(value);
  }

  public static String toString(Object value) {
    try {
      if (value == null) {
        return "ANY";
      }
      InteropLibrary interop = InteropLibrary.getFactory().getUncached(value);
      if (interop.fitsInLong(value)) {
        return Long.toString(interop.asLong(value));
      } else if (interop.isBoolean(value)) {
        return Boolean.toString(interop.asBoolean(value));
      } else if (interop.isString(value)) {
        return interop.asString(value);
      } else if (interop.isNull(value)) {
        return "null";
      } else if (interop.isExecutable(value)) {
        if (value instanceof CaliptoFunction) {
          return ((CaliptoFunction) value).getNamespace() + "/" + ((CaliptoFunction) value).getName();
        } else {
          return "Function";
        }
      } else if (interop.hasMembers(value)) {
        return "Object";
      } else {
        return "Unsupported";
      }
    } catch (UnsupportedMessageException e) {
      CompilerDirectives.transferToInterpreter();
      throw new AssertionError();
    }
  }

  @Override
  protected SourceSection findSourceLocation(CaliptoContext context, Object value) {
    if (value instanceof CaliptoFunction) {
      return ((CaliptoFunction) value).getDeclaredLocation();
    }
    return null;
  }

  @Override
  public Iterable<Scope> findLocalScopes(CaliptoContext context, Node node, Frame frame) {
    final CaliptoLexicalScope scope = CaliptoLexicalScope.createScope(node);
    return new Iterable<Scope>() {
      @Override
      public Iterator<Scope> iterator() {
        return new Iterator<Scope>() {
          private CaliptoLexicalScope previousScope;
          private CaliptoLexicalScope nextScope = scope;

          @Override
          public boolean hasNext() {
            if (nextScope == null) {
              nextScope = previousScope.findParent();
            }
            return nextScope != null;
          }

          @Override
          public Scope next() {
            if (!hasNext()) {
              throw new NoSuchElementException();
            }
            Scope vscope = Scope
                .newBuilder(nextScope.getName(), nextScope.getVariables(frame))
                .node(nextScope.getNode())
                .arguments(nextScope.getArguments(frame))
                .build();
            previousScope = nextScope;
            nextScope = null;
            return vscope;
          }
        };
      }
    };
  }

  @Override
  protected Iterable<Scope> findTopScopes(CaliptoContext context) {
    return context.getTopScopes();
  }

  public static CaliptoContext getCurrentContext() {
    return getCurrentContext(CaliptoLanguage.class);
  }
}