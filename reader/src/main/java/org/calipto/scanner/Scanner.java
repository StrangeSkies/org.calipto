package org.calipto.scanner;

import java.util.function.IntPredicate;

public interface Scanner {
  /**
   * @return the input position at the head of the scan
   */
  long inputPosition();

  /**
   * @return the position from which text is buffered, up to the input position
   */
  long bufferPosition();

  /**
   * @return the input position relative to the current retained position.
   */
  default long bufferSize() {
    return inputPosition() - bufferPosition();
  }

  Cursor advanceInput();

  /**
   * Advance the input position while the character at that position matches the
   * given predicate.
   * 
   * @param condition
   * @return the cursor at the first input position which doesn't match the
   *         predicate, which may be the end of input
   */
  Cursor advanceInputWhile(IntPredicate condition);

  /**
   * Advance the input position if the character at that position matches the
   * given predicate.
   * 
   * @param condition
   * @return the cursor at the first input position which doesn't match the
   *         predicate, which may be the end of input
   */
  Cursor advanceInputIf(IntPredicate condition);

  /**
   * Take everything in the interval from the mark position to the given
   * position and reset the mark position to the given position.
   * 
   * @return a text object containing the taken interval
   */
  String takeBufferTo(long position);

  /**
   * Take everything in the interval from the mark position to the input
   * position and reset the mark position to the input position.
   * 
   * @return a text object containing the taken interval
   */
  default String takeBuffer() {
    return takeBufferTo(inputPosition());
  }

  void discardBufferTo(long position);

  /**
   * Set the mark position to the current input position, discarding everything
   * prior.
   */
  default void discardBuffer() {
    discardBufferTo(inputPosition());
  }
}
