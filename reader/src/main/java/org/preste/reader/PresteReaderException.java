package org.preste.reader;

public class PresteReaderException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public PresteReaderException(String message) {
    super(message);
  }

  public PresteReaderException(String message, Throwable cause) {
    super(message, cause);
  }
}
