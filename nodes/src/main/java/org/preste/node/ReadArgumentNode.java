package org.preste.node;

import com.oracle.truffle.api.frame.VirtualFrame;

public class ReadArgumentNode extends PresteNode {
  private final int argumentIndex;

  public ReadArgumentNode(int argumentIndex) {
    this.argumentIndex = argumentIndex;
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    // TODO Auto-generated method stub
    return null;
  }

}
