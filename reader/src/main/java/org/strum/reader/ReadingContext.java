package org.strum.reader;

public interface ReadingContext {
  StrumData makeCons(Object car, Object cdr);

  StrumData makeSymbol(String namespace, String name);
}
