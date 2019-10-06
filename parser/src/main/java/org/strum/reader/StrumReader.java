package org.strum.reader;

import static java.lang.Character.codePointOf;

import java.util.Optional;

import org.strum.scanner.Scanner;

public class StrumReader {
  private static final String KEYWORD = "keyword";
  private static final String QUOTE = "quote";

  private final StrumFactory factory;
  private final Scanner scanner;

  private final Object keywordSymbol;
  private final Object quoteSymbol;

  public StrumReader(StrumFactory factory, Scanner scanner) {
    this.factory = factory;
    this.scanner = scanner;

    this.keywordSymbol = factory.symbol(KEYWORD);
    this.quoteSymbol = factory.symbol(QUOTE);
  }

  /*
   * Read the next item following the current source position
   */
  public Optional<Object> read() {
    skipWhitespace();
    return scanNext();
  }

  private void skipWhitespace() {
    scanner.advanceInputWhile(Character::isWhitespace);
    scanner.discardBuffer();
  }

  private Optional<Object> scanNext() {
    return scanner.peekInput().mapCodePoint(codePoint -> {

      if (Character.isAlphabetic(codePoint)) {
        return scanSymbol();

      } else if (codePoint == codePointOf("(")) {
        return scanList();

      } else if (codePoint == codePointOf(":")) {
        return scanKeyword();

      } else if (codePoint == codePointOf("'")) {
        return scanQuote();

      }

      throw new UnsupportedOperationException(
          "Custom reader macros are not yet supported. Unexpected character: "
              + Character.toString(codePoint));
    });
  }

  private Object scanSymbol() {
    String firstPart = scanName();

    if (scanner.peekInput().codePointMatches(c -> c == codePointOf("/"))) {
      scanner.advanceInput();
      scanner.discardBuffer();
      String secondPart = scanName();

      return factory.symbol(firstPart, secondPart);
    }

    return factory.symbol(firstPart);
  }

  private String scanName() {
    scanner
        .advanceInputWhile(
            codePoint -> Character.isAlphabetic(codePoint) || codePoint == codePointOf("-"));
    return scanner.takeBuffer();
  }

  private Object scanList() {
    scanner.advanceInput();
    skipWhitespace();

    if (scanner.peekInput().codePointMatches(c -> c == codePointOf(")"))) {
      scanner.advanceInput();
      scanner.discardBuffer();
      return factory.nil();

    } else {
      return factory.cons(scanNext(), scanListTail());
    }
  }

  private Object scanListTail() {
    skipWhitespace();

    if (scanner.peekInput().codePointMatches(c -> c == codePointOf(")"))) {
      scanner.advanceInput();
      scanner.discardBuffer();
      return factory.nil();

    } else if (scanner.peekInput().codePointMatches(c -> c == codePointOf("."))) {
      scanner.advanceInput();
      scanner.discardBuffer();
      return scanNext();

    } else {
      return factory.cons(scanNext(), scanListTail());
    }
  }

  private Object scanKeyword() {
    scanner.advanceInput();
    scanner.discardBuffer();

    return factory.cons(keywordSymbol, read().get());
  }

  private Object scanQuote() {
    scanner.advanceInput();
    scanner.discardBuffer();

    return factory.cons(quoteSymbol, read().get());
  }
}
