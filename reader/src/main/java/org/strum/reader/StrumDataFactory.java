package org.strum.reader;

public interface StrumDataFactory {
  StrumData cons(Object car, Object cdr);

  StrumData symbol(String namespace, String name);
}
