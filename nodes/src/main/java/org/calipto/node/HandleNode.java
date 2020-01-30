package org.calipto.node;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;

/**
 * Yield control from the current continuation to an enclosing scope to perform
 * some side effect.
 */
@NodeInfo(shortName = "perform")
@NodeChild("effect")
public abstract class HandleNode extends CaliptoNode {
  @Specialization(limit = "3")
  Object doDefault(Object handler) {
    return null;
  }
}
