package org.calipto.reader;

public class CaliptoReaderException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public CaliptoReaderException(String message) {
    super(message);
  }

  public CaliptoReaderException(String message, Throwable cause) {
    super(message, cause);
  }
}
