package org.preste.node.intrinsic;

import org.preste.type.cons.ConsLibrary;
import org.preste.type.symbol.Symbol;
import org.preste.type.symbol.SymbolLibrary;

import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameUtil;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeField(name = "slot", type = FrameSlot.class)
@NodeInfo(shortName = "expand")
public abstract class ExpandNode extends IntrinsicNode {
  protected abstract FrameSlot getSlot();

  @Specialization(guards = "conses.isCons(cons)", limit = "3")
  Object doDefault(VirtualFrame frame, Object cons, @CachedLibrary("cons") ConsLibrary conses) {
    var value = conses.car(cons);

    var macros = frame.getFrameDescriptor().findFrameSlot((Symbol) "macros");

    if (setContains(macros, value)) {
      frame.getFrameDescriptor().findFrameSlot(value);
    }

    return FrameUtil.getObjectSafe(frame, getSlot());
  }

  @Specialization(guards = "symbols.isSymbol(symbol)", limit = "3")
  Object doDefault(Object symbol, @CachedLibrary("symbol") SymbolLibrary symbols) {
    return symbol;
  }
}
