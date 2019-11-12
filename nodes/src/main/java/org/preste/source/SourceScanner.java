package org.preste.source;

import static java.lang.Character.offsetByCodePoints;

import java.util.function.IntPredicate;

import org.preste.scanner.Cursor;
import org.preste.scanner.Scanner;

import com.oracle.truffle.api.source.Source;

public class SourceScanner implements Scanner {
  private static final SourcePosition START = new SourcePosition(0, 0);

  // TODO record type
  private static class SourcePosition {
    private int chars;
    private int codePoints;

    public SourcePosition(int chars, int codePoints) {
      this.chars = chars;
      this.codePoints = codePoints;
    }

    public int chars() {
      return chars;
    }

    public int codePoints() {
      return codePoints;
    }

    public SourcePosition getAdvanced(int codePoint) {
      if (Character.isSupplementaryCodePoint(codePoint)) {
        return new SourcePosition(chars() + 2, codePoints() + 1);
      } else {
        return new SourcePosition(chars() + 1, codePoints() + 1);
      }
    }
  }

  private final CharSequence characters;
  private SourcePosition inputPosition = START;
  private SourcePosition bufferPosition = START;

  public SourceScanner(Source source) {
    this.characters = source.getCharacters();
  }

  @Override
  public long inputPosition() {
    return inputPosition.codePoints();
  }

  @Override
  public long bufferPosition() {
    return bufferPosition.codePoints();
  }

  @Override
  public Cursor peekInput() {
    if (inputPosition.chars() < characters.length()) {
      return new Cursor();
    } else {
      return new Cursor(Character.codePointAt(characters, inputPosition.chars()));
    }
  }

  @Override
  public Cursor advanceInput() {
    if (inputPosition.chars() < characters.length()) {
      int codePoint = Character.codePointAt(characters, inputPosition.chars());
      inputPosition = inputPosition.getAdvanced(codePoint);
      return new Cursor(codePoint);
    }
    return new Cursor();
  }

  @Override
  public Cursor advanceInputWhile(IntPredicate condition) {
    while (inputPosition.chars() < characters.length()) {
      int codePoint = Character.codePointAt(characters, inputPosition.chars());
      if (!condition.test(codePoint)) {
        break;
      }
      inputPosition = inputPosition.getAdvanced(codePoint);
    }
    return new Cursor();
  }

  @Override
  public String takeBufferTo(long position) {
    int from = bufferPosition.chars();
    discardBufferTo(position);
    return characters.subSequence(from, bufferPosition.chars()).toString();
  }

  @Override
  public void discardBufferTo(long position) {
    checkBufferIndex(position);

    var fullBuffer = characters.subSequence(bufferPosition.chars(), inputPosition.chars());
    var charCount = offsetByCodePoints(fullBuffer, 0, (int) position - bufferPosition.codePoints());
    bufferPosition = new SourcePosition(bufferPosition.chars() + charCount, (int) position);
  }

  private void checkBufferIndex(long position) {
    if (position < bufferPosition() || position > inputPosition()) {
      throw new IndexOutOfBoundsException();
    }
  }
}
