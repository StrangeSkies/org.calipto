package org.calipto.node.intrinsic;

import org.calipto.CaliptoTypeException;
import org.calipto.node.CaliptoNode;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.UnsupportedSpecializationException;
import com.oracle.truffle.api.frame.VirtualFrame;

/**
 * An intrinsic function. Intrinsics can only override standard library
 * functions, and
 */
@NodeChild(value = "arguments", type = CaliptoNode[].class)
@GenerateNodeFactory
public abstract class IntrinsicNode extends CaliptoNode {
  @Override
  public final Object executeGeneric(VirtualFrame frame) {
    try {
      return execute(frame);
    } catch (UnsupportedSpecializationException e) {
      throw new CaliptoTypeException(e.getNode(), e.getSuppliedValues()[0]);
    }
  }

  protected abstract Object execute(VirtualFrame frame);
}
