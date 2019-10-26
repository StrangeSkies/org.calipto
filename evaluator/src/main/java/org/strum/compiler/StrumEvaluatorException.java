package org.strum.compiler;

public class StrumEvaluatorException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public StrumEvaluatorException(String message) {
    super(message);
  }

  public StrumEvaluatorException(String message, Throwable cause) {
    super(message, cause);
  }
}
