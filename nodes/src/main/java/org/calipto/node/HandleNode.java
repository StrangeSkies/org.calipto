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

  @Child
  private CaliptoNode handlerNode;
  @Children
  private final CaliptoNode[] performerNodes;
  @Child
  private InteropLibrary library;

  public HandleNode(CaliptoNode handlerNode, CaliptoNode[] performerNodes) {
    this.handlerNode = handlerNode;
    this.performerNodes = performerNodes;
    this.library = InteropLibrary.getFactory().createDispatched(3);

    /*
     * TODO create array of frame slots
     */
  }

  @ExplodeLoop
  @Override
  public Object executeGeneric(VirtualFrame frame) {
    CompilerAsserts.compilationConstant(performerNodes.length);

    var handlers = HANDLERS.get();

    var mediator = new EffectMediator();

    for (var performerNode : performerNodes) {
      var effectPerformers = new Thread(() -> {
        HANDLERS.set(handlers.withPerformerMediator(mediator));

        // run our effect-performing code

        var result = performerNode.executeGeneric(frame);

        // TODO assign result to frame slot
      });
      effectPerformers.start();
    }

    try {
      HANDLERS.set(handlers.withHandlerMediator(mediator));

      // run our effect-handling code

      return handlerNode.executeGeneric(frame);
    } finally {
      HANDLERS.set(handlers);

      /*
       * whatever, we're done with all our parameters now
       * 
       * TODO Do we also want to invalidate the side-effect mediator passed to the
       * handler thread? Surely it shouldn't continue to have side-effects at this
       * point...
       */
      effectPerformers.interrupt();
      try {
        effectPerformers.join();
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
