package org.calipto.reader;

public interface ReadingContext {
  CaliptoData makeCons(Object car, Object cdr);

  CaliptoData makeSymbol(String namespace, String name);

  ReaderMacro resolveReaderMacro(CaliptoData symbol);
}
