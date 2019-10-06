package org.strum.reader;

public interface StrumFactory {
  StrumBuilder cons(StrumExpression car, StrumExpression cdr);

  StrumBuilder symbol(String namespace, String name);

  StrumBuilder symbol(String name);

  StrumBuilder nil();
}
