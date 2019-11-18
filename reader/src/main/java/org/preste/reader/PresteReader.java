package org.preste.reader;

import static java.lang.Character.codePointOf;

import java.util.Optional;

import org.preste.scanner.Scanner;

public class PresteReader {
  private static final String CORE_NAMESPACE = "preste";

  private static final String KEYWORD = "keyword";
  private static final String QUOTE = "quote";
  private static final String NIL = "nil";

  private final ReadingContext context;
  private final Scanner scanner;

  private LexicalScope scope;

  private final PresteData keywordSymbol;
  private final PresteData quoteSymbol;

  public PresteReader(ReadingContext context, Scanner scanner) {
    this.context = context;
    this.scanner = scanner;

    this.scope = new LexicalScope();

    this.keywordSymbol = context.makeSymbol(CORE_NAMESPACE, KEYWORD);
    this.quoteSymbol = context.makeSymbol(CORE_NAMESPACE, QUOTE);
  }

  /*
   * Read the next item following the current source position
   */
  public Optional<PresteData> read() {
    skipWhitespace();
    return scan();
  }

  private void skipWhitespace() {
    scanner.advanceInputWhile(Character::isWhitespace);
    scanner.discardBuffer();
  }

  private PresteData scanNext() {
    return scan().orElseThrow(() -> new PresteReaderException("Unexpected end of input"));
  }

  private Optional<PresteData> scan() {
    return scanner.peekInput().mapCodePoint(codePoint -> {
      long startPosition = scanner.inputPosition();

      PresteData data;
      if (Character.isAlphabetic(codePoint)) {
        data = scanSymbol();

      } else if (codePoint == codePointOf("(")) {
        data = scanList();

      } else if (codePoint == codePointOf(":")) {
        data = scanKeyword();

      } else if (codePoint == codePointOf("'")) {
        data = scanQuote();

      } else {
        var macro = scope
            .findMacro(codePoint)
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

  private PresteData scanSymbol() {
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

  private PresteData scanList() {
    scanner.advanceInput();
    skipWhitespace();

    return scanListStart();
  }

  private PresteData scanListStart() {
    if (scanner.peekInput().codePointMatches(c -> c == codePointOf(")"))) {
      scanner.advanceInput();
      scanner.discardBuffer();

      return context.makeSymbol(CORE_NAMESPACE, NIL);

    } else {
      return context.makeCons(scanNext(), scanListTail());
    }
  }

  private PresteData scanListTail() {
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

  private PresteData scanKeyword() {
    scanner.advanceInput();
    scanner.discardBuffer();

    recordSyntheticExpression();

    return context.makeCons(keywordSymbol, read().get());
  }

  private PresteData scanQuote() {
    scanner.advanceInput();
    scanner.discardBuffer();

    recordSyntheticExpression();

    return context.makeCons(quoteSymbol, read().get());
  }
}
