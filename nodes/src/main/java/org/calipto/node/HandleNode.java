package org.calipto.node;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.nodes.ExplodeLoop;

/**
 * Yield control from the current continuation to an enclosing scope to perform
 * some side effect.
 */
@GenerateNodeFactory
public abstract class HandleNode extends CaliptoNode {
  static final ThreadLocal<Handlers> HANDLERS = new ThreadLocal<>() {
    @Override
    protected Handlers initialValue() {
      return new Handlers();
    }
  };

  @Children
  private final CaliptoNode[] argumentNodes;
  @Child
  private InteropLibrary library;

  public HandleNode(CaliptoNode[] argumentNodes) {
    this.argumentNodes = argumentNodes;
    this.library = InteropLibrary.getFactory().createDispatched(3);
  }

  @ExplodeLoop
  @Override
  public Object executeGeneric(VirtualFrame frame) {
    CompilerAsserts.compilationConstant(argumentNodes.length);

    var handlers = HANDLERS.get();

    var mediator = new EffectMediator();

    var effectHandlers = new Thread(() -> {
      HANDLERS.set(handlers.withHandlerMediator(mediator));

      // We might want an extra rule here that non-purely-functional side-effects are
      // not allowed. Thing is, we still want to be able to get variables and stuff,
      // which would be a side effect.

      // run our effect-handling code
    });
    effectHandlers.start();

    try {
      HANDLERS.set(handlers.withPerformerMediator(mediator));

      // run our effect-performing code

      return null; // result
    } finally {
      HANDLERS.set(handlers);

      /*
       * whatever, we're done with it.
       * 
       * TODO Do we also want to invalidate the side-effect mediator passed to the
       * handler thread? Surely it shouldn't continue to have side-effects at this
       * point...
       */
      effectHandlers.interrupt();
      try {
        effectHandlers.join();
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
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
