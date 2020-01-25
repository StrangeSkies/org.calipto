package org.calipto.node;

import org.calipto.type.DataLibrary;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo(shortName = "car")
@NodeChild("left")
@NodeChild("right")
public abstract class EqNode extends CaliptoNode {
  @Specialization(limit = "3")
  Object doConsOnto(Object left, Object right, @CachedLibrary("left") DataLibrary data) {
    return data.equals(left, right);
  }
}
