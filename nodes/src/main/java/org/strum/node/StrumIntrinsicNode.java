package org.strum.node;

/**
 * An intrinsic function. Intrinsics can only override standard library
 * functions, and
 */
@NodeChild(value = "arguments", type = SLExpressionNode[].class)
@GenerateNodeFactory
public class StrumIntrinsicNode extends StrumNode {

  @Override
  public final Object executeGeneric(VirtualFrame frame) {
    try {
      return execute(frame);
    } catch (UnsupportedSpecializationException e) {
      throw SLException.typeError(e.getNode(), e.getSuppliedValues());
    }
  }

  @Override
  public final boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
    return super.executeBoolean(frame);
  }

  @Override
  public final long executeLong(VirtualFrame frame) throws UnexpectedResultException {
    return super.executeLong(frame);
  }

  @Override
  public final void executeVoid(VirtualFrame frame) {
    super.executeVoid(frame);
  }

  protected abstract Object execute(VirtualFrame frame);
}
