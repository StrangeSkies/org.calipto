package org.strum.compiler;

import org.strum.reader.StrumData;

public interface StrumNodeFactory {
  StrumNode compile(StrumData data);
}
