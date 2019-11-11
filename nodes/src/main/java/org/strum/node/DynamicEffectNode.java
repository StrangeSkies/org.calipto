package org.strum.node;

import org.strum.StrumTypeException;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.nodes.ExplodeLoop;

/**
 * A node for built-in side effects. This represents the enclosing context which
 * can be yielded to in order to perform some platform-provided side effect.
 */
@NodeChild(value = "arguments", type = StrumNode[].class)
@GenerateNodeFactory
public class DynamicEffectNode extends StrumNode {
  @Child
  private StrumNode targetNode;
  @Children
  private final StrumNode[] argumentNodes;
  @Child
  private InteropLibrary library;

  public DynamicEffectNode(StrumNode targetNode, StrumNode[] argumentNodes) {
    this.targetNode = targetNode;
    this.argumentNodes = argumentNodes;
    this.library = InteropLibrary.getFactory().createDispatched(3);
  }

  @ExplodeLoop
  @Override
  public Object executeGeneric(VirtualFrame frame) {
    Object effect = targetNode.executeGeneric(frame);

    /*
     * "effect" should resolve to the effect descriptor object, which holds the name
     * of the effect, the argument types, and the return type of yielding to the
     * effect.
     */

    CompilerAsserts.compilationConstant(argumentNodes.length);

    Object[] argumentValues = new Object[argumentNodes.length];
    for (int i = 0; i < argumentNodes.length; i++) {
      argumentValues[i] = argumentNodes[i].executeGeneric(frame);
    }

    try {
      
    } catch (ArityException | UnsupportedTypeException | UnsupportedMessageException e) {
      throw new StrumTypeException(targetNode, argumentValues);
    }
  }

  @Override
  public boolean hasTag(Class<? extends Tag> tag) {
    if (tag == StandardTags.CallTag.class) {
      return true;
    }
    return super.hasTag(tag);
  }
}
