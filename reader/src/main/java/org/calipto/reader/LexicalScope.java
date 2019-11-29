package org.calipto.reader;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class LexicalScope {
  private Map<Integer, ReaderMacro> characterMacros;
  private Map<CaliptoData, ReaderMacro> namedMacros;

  private final LexicalScope parent;

  private LexicalScope(LexicalScope parent) {
    this.parent = requireNonNull(parent);
  }

  public LexicalScope() {
    this.parent = null;
  }

  public LexicalScope createChild() {
    return new LexicalScope(this);
  }

  public LexicalScope getParent() {
    return Objects.requireNonNull(parent);
  }

  public Optional<ReaderMacro> findMacro(Integer codePoint) {
    if (characterMacros != null) {
      var macro = characterMacros.get(codePoint);
      if (macro != null) {
        return Optional.of(macro);
      }
    }
    if (parent != null) {
      return parent.findMacro(codePoint);
    }
    return Optional.empty();
  }

  public void setMacro(Integer codePoint, ReaderMacro macro) {
    if (characterMacros == null) {
      characterMacros = new HashMap<>();
    }
    characterMacros.put(codePoint, macro);
  }

  public Optional<ReaderMacro> findMacro(CaliptoData symbol) {
    if (namedMacros != null) {
      var macro = namedMacros.get(symbol);
      if (macro != null) {
        return Optional.of(macro);
      }
    }
    if (parent != null) {
      return parent.findMacro(symbol);
    }
    return Optional.empty();
  }

  public void setMacro(CaliptoData symbol, ReaderMacro macro) {
    if (namedMacros == null) {
      namedMacros = new HashMap<>();
    }
    namedMacros.put(symbol, macro);
  }
}
