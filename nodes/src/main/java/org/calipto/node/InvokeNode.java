package org.calipto.node;

import java.util.Map;

import org.calipto.CaliptoTypeException;
import org.calipto.node.ScopingNode.VariablesMapObject;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.nodes.ExplodeLoop;

public abstract class InvokeNode extends ScopingNode {
  @Child
  private CaliptoNode targetNode;
  @Children
  private final CaliptoNode[] argumentNodes;
  @Child
  private InteropLibrary library;

  public InvokeNode(CaliptoNode targetNode, CaliptoNode[] argumentNodes) {
    this.targetNode = targetNode;
    this.argumentNodes = argumentNodes;
    this.library = InteropLibrary.getFactory().createDispatched(3);
  }

  @ExplodeLoop
  @Override
  public Object executeGeneric(VirtualFrame frame) {
    Object function = targetNode.executeGeneric(frame);

    CompilerAsserts.compilationConstant(argumentNodes.length);

    Object[] argumentValues = new Object[argumentNodes.length];
    for (int i = 0; i < argumentNodes.length; i++) {
      argumentValues[i] = argumentNodes[i].executeGeneric(frame);
    }

    try {
      return library.execute(function, argumentValues);
    } catch (ArityException | UnsupportedTypeException | UnsupportedMessageException e) {
      throw new CaliptoTypeException(targetNode, argumentValues);
    }
  }

  @Override
  public boolean hasTag(Class<? extends Tag> tag) {
    if (tag == StandardTags.CallTag.class) {
      return true;
    }
    return super.hasTag(tag);
  }

  @Override
  public Object getVariables(Frame frame) {
    if (root == null) {
      // No arguments for block scope
      return null;
    }
    // The slots give us names of the arguments:
    Map<String, FrameSlot> argSlots = collectArgs(block);
    // The frame's arguments array give us the argument values:
    Object[] args = (frame != null) ? frame.getArguments() : null;
    // Create a TruffleObject having the arguments as properties:
    return new VariablesMapObject(argSlots, args, frame);
  }
}
