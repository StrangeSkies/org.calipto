package org.calipto.node;

import com.oracle.truffle.api.frame.VirtualFrame;

public class ReadArgumentNode extends CaliptoNode {
  @Override
  public Object executeGeneric(VirtualFrame frame) {
    return frame.getArguments()[0];
  }
}
