package org.preste.node.intrinsic;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo(shortName = "cdr")
public abstract class CdrNode extends IntrinsicNode {
  @Specialization(guards = "conses.isCons(cons)", limit = "3")
  Object doDefault(Object cons, @CachedLibrary("cons") ConsLibrary conses) {
    return conses.cdr(cons);
  }
}