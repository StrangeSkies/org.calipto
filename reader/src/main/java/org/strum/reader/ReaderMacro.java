package org.strum.reader;

public class ReaderMacro {
  private final int codePoint;
  private final Object function;

  public ReaderMacro(int codePoint, Object function) {
    this.codePoint = codePoint;
    this.function = function;
  }

  public int codePoint() {
    return codePoint;
  }

  public Object function() {
    return function;
  }
}
