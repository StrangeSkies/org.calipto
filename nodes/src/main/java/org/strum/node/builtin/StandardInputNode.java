package org.strum.node.builtin;

import org.strum.StrumLanguage;
import org.strum.node.intrinsic.IntrinsicNode;
import org.strum.type.ConsLibrary;
import org.strum.type.Symbol;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo(shortName = "input")
public abstract class StandardInputNode extends IntrinsicNode {
  private final StrumLanguage language;

  public StandardInputNode(StrumLanguage language) {
    this.language = language;
  }

  @Specialization(guards = "interop.isInt(cons)", limit = "3")
  Object doDefault(Object maximumSize, @CachedLibrary("maximumSize") InteropLibrary interop) {
    int size = interop.asInt(maximumSize);

    return ;
  }

  @Specialization
  Object doDefault(int maximumSize) {
    return evaluator.eval(symbol);
  }
}
