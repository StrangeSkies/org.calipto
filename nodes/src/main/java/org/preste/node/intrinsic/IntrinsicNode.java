package org.preste.node.intrinsic;

import org.preste.PresteTypeException;
import org.preste.node.PresteNode;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.UnsupportedSpecializationException;
import com.oracle.truffle.api.frame.VirtualFrame;

/**
 * An intrinsic function. Intrinsics can only override standard library
 * functions, and
 */
@NodeChild(value = "arguments", type = PresteNode[].class)
@GenerateNodeFactory
public abstract class IntrinsicNode extends PresteNode {
  @Override
  public final Object executeGeneric(VirtualFrame frame) {
    try {
      return execute(frame);
    } catch (UnsupportedSpecializationException e) {
      throw new PresteTypeException(e.getNode(), e.getSuppliedValues()[0]);
    }
  }

  protected abstract Object execute(VirtualFrame frame);
}
