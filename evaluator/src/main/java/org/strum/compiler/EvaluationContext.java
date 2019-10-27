package org.strum.compiler;

import java.util.Collection;
import java.util.Optional;

public interface EvaluationContext {
  StrumNode makeInvoke(StrumNode target, Collection<? extends StrumNode> arguments);

  StrumNode makeQuote(Object data);

  StrumNode makeResolve(String namespace, String name);

  boolean isSymbol(Object data);

  Object getCar(Object data);

  Object getCdr(Object data);

  String getNamespace(Object data);

  String getName(Object data);

  Optional<StrumNode> getMacro(String namespace, String name);
}
