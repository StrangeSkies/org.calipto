package org.strum.node;

public class StrumException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public StrumException(String message, Throwable cause) {
    super(message, cause);
  }

  public StrumException(Throwable cause) {
    super(cause);
  }
}
