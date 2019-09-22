package org.strum.type;

import java.util.Objects;

// TODO value type
public final class Symbol {
  private final Namespace namespace;
  private final Name name;

  public Symbol(Namespace namespace, Name name) {
    this.namespace = namespace;
    this.name = name;
  }

  public Namespace namespace() {
    return namespace;
  }

  public Name name() {
    return name;
  }

  @Override
  public String toString() {
    return namespace + "/" + name;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || obj.getClass() != getClass()) {
      return false;
    }

    Symbol that = (Symbol) obj;

    return Objects.equals(this.name, that.name) && Objects.equals(this.namespace, that.namespace);
  }
}
