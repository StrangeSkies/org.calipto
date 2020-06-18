package org.calipto.node;

import org.calipto.type.DataLibrary;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo(shortName = "atom")
@NodeChild("data")
public abstract class AtomNode extends CaliptoNode {
  @Specialization(limit = "3")
  Object doDefault(Object item, @CachedLibrary("item") DataLibrary data) {
    return data.isSymbol(item);
  }
}
