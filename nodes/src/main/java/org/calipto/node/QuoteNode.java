package org.calipto.node;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo(shortName = "quote")
public final class QuoteNode extends CaliptoNode {
  private final Object data;

  public QuoteNode(Object data) {
    this.data = data;
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    return data;
  }
}
