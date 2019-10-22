package org.strum.reader;

import static java.lang.Character.codePointOf;

import java.util.Optional;

import org.strum.scanner.Scanner;

public class StrumReader {
  private static final String KEYWORD = "keyword";
  private static final String QUOTE = "quote";
  private static final String NIL = "nil";

  private final StrumFactory factory;
  private final Scanner scanner;

  private final StrumExpression keywordSymbol;
  private final StrumExpression quoteSymbol;

  public StrumReader(StrumFactory factory, Scanner scanner) {
    this.factory = factory;
    this.scanner = scanner;

    this.keywordSymbol = factory.symbol(KEYWORD).synthetic();
    this.quoteSymbol = factory.symbol(QUOTE).synthetic();
  }

  /*
   * Read the next item following the current source position
   */
  public Optional<StrumExpression> read() {
    skipWhitespace();
    return scan();
  }

  private void skipWhitespace() {
    scanner.advanceInputWhile(Character::isWhitespace);
    scanner.discardBuffer();
  }

  private StrumExpression scanNext() {
    return scan().orElseThrow(() -> new StrumReaderException("Unexpected end of input"));
  }

  private Optional<StrumExpression> scan() {
    return scanner.peekInput().mapCodePoint(codePoint -> {
      long startPosition = scanner.inputPosition();

      StrumBuilder builder;
      if (Character.isAlphabetic(codePoint)) {
        builder = scanSymbol();

      } else if (codePoint == codePointOf("(")) {
        builder = scanList();

      } else if (codePoint == codePointOf(":")) {
        builder = scanKeyword();

      } else if (codePoint == codePointOf("'")) {
        builder = scanQuote();

      } else {
        throw new UnsupportedOperationException(
            "Custom reader macros are not yet supported. Unexpected character: "
                + Character.toString(codePoint));
      }

      return builder.between(startPosition, scanner.inputPosition());
    });
  }

  private StrumBuilder scanSymbol() {
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

  private StrumBuilder scanList() {
    scanner.advanceInput();
    skipWhitespace();

    return scanListStart();
  }

  private StrumBuilder scanListStart() {
    if (scanner.peekInput().codePointMatches(c -> c == codePointOf(")"))) {
      scanner.advanceInput();
      scanner.discardBuffer();
      return factory.symbol(NIL);

    } else {
      return factory.cons(scanNext(), scanListTail());
    }
  }

  private StrumExpression scanListTail() {
    skipWhitespace();

    if (scanner.peekInput().codePointMatches(c -> c == codePointOf("."))) {
      scanner.advanceInput();
      scanner.discardBuffer();
      return scanNext();

    } else {
      long startPosition = scanner.inputPosition();
      return scanListStart().between(startPosition, scanner.inputPosition());
    }
  }

  private StrumBuilder scanKeyword() {
    scanner.advanceInput();
    scanner.discardBuffer();

    return factory.cons(keywordSymbol, read().get());
  }

  private StrumBuilder scanQuote() {
    scanner.advanceInput();
    scanner.discardBuffer();

    return factory.cons(quoteSymbol, read().get());
  }
}
