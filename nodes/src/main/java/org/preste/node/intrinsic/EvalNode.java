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
public abstract class EvalNode extends IntrinsicNode {
  protected abstract FrameSlot getSlot();

  private final ConsLibrary consLibrary = ConsLibrary.getFactory().getUncached();
  private final SymbolLibrary symbolLibrary = ConsLibrary.getFactory().getUncached();

  public EvalNode() {
    // TODO Auto-generated constructor stub
  }

  @Specialization
  Object doDefault(VirtualFrame frame, Object value) {

    var value = conses.car(cons);

    var macros = frame.getFrameDescriptor().findFrameSlot((Symbol) "macros");

    if (setContains(macros, value)) {
      frame.getFrameDescriptor().findFrameSlot(value);
    }

    return FrameUtil.getObjectSafe(frame, getSlot());
  }
}
