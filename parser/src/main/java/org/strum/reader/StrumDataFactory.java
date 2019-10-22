package org.strum.reader;

public interface StrumDataFactory {
  StrumBuilder cons(StrumData car, StrumData cdr);

  StrumBuilder symbol(String namespace, String name);
}
