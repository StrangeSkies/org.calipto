package org.strum.reader;

public interface StrumBuilder {
  StrumExpression between(long startPosition, long endPosition);

  StrumExpression synthetic();
}
