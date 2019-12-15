package org.calipto.reader;

import static java.lang.Character.codePointOf;

import java.util.Optional;

import org.calipto.scanner.Scanner;

public class CaliptoReader {
  private static final String CORE_NAMESPACE = "calipto";

  private static final String KEYWORD = "keyword";
  private static final String QUOTE = "quote";
  private static final String NIL = "nil";

  private final ReadingContext context;
  private final Scanner scanner;

  private final CaliptoData keywordSymbol;
  private final CaliptoData quoteSymbol;

  public CaliptoReader(ReadingContext context, Scanner scanner) {
    this.context = context;
    this.scanner = scanner;

    this.keywordSymbol = context.makeSymbol(CORE_NAMESPACE, KEYWORD);
    this.quoteSymbol = context.makeSymbol(CORE_NAMESPACE, QUOTE);
  }

  /*
   * Read the next item following the current source position
   */
  public Optional<CaliptoData> read() {
    skipWhitespace();
    return scan();
  }

  private void skipWhitespace() {
    scanner.advanceInputWhile(Character::isWhitespace);
    scanner.discardBuffer();
  }

  private CaliptoData scanNext() {
    return scan().orElseThrow(() -> new CaliptoReaderException("Unexpected end of input"));
  }

  private Optional<CaliptoData> scan() {
    return scanner.peekInput().mapCodePoint(codePoint -> {
      long startPosition = scanner.inputPosition();

      CaliptoData data;
      if (Character.isAlphabetic(codePoint)) {
        data = scanSymbol();

      } else if (codePoint == codePointOf("(")) {
        data = scanList();

      } else if (codePoint == codePointOf(":")) {
        data = scanKeyword();

      } else if (codePoint == codePointOf("'")) {
        data = scanQuote();

      } else {
        var macro = context
            .findCharacterMacro(codePoint)
            .orElseThrow(
                () -> new UnsupportedOperationException(
                    "Custom reader macros are not yet supported. Unexpected character: "
                        + Character.toString(codePoint)));

        data = macro.call();
      }

      recordSourceLocation(startPosition, scanner.inputPosition());

      return data;
    });
  }

  private void recordSyntheticExpression() {
    // TODO implement as a side effect
  }

  private void recordSourceLocation(long startPosition, long inputPosition) {
    // TODO implement as a side effect
  }

  private CaliptoData scanSymbol() {
    String firstPart = scanName();

    if (scanner.peekInput().codePointMatches(c -> c == codePointOf("/"))) {
      scanner.advanceInput();
      scanner.discardBuffer();
      String secondPart = scanName();

      return context.makeSymbol(firstPart, secondPart);
    }

    return context.makeSymbol(CORE_NAMESPACE, firstPart);
  }

  private String scanName() {
    scanner
        .advanceInputWhile(
            codePoint -> Character.isAlphabetic(codePoint) || codePoint == codePointOf("-"));
    return scanner.takeBuffer();
  }

  private CaliptoData scanList() {
    scanner.advanceInput();
    skipWhitespace();

    return scanListStart();
  }

  private CaliptoData scanListStart() {
    if (scanner.peekInput().codePointMatches(c -> c == codePointOf(")"))) {
      scanner.advanceInput();
      scanner.discardBuffer();

      return context.makeSymbol(CORE_NAMESPACE, NIL);

    } else {
      return context.makeCons(scanNext(), scanListTail());
    }
  }

  private CaliptoData scanListTail() {
    skipWhitespace();

    if (scanner.peekInput().codePointMatches(c -> c == codePointOf("."))) {
      scanner.advanceInput();
      scanner.discardBuffer();
      return scanNext();

    } else {
      long startPosition = scanner.inputPosition();

      recordSourceLocation(startPosition, scanner.inputPosition());

      return scanListStart();
    }
  }

  private CaliptoData scanKeyword() {
    scanner.advanceInput();
    scanner.discardBuffer();

    recordSyntheticExpression();

    return context.makeCons(keywordSymbol, read().get());
  }

  private CaliptoData scanQuote() {
    scanner.advanceInput();
    scanner.discardBuffer();

    recordSyntheticExpression();

    return context.makeCons(quoteSymbol, read().get());
  }
}
