package org.strum.reader;

import static java.lang.Character.codePointOf;

import java.util.Optional;

import org.strum.scanner.Scanner;

public class StrumReader {
  private static final String CORE_NAMESPACE = "strum";

  private static final String KEYWORD = "keyword";
  private static final String QUOTE = "quote";
  private static final String NIL = "nil";

  private final StrumDataFactory factory;
  private final Scanner scanner;

  private final StrumData keywordSymbol;
  private final StrumData quoteSymbol;

  public StrumReader(StrumDataFactory factory, Scanner scanner) {
    this.factory = factory;
    this.scanner = scanner;

    this.keywordSymbol = factory.symbol(CORE_NAMESPACE, KEYWORD).synthetic();
    this.quoteSymbol = factory.symbol(CORE_NAMESPACE, QUOTE).synthetic();
  }

  /*
   * Read the next item following the current source position
   */
  public Optional<StrumData> read() {
    skipWhitespace();
    return scan();
  }

  private void skipWhitespace() {
    scanner.advanceInputWhile(Character::isWhitespace);
    scanner.discardBuffer();
  }

  private StrumData scanNext() {
    return scan().orElseThrow(() -> new StrumReaderException("Unexpected end of input"));
  }

  private Optional<StrumData> scan() {
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

    return factory.symbol(CORE_NAMESPACE, firstPart);
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
      return factory.symbol(CORE_NAMESPACE, NIL);

    } else {
      return factory.cons(scanNext(), scanListTail());
    }
  }

  private StrumData scanListTail() {
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
