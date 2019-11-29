package org.calipto.node.builtin;

import org.calipto.CaliptoTypeException;
import org.calipto.node.CaliptoNode;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.UnsupportedSpecializationException;
import com.oracle.truffle.api.frame.VirtualFrame;

/**
 * A node for built-in side effects. This represents the enclosing context which
 * can be yielded to in order to perform some platform-provided side effect.
 */
@NodeChild(value = "arguments", type = CaliptoNode[].class)
@GenerateNodeFactory
public abstract class BuiltinNode extends CaliptoNode {
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
