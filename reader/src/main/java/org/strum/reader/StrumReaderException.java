package org.strum.reader;

public class StrumReaderException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public StrumReaderException(String message) {
    super(message);
  }

  public StrumReaderException(String message, Throwable cause) {
    super(message, cause);
  }
}
