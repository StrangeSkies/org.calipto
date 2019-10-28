package org.strum;

import java.util.Collection;
import java.util.Optional;

import org.strum.compiler.EvaluationContext;
import org.strum.compiler.StrumNode;

public class EvaluationContextImpl implements EvaluationContext {

  @Override
  public StrumNode makeInvoke(StrumNode target, Collection<? extends StrumNode> arguments) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public StrumNode makeQuote(Object data) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public StrumNode makeResolve(String namespace, String name) {
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
  public Optional<StrumNode> getMacro(String namespace, String name) {
    // TODO Auto-generated method stub
    return null;
  }

}
