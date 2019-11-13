package org.preste.compiler;

import java.util.Collection;
import java.util.Optional;

public interface EvaluationContext {
  PresteExpression makeInvoke(PresteExpression target, Collection<? extends PresteExpression> arguments);

  PresteExpression makeQuote(Object data);

  PresteExpression makeResolve(String namespace, String name);

  boolean isSymbol(Object data);

  Object getCar(Object data);

  Object getCdr(Object data);

  String getNamespace(Object data);

  String getName(Object data);

  Optional<Object> getDefine(String namespace, String name);
}
