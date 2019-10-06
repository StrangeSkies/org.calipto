package org.strum.reader;

public interface StrumFactory {
  Object cons(Object car, Object cdr);

  Object symbol(String namespace, String name);

  Object symbol(String name);

  Object nil();
}
