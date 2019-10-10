package org.strum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.source.Source;

public class StrumDefinitionRegistry {
  private final StrumLanguage language;
  private final DefinitionsObject functionsObject = new DefinitionsObject();

  public StrumDefinitionRegistry(StrumLanguage language) {
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
    SLFunction function = lookup(name, true);
    function.setCallTarget(callTarget);
    return function;
  }

  public void register(Map<String, RootCallTarget> newFunctions) {
    for (Map.Entry<String, RootCallTarget> entry : newFunctions.entrySet()) {
      register(entry.getKey(), entry.getValue());
    }
  }

  public void register(Source newFunctions) {
    register(SimpleLanguageParser.parseSL(language, newFunctions));
  }

  public SLFunction getFunction(String name) {
    return functionsObject.functions.get(name);
  }

  /**
   * Returns the sorted list of all functions, for printing purposes only.
   */
  public List<SLFunction> getFunctions() {
    List<SLFunction> result = new ArrayList<>(functionsObject.functions.values());
    Collections.sort(result, new Comparator<SLFunction>() {
      public int compare(SLFunction f1, SLFunction f2) {
        return f1.toString().compareTo(f2.toString());
      }
    });
    return result;
  }

  public TruffleObject getFunctionsObject() {
    return functionsObject;
  }

}
