package org.strum.type.cells;

import org.strum.type.CellLibrary;

import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

@ExportLibrary(CellLibrary.class)
final class ConsCell {
  private final Object car;
  private final Object cdr;

  public ConsCell(Object car, Object cdr) {
    this.car = car;
    this.cdr = cdr;
  }

  @ExportMessage
  Object car() {
    return car;
  }

  @ExportMessage
  Object cdr() {
    return cdr;
  }

  
}
