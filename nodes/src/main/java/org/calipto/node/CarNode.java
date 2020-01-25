package org.calipto.node;

import org.calipto.type.DataLibrary;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo(shortName = "car")
@NodeChild("cons")
public abstract class CarNode extends CaliptoNode {
  @Specialization(guards = "conses.isCons(cons)", limit = "3")
  Object doDefault(Object cons, @CachedLibrary("cons") DataLibrary conses) {
    return conses.car(cons);
  }
}