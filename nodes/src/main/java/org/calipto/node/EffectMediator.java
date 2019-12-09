package org.calipto.node;

public class EffectMediator {
  public boolean perform(Object effect) {
    // if we can handle, do so and return true, else return false.
  }

  public void handle(Handler handler) {
    // submit handler and wait until it receives an effect it can handle
  }
}
