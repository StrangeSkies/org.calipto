package org.calipto.node.builtin;

import org.calipto.CaliptoTypeException;
import org.calipto.node.intrinsic.IntrinsicNode;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo(shortName = "input")
public abstract class StandardInputNode extends IntrinsicNode {
  @Specialization(guards = "interop.isNumber(maximumSize)", limit = "3")
  Object doDefault(Object maximumSize, @CachedLibrary("maximumSize") InteropLibrary interop) {
    try {
      return read(interop.asLong(maximumSize));
    } catch (Exception e) {
      throw new CaliptoTypeException(this, maximumSize);
    }
  }

  @Specialization
  Object doDefault(int maximumSize) {
    return read(maximumSize);
  }

  Object read(long maximumSize) {
    return null;
  }
}
