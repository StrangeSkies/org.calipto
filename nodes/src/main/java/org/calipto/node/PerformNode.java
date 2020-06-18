package org.calipto.node;

import org.calipto.type.DataLibrary;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;

/**
 * Yield control from the current continuation to an enclosing scope to perform
 * some side effect.
 */
@NodeInfo(shortName = "perform")
@NodeChild("effect")
public abstract class PerformNode extends CaliptoNode {
  @Specialization(limit = "3", guards = "effects.isSymbol(effect)")
  Object doDefault(
      Object effect,
      @CachedLibrary("effect") DataLibrary effects) {
    var handlers = InvokeHandlerNode.HANDLERS.get();

    var handler = handlers.find(effect);
  }
}
