package org.preste;

import java.util.Collection;
import java.util.Optional;

import org.preste.compiler.EvaluationContext;
import org.preste.compiler.PresteExpression;

public class EvaluationContextImpl implements EvaluationContext {
  @Override
  public PresteExpression makeInvoke(
      PresteExpression target,
      Collection<? extends PresteExpression> arguments) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PresteExpression makeQuote(Object data) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PresteExpression makeResolve(String namespace, String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isSymbol(Object data) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Object getCar(Object data) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object getCdr(Object data) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getNamespace(Object data) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getName(Object data) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Optional<PresteExpression> getMacro(String namespace, String name) {
    // TODO Auto-generated method stub
    return null;
  }
}
