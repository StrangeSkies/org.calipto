package org.calipto.node.intrinsic;

import org.calipto.type.DataLibrary;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo(shortName = "car")
public abstract class CarNode extends IntrinsicNode {
  @Specialization(guards = "conses.isCons(cons)", limit = "3")
  Object doDefault(Object cons, @CachedLibrary("cons") DataLibrary conses) {
    return conses.car(cons);
  }
}