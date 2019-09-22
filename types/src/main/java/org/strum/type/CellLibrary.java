package org.strum.type;

import com.oracle.truffle.api.library.GenerateLibrary;
import com.oracle.truffle.api.library.Library;

@GenerateLibrary
public abstract class CellLibrary extends Library {
  public abstract Object car();

  public abstract Object cdr();

  public abstract CellLibrary cons(Symbol symbol);
}
