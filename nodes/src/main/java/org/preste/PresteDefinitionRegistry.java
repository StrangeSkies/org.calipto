package org.preste;

import java.util.Map;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.interop.TruffleObject;

public class PresteDefinitionRegistry {
  private final PresteLanguage language;
  private final DefinitionsObject functionsObject = new DefinitionsObject();

  public PresteDefinitionRegistry(PresteLanguage language) {
    this.language = language;
  }

  /**
   * Returns the canonical {@link SLFunction} object for the given name. If it
   * does not exist yet, it is created.
   */
  public Object lookup(String name) {
    return functionsObject.objects.get(name);
  }

  /**
   * Associates the {@link SLFunction} with the given name with the given
   * implementation root node. If the function did not exist before, it defines
   * the function. If the function existed before, it redefines the function and
   * the old implementation is discarded.
   */
  public Object register(String name, RootCallTarget callTarget) {
    PresteFunction function = (PresteFunction) lookup(name);
    function.setCallTarget(callTarget);
    return function;
  }

  public void register(Map<String, RootCallTarget> newFunctions) {
    for (Map.Entry<String, RootCallTarget> entry : newFunctions.entrySet()) {
      register(entry.getKey(), entry.getValue());
    }
  }

  public PresteFunction getFunction(String name) {
    return (PresteFunction) functionsObject.objects.get(name);
  }

  public TruffleObject getFunctionsObject() {
    return functionsObject;
  }
}
