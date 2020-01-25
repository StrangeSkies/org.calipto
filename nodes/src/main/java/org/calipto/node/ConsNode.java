package org.calipto.node;

import org.calipto.type.DataLibrary;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo(shortName = "cons")
@NodeChild("car")
@NodeChild("cdr")
public abstract class ConsNode extends CaliptoNode {
  @Specialization(limit = "3", guards = "cons != null")
  Object doConsOnto(
      Object car,
      Object cdr,
      @CachedLibrary("car") DataLibrary cars,
      @Cached("cars.consOnto(car, cdr)") Object cons) {
    return cons;
  }

  @Specialization(replaces = "doConsOnto", limit = "3")
  Object doConsWith(Object car, Object cdr, @CachedLibrary("cdr") DataLibrary cdrs) {
    return cdrs.consWith(cdr, car);
  }
}
