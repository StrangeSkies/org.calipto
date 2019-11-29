package org.calipto;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.SourceSection;

public class CaliptoException extends RuntimeException implements TruffleException {
  private static final long serialVersionUID = 1L;

  private final Node location;

  @TruffleBoundary
  public CaliptoException(Node location, String message) {
    super(decorateMessage(location, message));
    this.location = location;
  }

  @TruffleBoundary
  public CaliptoException(Node location, String message, Throwable cause) {
    super(decorateMessage(location, message), cause);
    this.location = location;
  }

  private static String decorateMessage(Node location, String message) {
    if (location != null) {
      SourceSection ss = location.getEncapsulatingSourceSection();
      if (ss != null && ss.isAvailable()) {
        message = message + " at " + ss.getSource().getName() + " line " + ss.getStartLine()
            + " col " + ss.getStartColumn();
      }
    }
    return message;
  }

  @Override
  public final Throwable fillInStackTrace() {
    return this;
  }

  @Override
  public Node getLocation() {
    return location;
  }
}
