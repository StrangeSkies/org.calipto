package org.calipto.reader.scanning;

import static java.lang.Character.codePointOf;
import static org.calipto.type.symbol.Symbols.SYSTEM_NAMESPACE;

import java.util.List;
import java.util.Optional;

import org.calipto.reader.Reader;
import org.calipto.scanner.Scanner;
import org.calipto.type.DataLibrary;
import org.calipto.type.symbol.Symbols;

public class ScanningReader implements Reader {
  private final DataLibrary data;
  private final Symbols symbols;
  private final Scanner scanner;

  private List<Long> cursor;

  public ScanningReader(Symbols symbols, Scanner scanner) {
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
    if (scanStepIn()) {
      skipWhitespace();

      return Optional.of(scanStepOut());
    }
    return Optional.empty();
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

  private Optional<Object> scan() {
    return scanSymbol().or(() -> scanList());
  }

  @Override
  public Optional<Object> readSymbol() {
    skipWhitespace();
    return scanSymbol();
  }

  @Override
  public boolean readStepIn() {
    skipWhitespace();
    return scanStepIn();
  }

  private boolean scanStepIn() {
    return scanner.advanceInputIf(c -> c == codePointOf("("));
  }

  @Override
  public Optional<Object> readStepOut() {
    skipWhitespace();
    return scanStepOut();
  }

  private Optional<Object> scanStepOut() {
    if (cursorDepth() <= 0) {
      return Optional.empty();
    }

    if (scanner.advanceInputIf(c -> c == codePointOf(")"))) {
      scanner.discardBuffer();

      return Optional.of(Symbols.NIL);
    }

    Object head = scanNext();

    skipWhitespace();
    Object tail;
    if (scanner.advanceInputIf(c -> c == codePointOf("."))) {
      skipWhitespace();
      tail = scanNext();

    } else {
      tail = scanStepOut();
    }

    return Optional.of(cons(head, tail));
  }
}
