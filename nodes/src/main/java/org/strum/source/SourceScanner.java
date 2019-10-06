package org.strum.source;

import java.util.function.IntPredicate;

import org.strum.scanner.Cursor;
import org.strum.scanner.Scanner;

import com.oracle.truffle.api.source.Source;

public class SourceScanner implements Scanner {
  private final Source source;

  public SourceScanner(Source source) {
    this.source = source;
  }

  @Override
  public long inputPosition() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public long bufferPosition() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Cursor peekInput() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Cursor advanceInput() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Cursor advanceInputWhile(IntPredicate condition) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String takeBufferTo(long position) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void discardBufferTo(long position) {
    // TODO Auto-generated method stub

  }

}
