package org.calipto.reader;

import static java.lang.Character.codePointOf;

import java.util.Optional;

import org.calipto.scanner.Scanner;
import org.calipto.walker.Walker;

public class Reader implements Walker {
  private static final String CORE_NAMESPACE = "calipto";

  private static final String KEYWORD = "keyword";
  private static final String QUOTE = "quote";
  private static final String NIL = "nil";

  private final ReadingContext context;
  private final Scanner scanner;

  /*
   * TODO Do we need a stack of scanners, sub-scanners, and reader-macro states
   * so that we can transition to streaming from reader macros? Perhaps all
   * reader macros have to open a list, and may then append items to that list
   * as a side-effect. This is not too limiting as this can punt to more
   * flexible macros. The reader part can and should be simple.
   */

  private final CaliptoData keywordSymbol;
  private final CaliptoData quoteSymbol;

  public Reader(ReadingContext context, Scanner scanner) {
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
    long startPosition = scanner.inputPosition();

    var data = scanSymbol()
        .or(() -> scanList())
        .or(() -> scanKeyword())
        .or(() -> scanQuote())
        .or(() -> scanMacro());

    recordSourceLocation(startPosition, scanner.inputPosition());

    return data;
  }

  private Optional<CaliptoData> scanSymbol() {
    return scanName().map(firstPart -> {
      if (scanner.advanceInputIf(c -> c == codePointOf("/"))) {
        String secondPart = scanName()
            .orElseThrow(() -> new CaliptoReaderException("Symbol name expected after namespace"));

        return context.makeSymbol(firstPart, secondPart);
      }

      return context.makeSymbol(CORE_NAMESPACE, firstPart);
    });
  }

  private Optional<String> scanName() {
    scanner.discardBuffer();
    if (scanner.advanceInputIf(c -> Character.isAlphabetic(c))) {
      scanner.advanceInputWhile(c -> Character.isAlphabetic(c) || c == codePointOf("-"));
      return Optional.of(scanner.takeBuffer());
    }
    return Optional.empty();
  }

  private Optional<CaliptoData> scanList() {
    if (scanner.advanceInputIf(c -> c == codePointOf("("))) {
      skipWhitespace();

      return Optional.of(scanListStart());
    }
    return Optional.empty();
  }

  private CaliptoData scanListStart() {
    if (scanner.advanceInputIf(c -> c == codePointOf(")"))) {
      scanner.discardBuffer();

      return context.makeSymbol(CORE_NAMESPACE, NIL);

    } else {
      return context.makeCons(scanNext(), scanListTail());
    }
  }

  private CaliptoData scanListTail() {
    skipWhitespace();

    if (scanner.advanceInputIf(c -> c == codePointOf("."))) {
      scanner.discardBuffer();
      return scanNext();

    } else {
      long startPosition = scanner.inputPosition();

      recordSourceLocation(startPosition, scanner.inputPosition());

      return scanListStart();
    }
  }

  private Optional<CaliptoData> scanKeyword() {
    if (scanner.advanceInputIf(c -> c == codePointOf(":"))) {
      scanner.discardBuffer();

      recordSyntheticExpression();

      return Optional.of(context.makeCons(keywordSymbol, read().get()));
    }
    return Optional.empty();
  }

  private Optional<CaliptoData> scanQuote() {
    if (scanner.advanceInputIf(c -> c == codePointOf("'"))) {
      scanner.discardBuffer();

      recordSyntheticExpression();

      return Optional.of(context.makeCons(quoteSymbol, read().get()));
    }
    return Optional.empty();
  }

  private Optional<CaliptoData> scanMacro() {
    return scanner.advanceInput(c -> {
      var macro = context
          .findCharacterMacro(c)
          .orElseThrow(
              () -> new UnsupportedOperationException(
                  "Custom reader macros are not yet supported. Unexpected character: "
                      + Character.toString(c)));

      return macro.call();
    });
  }

  public long inputPosition() {
    return scanner.inputPosition();
  }

  @Override
  public long cursorPosition(long inputDepth) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public long cursorDepth() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Optional<Object> stepOver() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Optional<Object> stepOverSymbol() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean stepIntoList() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Optional<Object> stepOutOfList() {
    // TODO Auto-generated method stub
    return null;
  }
}
