package org.preste;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;

public class PresteTypeException extends PresteException implements TruffleException {
  private static final long serialVersionUID = 1L;

  @TruffleBoundary
  public PresteTypeException(Node operation, Object value) {
    super(operation, writeMessage(operation, value));
  }

  private static String writeMessage(Node operation, Object value) {
    StringBuilder result = new StringBuilder();
    result.append("Type error: operation");

    if (operation != null) {
      NodeInfo nodeInfo = PresteContext.lookupNodeInfo(operation.getClass());
      if (nodeInfo != null) {
        result.append(" \"").append(nodeInfo.shortName()).append("\"");
      }
    }

    result.append(" not defined for ");

    if (value != null && !InteropLibrary.getFactory().getUncached().isNull(value)) {
      result.append(PresteLanguage.toString(value)).append(" ");
    }
    result.append(PresteLanguage.toString(value));

    return result.toString();
  }
}
