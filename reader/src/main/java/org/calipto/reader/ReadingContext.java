package org.calipto.reader;

import java.util.Optional;

public interface ReadingContext {
  CaliptoData makeCons(Object car, Object cdr);

  CaliptoData makeSymbol(String namespace, String name);

  ReaderMacro resolveReaderMacro(CaliptoData symbol);

  Optional<ReaderMacro> findCharacterMacro(int codePoint);
}
