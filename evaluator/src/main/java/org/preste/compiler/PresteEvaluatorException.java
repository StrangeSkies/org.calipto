package org.preste.compiler;

public class PresteEvaluatorException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public PresteEvaluatorException(String message) {
    super(message);
  }

  public PresteEvaluatorException(String message, Throwable cause) {
    super(message, cause);
  }
}
