package org.strum.compiler;

public class StrumCompilerException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public StrumCompilerException(String message) {
    super(message);
  }

  public StrumCompilerException(String message, Throwable cause) {
    super(message, cause);
  }
}
