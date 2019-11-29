package org.calipto.scanner;

import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.Supplier;

//TODO inline class
public class Cursor {
  private final boolean endOfInput;
  private final int codePoint;

  public Cursor() {
    this.endOfInput = true;
    this.codePoint = 0;
  }

  public Cursor(int codePoint) {
    this.endOfInput = false;
    this.codePoint = codePoint;
  }

  public boolean isEndOfInput() {
    return endOfInput;
  }

  public <T> Optional<T> mapCodePoint(IntFunction<T> mapping) {
    if (endOfInput) {
      return Optional.empty();
    } else {
      return Optional.ofNullable(mapping.apply(codePoint));
    }
  }

  public <E extends Throwable> int codePointOrThrow(Supplier<E> throwable) throws E {
    if (endOfInput) {
      throw throwable.get();
    }
    return codePoint;
  }

  public boolean codePointMatches(IntPredicate condition) {
    return !endOfInput && condition.test(codePoint);
  }
}
