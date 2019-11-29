package org.calipto.type;

import java.util.Iterator;

/**
 * An iterator over a Lisp data item. Conses and symbols are both considered
 * lists; a symbol is an empty list, with nil being a proper list and any other
 * symbol being improper.
 * 
 * @author Elias N Vasylenko
 *
 */
public interface DataIterator extends Iterator<Object> {
  /**
   * Whether or not the data in the terminal position is known yet. This should
   * return true if the terminal item in the list is "known", i.e. can easily be
   * fetched with a minimal amount of work, and without duplicating work that
   * would be done with a call to {@link #next()}. The motivation for this is to
   * deal with, for instance, cons cells which are implemented as singly linked
   * lists where you have to iterate to the end before you can look at the
   * terminating item.
   * <p>
   * This is required to return true if {@link #hasNext()} would return false, or
   * if it would have returned true before the last invocation of {@link #next()}.
   * 
   * @return true if we know the terminal item
   */
  boolean isTerminalKnown();

  /**
   * Test whether the list described by the cons cell is proper, i.e. if it is
   * terminated by the nil symbol.
   * 
   * @return true if the cons list is terminated with nil
   * @throws IllegalStateException if {@link #isTerminalKnown()} would return true
   */
  boolean isProper();

  /**
   * Get the terminating item in the list if it is known. For a symbol, this is
   * itself.
   * 
   * @return the data in the terminal position of the cons list
   * @throws IllegalStateException if {@link #isTerminalKnown()} would return true
   */
  Object terminal();
}
