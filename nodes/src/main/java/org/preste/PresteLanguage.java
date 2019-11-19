package org.preste;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.preste.node.PresteReplNode;
import org.preste.node.builtin.BuiltinNode;
import org.preste.node.intrinsic.IntrinsicNode;
import org.preste.reader.PresteReader;
import org.preste.reader.ReadingContext;
import org.preste.source.PresteFileDetector;
import org.preste.source.SourceScanner;
import org.preste.type.DataLibrary;

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
    id = PresteLanguage.ID,
    name = "Preste",
    defaultMimeType = PresteFileDetector.MIME_TYPE,
    characterMimeTypes = PresteFileDetector.MIME_TYPE,
    contextPolicy = ContextPolicy.SHARED,
    fileTypeDetectors = PresteFileDetector.class)
@ProvidedTags({
    StandardTags.CallTag.class,
    StandardTags.StatementTag.class,
    StandardTags.RootTag.class,
    StandardTags.RootBodyTag.class,
    StandardTags.ExpressionTag.class,
    DebuggerTags.AlwaysHalt.class })
public class PresteLanguage extends TruffleLanguage<PresteContext> {
  public static volatile int counter;

  public static final String ID = "preste";

  private final Set<NodeFactory<? extends IntrinsicNode>> intrinsics;
  private final Set<NodeFactory<? extends BuiltinNode>> builtins;

  public PresteLanguage() {
    this(Set.of(), Set.of());
  }

  public PresteLanguage(
      Collection<? extends NodeFactory<? extends IntrinsicNode>> intrinsics,
      Collection<? extends NodeFactory<? extends BuiltinNode>> builtins) {
    this.intrinsics = Set.copyOf(intrinsics);
    this.builtins = Set.copyOf(builtins);
    counter++;
  }

  @Override
  protected PresteContext createContext(Env env) {
    return new PresteContext(this, env, intrinsics, builtins);
  }

  @Override
  protected CallTarget parse(ParsingRequest request) throws Exception {
    Source source = request.getSource();
    PresteReader reader = new PresteReader(getReadingContext(), new SourceScanner(source));

    var evalMain = new PresteReplNode(this, null, functions);

    return Truffle.getRuntime().createCallTarget(evalMain);
  }

  private ReadingContext getReadingContext() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected boolean isVisible(PresteContext context, Object value) {
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
  protected String toString(PresteContext context, Object value) {
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
        if (value instanceof PresteFunction) {
          return ((PresteFunction) value).getNamespace() + "/" + ((PresteFunction) value).getName();
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
  protected SourceSection findSourceLocation(PresteContext context, Object value) {
    if (value instanceof PresteFunction) {
      return ((PresteFunction) value).getDeclaredLocation();
    }
    return null;
  }

  @Override
  public Iterable<Scope> findLocalScopes(PresteContext context, Node node, Frame frame) {
    final PresteLexicalScope scope = PresteLexicalScope.createScope(node);
    return new Iterable<Scope>() {
      @Override
      public Iterator<Scope> iterator() {
        return new Iterator<Scope>() {
          private PresteLexicalScope previousScope;
          private PresteLexicalScope nextScope = scope;

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
  protected Iterable<Scope> findTopScopes(PresteContext context) {
    return context.getTopScopes();
  }

  public static PresteContext getCurrentContext() {
    return getCurrentContext(PresteLanguage.class);
  }
}