package org.calipto.reader.scanning;

public class ScanningReaderException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public ScanningReaderException(String message) {
    super(message);
  }

  public ScanningReaderException(String message, Throwable cause) {
    super(message, cause);
  }
}
