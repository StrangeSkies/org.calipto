package org.strum.node.intrinsic;

import org.strum.StrumTypeException;
import org.strum.node.StrumNode;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.UnsupportedSpecializationException;
import com.oracle.truffle.api.frame.VirtualFrame;

/**
 * An intrinsic function. Intrinsics can only override standard library
 * functions, and
 */
@NodeChild(value = "arguments", type = StrumNode[].class)
@GenerateNodeFactory
public abstract class IntrinsicNode extends StrumNode {
  @Override
  public final Object executeGeneric(VirtualFrame frame) {
    try {
      return execute(frame);
    } catch (UnsupportedSpecializationException e) {
      throw new StrumTypeException(e.getNode(), e.getSuppliedValues()[0]);
    }
  }

  protected abstract Object execute(VirtualFrame frame);
}
