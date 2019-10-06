package org.strum.node;

import org.strum.type.ConsLibrary;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;

@NodeChild
abstract class CdrNode extends StrumNode {
  @Specialization(guards = "conses.isCons(cons)", limit = "3")
  Object doDefault(Object cons, @CachedLibrary("cons") ConsLibrary conses) {
    return conses.cdr(cons);
  }
}