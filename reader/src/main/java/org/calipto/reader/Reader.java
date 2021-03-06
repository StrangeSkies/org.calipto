package org.calipto.reader;

import java.util.Optional;

public interface Reader {
  default long cursorPosition() {
    return cursorPosition(cursorDepth());
  }

  long cursorPosition(int inputDepth);

  int cursorDepth();

  /**
   * At the current depth, given that there are more items at the current
   * position, advance the position.
   * 
   * @return the item advanced over
   */
  Optional<Object> read();

  /**
   * At the current depth, given that there are more items at the current
   * position, and that the next item is a symbol, advance the position.
   * 
   * @return the symbol advanced over
   */
  Optional<Object> readSymbol();

  /**
   * At the current depth, given that the next item is a list, advance the
   * current depth and set the position to 0 at the next depth, thus entering
   * the list.
   * 
   * @return true if the depth has increased as a result of the call
   */
  boolean readStepIn();

  /**
   * Given we are not at depth 0, return to the preceding depth, then advance
   * the position, thus exiting the list.
   * 
   * @return the tail of the list we just exited, including the item in the
   *         terminal position
   */
  Optional<Object> readStepOut();
}
