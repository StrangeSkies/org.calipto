package org.calipto;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;

public class CaliptoTypeException extends CaliptoException implements TruffleException {
  private static final long serialVersionUID = 1L;

  @TruffleBoundary
  public CaliptoTypeException(Node operation, Object value) {
    super(operation, writeMessage(operation, value));
  }

  private static String writeMessage(Node operation, Object value) {
    StringBuilder result = new StringBuilder();
    result.append("Type error: operation");

    if (operation != null) {
      NodeInfo nodeInfo = CaliptoContext.lookupNodeInfo(operation.getClass());
      if (nodeInfo != null) {
        result.append(" \"").append(nodeInfo.shortName()).append("\"");
      }
    }

    result.append(" not defined for ");

    if (value != null && !InteropLibrary.getFactory().getUncached().isNull(value)) {
      result.append(CaliptoLanguage.toString(value)).append(" ");
    }
    result.append(CaliptoLanguage.toString(value));

    return result.toString();
  }
}
