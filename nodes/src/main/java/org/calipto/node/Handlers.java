package org.calipto.node;

import java.util.List;

public class Handlers {
  private final List<EffectMediator> performerMediators;
  private final List<EffectMediator> handlerMediators;

  public Handlers() {
    this.performerMediators = List.of();
    this.handlerMediators = List.of();
  }

  private Handlers(Handlers parent) {
    this.parent = parent;
  }

  public Object find(Object[] argumentValues) {
    // TODO Auto-generated method stub
    return null;
  }

  public Handlers withPerformerMediator(EffectMediator mediator) {
    // TODO Auto-generated method stub
    return null;
  }

  public Handlers withHandlerMediator(EffectMediator mediator) {
    // TODO Auto-generated method stub
    return null;
  }
}
