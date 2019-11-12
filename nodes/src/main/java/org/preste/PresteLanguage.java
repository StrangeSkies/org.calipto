package org.preste;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.preste.node.PresteReplNode;
import org.preste.reader.PresteDataFactory;
import org.preste.reader.PresteReader;
import org.preste.source.PresteFileDetector;
import org.preste.source.SourceScanner;
import org.preste.type.cons.ConsLibrary;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.RootCallTarget;
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
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
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

  public PresteLanguage() {
    counter++;
  }

  @Override
  protected PresteContext createContext(Env env) {
    return new PresteContext(this, env, new ArrayList<>(EXTERNAL_BUILTINS));
  }

  @Override
  protected CallTarget parse(ParsingRequest request) throws Exception {
    Source source = request.getSource();
    PresteReader reader = new PresteReader(getFactory(), new SourceScanner(source));

    RootCallTarget main = functions.get("main");
    RootNode evalMain;
    if (main != null) {
      /*
       * We have a main function, so "evaluating" the parsed source means invoking
       * that main function. However, we need to lazily register functions into the
       * PresteContext first, so we cannot use the original SLRootNode for the main
       * function. Instead, we create a new SLEvalRootNode that does everything we
       * need.
       */
      evalMain = new PresteReplNode(this, main, functions);
    } else {
      /*
       * Even without a main function, "evaluating" the parsed source needs to
       * register the functions into the PresteContext.
       */
      evalMain = new PresteReplNode(this, null, functions);
    }

    return Truffle.getRuntime().createCallTarget(evalMain);
  }

  private PresteDataFactory getFactory() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * Still necessary for the old SL TCK to pass. We should remove with the old
   * TCK. New language should not override this.
   */
  @SuppressWarnings("deprecation")
  @Override
  protected Object findExportedSymbol(
      PresteContext context,
      String globalName,
      boolean onlyExplicit) {
    return context.getFunctionRegistry().lookup(globalName, false);
  }

  @Override
  protected boolean isVisible(PresteContext context, Object value) {
    return !InteropLibrary.getFactory().getUncached(value).isNull(value);
  }

  @Override
  protected boolean isObjectOfLanguage(Object object) {
    if (!(object instanceof TruffleObject)) {
      return false;
    } else if (object instanceof SLBigNumber || object instanceof SLFunction
        || object instanceof SLNull) {
      return true;
    } else if (ConsLibrary.getFactory().getUncached().isCons(object)) {
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
        return "NULL";
      } else if (interop.isExecutable(value)) {
        if (value instanceof SLFunction) {
          return ((SLFunction) value).getName();
        } else {
          return "Function";
        }
      } else if (interop.hasMembers(value)) {
        return "Object";
      } else if (value instanceof SLBigNumber) {
        return value.toString();
      } else {
        return "Unsupported";
      }
    } catch (UnsupportedMessageException e) {
      CompilerDirectives.transferToInterpreter();
      throw new AssertionError();
    }
  }

  @Override
  protected Object findMetaObject(PresteContext context, Object value) {
    return getMetaObject(value);
  }

  public static String getMetaObject(Object value) {
    if (value == null) {
      return "ANY";
    }
    InteropLibrary interop = InteropLibrary.getFactory().getUncached(value);
    if (interop.isNumber(value) || value instanceof SLBigNumber) {
      return "Number";
    } else if (interop.isBoolean(value)) {
      return "Boolean";
    } else if (interop.isString(value)) {
      return "String";
    } else if (interop.isNull(value)) {
      return "NULL";
    } else if (interop.isExecutable(value)) {
      return "Function";
    } else if (interop.hasMembers(value)) {
      return "Object";
    } else {
      return "Unsupported";
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
          private SLLexicalScope previousScope;
          private SLLexicalScope nextScope = scope;

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

  private static final List<NodeFactory<? extends PresteBuiltinNode>> EXTERNAL_BUILTINS = Collections
      .synchronizedList(new ArrayList<>());

  public static void installBuiltin(NodeFactory<? extends PresteBuiltinNode> builtin) {
    EXTERNAL_BUILTINS.add(builtin);
  }
}