package org.calipto.reader;

import static java.lang.Character.codePointOf;
import static org.calipto.type.symbol.Symbols.SYSTEM_NAMESPACE;

import java.util.Optional;

import org.calipto.scanner.Scanner;
import org.calipto.type.DataLibrary;
import org.calipto.type.symbol.SymbolIndex;
import org.calipto.type.symbol.Symbols;
import org.calipto.walker.Walker;

public class Reader implements Walker {
  private static final String KEYWORD = "keyword";

  private final DataLibrary data;
  private final SymbolIndex symbols;
  private final Scanner scanner;

  /*
   * TODO Do we need a stack of scanners, sub-scanners, and reader-macro states so
   * that we can transition to streaming from reader macros? Perhaps all reader
   * macros have to open a list, and may then append items to that list as a
   * side-effect. This is not too limiting as this can punt to more flexible
   * macros. The reader part can and should be simple.
   */

  private final Object keywordSymbol;

  public Reader(SymbolIndex symbols, Scanner scanner) {
    this.data = DataLibrary.getFactory().getUncached();
    this.symbols = symbols;
    this.scanner = scanner;

    this.keywordSymbol = symbols.internSymbol(SYSTEM_NAMESPACE, KEYWORD);
  }

  private Object cons(Object car, Object cdr) {
    var cons = data.consOnto(car, cdr);
    if (cons == null) {
      cons = data.consWith(cdr, car);
    }
    return cons;
  }

  private void skipWhitespace() {
    scanner.advanceInputWhile(Character::isWhitespace);
    scanner.discardBuffer();
  }

  private Object scanNext() {
    return scan().orElseThrow(() -> new CaliptoReaderException("Unexpected end of input"));
  }

  private Optional<Object> scan() {
    long startPosition = scanner.inputPosition();

    var data = scanSymbol()
        .or(() -> scanList())
        .or(() -> scanKeyword())
        .or(() -> scanQuote())
        .or(() -> scanMacro());

    recordSourceLocation(startPosition, scanner.inputPosition());

    return data;
  }

  private Optional<Object> scanSymbol() {
    return scanName().map(firstPart -> {
      if (scanner.advanceInputIf(c -> c == codePointOf("/"))) {
        String secondPart = scanName()
            .orElseThrow(() -> new CaliptoReaderException("Symbol name expected after namespace"));

        return symbols.internSymbol(firstPart, secondPart);
      }

      return symbols.internSymbol(SYSTEM_NAMESPACE, firstPart);
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

  private Optional<Object> scanList() {
    if (scanner.advanceInputIf(c -> c == codePointOf("("))) {
      skipWhitespace();

      return Optional.of(scanListStart());
    }
    return Optional.empty();
  }

  private Object scanListStart() {
    if (scanner.advanceInputIf(c -> c == codePointOf(")"))) {
      scanner.discardBuffer();

      return Symbols.NIL;

    } else {
      return cons(scanNext(), scanListTail());
    }
  }

  private Object scanListTail() {
    skipWhitespace();

    if (scanner.advanceInputIf(c -> c == codePointOf("."))) {
      skipWhitespace();
      return scanNext();

    } else {
      long startPosition = scanner.inputPosition();

      recordSourceLocation(startPosition, scanner.inputPosition());

      return scanListStart();
    }
  }

  private Optional<Object> scanKeyword() {
    if (scanner.advanceInputIf(c -> c == codePointOf(":"))) {
      scanner.discardBuffer();

      recordSyntheticExpression();

      return Optional.of(cons(keywordSymbol, read().get()));
    }
    return Optional.empty();
  }

  private Optional<Object> scanQuote() {
    if (scanner.advanceInputIf(c -> c == codePointOf("'"))) {
      scanner.discardBuffer();

      recordSyntheticExpression();

      return Optional.of(cons(Symbols.QUOTE, read().get()));
    }
    return Optional.empty();
  }

  private Optional<Object> scanMacro() {
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
  public Optional<Object> read() {
    skipWhitespace();
    return scan();
  }

  @Override
  public Optional<Object> readSymbol() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean readStepIn() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Optional<Object> readStepOut() {
    // TODO Auto-generated method stub
    return null;
  }
}
