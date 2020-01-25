package org.calipto.node;

import org.calipto.type.DataLibrary;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo(shortName = "cons")
@NodeChild("car")
@NodeChild("cdr")
public abstract class ConsNode extends CaliptoNode {
  @Specialization(limit = "3")
  Object doDefault(
      Object car,
      Object cdr,
      @CachedLibrary("car") DataLibrary cars,
      @CachedLibrary("cdr") DataLibrary cdrs) {
    var cons = cars.consOnto(car, cdr);
    if (cons == null) {
      cons = cdrs.consWith(cdr, car);
    }
    return cons;
  }
}
