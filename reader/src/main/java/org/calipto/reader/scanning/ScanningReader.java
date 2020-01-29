package org.calipto.reader.scanning;

import static java.lang.Character.codePointOf;
import static org.calipto.type.symbol.Symbols.SYSTEM_NAMESPACE;

import java.util.List;
import java.util.Optional;

import org.calipto.reader.Reader;
import org.calipto.scanner.Scanner;
import org.calipto.type.DataLibrary;
import org.calipto.type.symbol.SymbolIndex;
import org.calipto.type.symbol.Symbols;

public class ScanningReader implements Reader {
  private final DataLibrary data;
  private final SymbolIndex symbols;
  private final Scanner scanner;

  private List<Long> cursor;

  public ScanningReader(SymbolIndex symbols, Scanner scanner) {
    this.data = DataLibrary.getFactory().getUncached();
    this.symbols = symbols;
    this.scanner = scanner;

    this.cursor = List.of(0l);
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
    return scan()
        .orElseThrow(
            () -> new ScanningReaderException("Unexpected end of input"));
  }

  private Optional<Object> scan() {
    return scanSymbol().or(() -> scanList());
  }

  private Optional<Object> scanSymbol() {
    return scanName().map(firstPart -> {
      if (scanner.advanceInputIf(c -> c == codePointOf("/"))) {
        String secondPart = scanName()
            .orElseThrow(
                () -> new ScanningReaderException(
                    "Symbol name expected after namespace"));

        return symbols.internSymbol(firstPart, secondPart);
      }

      return symbols.internSymbol(SYSTEM_NAMESPACE, firstPart);
    });
  }

  private Optional<String> scanName() {
    scanner.discardBuffer();
    if (scanner.advanceInputIf(c -> Character.isAlphabetic(c))) {
      scanner
          .advanceInputWhile(
              c -> Character.isAlphabetic(c) || c == codePointOf("-"));
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
      return scanListStart();
    }
  }

  @Override
  public long cursorPosition(int inputDepth) {
    if (inputDepth < 0 || inputDepth > cursorDepth()) {
      return -1;
    }
    return cursor.get(inputDepth);
  }

  @Override
  public int cursorDepth() {
    return cursor.size() - 1;
  }

  @Override
  public Optional<Object> read() {
    skipWhitespace();
    return scan();
  }

  @Override
  public Optional<Object> readSymbol() {
    skipWhitespace();
    return scanSymbol();
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
