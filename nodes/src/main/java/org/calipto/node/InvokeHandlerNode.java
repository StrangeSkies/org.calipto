package org.calipto.node;

import static java.util.Objects.requireNonNull;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.ExplodeLoop;

/**
 * Yield control from the current continuation to an enclosing scope to perform
 * some side effect.
 */
@GenerateNodeFactory
public abstract class InvokeHandlerNode extends CaliptoNode {
  static final ThreadLocal<Handlers> HANDLERS = new ThreadLocal<>() {
    @Override
    protected Handlers initialValue() {
      return new Handlers();
    }
  };

  @Child
  private CaliptoNode handlerNode;
  @Children
  private final CaliptoNode[] argumentNodes;
  @Child
  private InteropLibrary library;

  public InvokeHandlerNode(CaliptoNode handlerNode, CaliptoNode[] argumentNodes) {
    this.handlerNode = requireNonNull(handlerNode);
    this.argumentNodes = requireNonNull(argumentNodes);
    this.library = InteropLibrary.getFactory().createDispatched(3);
  }

  @ExplodeLoop
  @Override
  public Object executeGeneric(VirtualFrame frame) {
    /*
     * TODO think about how to specialise param assignments
     */

    CompilerAsserts.compilationConstant(argumentNodes.length);

    /*
     * Resolve handler, register which effects we're looking for.
     */
    var handler = (Handler) handlerNode.executeGeneric(frame);

    var handlers = HANDLERS.get();
    var mediator = new EffectMediator();

    try {
      HANDLERS.set(handlers.withHandlerMediator(mediator));

      var argumentsContinuation = new Thread(() -> {
        for (var argumentNode : argumentNodes) {
          HANDLERS.set(handlers.withPerformerMediator(mediator));

          // run our effect-performing code

          var result = argumentNode.executeGeneric(frame);

          /*
           * collect arguments into array IFF one is handled directly by handler
           */
        }
      });
      // TODO assign result to frame slot if
      argumentsContinuation.start();

      // block to wait for handled effect

      // if arguments thread ends, check if we're handling arguments explicitly

      // if not collecting arguments, return last argument as result

      String effect = null;
      return library.execute(handler.getEffect(effect), arguments);
    } finally {
      HANDLERS.set(handlers);
    }
  }

  @Override
  public boolean hasTag(Class<? extends Tag> tag) {
    if (tag == StandardTags.CallTag.class) {
      return true;
    }
    return super.hasTag(tag);
  }

}
