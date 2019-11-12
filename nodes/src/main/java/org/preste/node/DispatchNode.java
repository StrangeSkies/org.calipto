package org.preste.node;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;

public abstract class DispatchNode extends Node {
  protected abstract Object executeDispatch(
      VirtualFrame virtualFrame,
      CallTarget callTarget,
      Object argumentValue);
}