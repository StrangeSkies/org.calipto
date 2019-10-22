package org.strum.reader;

public interface StrumBuilder {
  StrumData between(long startPosition, long endPosition);

  StrumData synthetic();
}
