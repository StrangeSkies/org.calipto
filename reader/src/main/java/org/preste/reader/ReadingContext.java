package org.preste.reader;

public interface ReadingContext {
  PresteData makeCons(Object car, Object cdr);

  PresteData makeSymbol(String namespace, String name);

  ReaderMacro resolveReaderMacro(PresteData symbol);
}
