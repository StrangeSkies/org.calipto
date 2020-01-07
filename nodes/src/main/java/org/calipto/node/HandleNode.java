package org.calipto.node;

import static java.util.Objects.requireNonNull;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
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
public abstract class HandleNode extends ScopingNode {
  static final ThreadLocal<Handlers> HANDLERS = new ThreadLocal<>() {
    @Override
    protected Handlers initialValue() {
      return new Handlers();
    }
  };

  @Child
  private CaliptoNode targetNode;
  @Children
  private final CaliptoNode[] argumentNodes;
  private final FrameDescriptor frameDescriptor;
  @Child
  private InteropLibrary library;

  public HandleNode(CaliptoNode handlerNode, CaliptoNode[] argumentNodes) {
    this.targetNode = requireNonNull(handlerNode);
    this.argumentNodes = requireNonNull(argumentNodes);
    this.library = InteropLibrary.getFactory().createDispatched(3);

    frameDescriptor = new FrameDescriptor();
    for (int i = 0; i < argumentNodes.length; i++) {
      createAssignment(frameDescriptor, "", argumentNodes[i], i);
    }
  }

  public static CaliptoNode createAssignment(
      FrameDescriptor frameDescriptor,
      String name,
      CaliptoNode valueNode,
      int argumentIndex) {
    requireNonNull(name);
    requireNonNull(valueNode);

    FrameSlot frameSlot = frameDescriptor
        .findOrAddFrameSlot(name, argumentIndex, FrameSlotKind.Illegal);
    final CaliptoNode result = CaliptoWriteLocalVariableNodeGen.create(valueNode, frameSlot);

    if (valueNode.hasSource()) {
      final int start = valueNode.getSourceCharIndex();
      final int length = valueNode.getSourceEndIndex() - start;
      result.setSourceSection(start, length);
    }
    result.addExpressionTag();

    return result;
  }

  @ExplodeLoop
  @Override
  public Object executeGeneric(VirtualFrame frame) {
    /*
     * TODO think about how to specialise param assignments
     */

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
