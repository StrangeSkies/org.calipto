package org.preste.node;

import org.preste.PresteTypeException;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node.Child;
import com.oracle.truffle.api.nodes.Node.Children;

/**
 * Yield control from the current continuation to an enclosing scope to perform
 * some side effect.
 */
@GenerateNodeFactory
public abstract class YieldNode extends PresteNode {
  @Children
  private final PresteNode[] argumentNodes;
  @Child
  private InteropLibrary library;

  public YieldNode(PresteNode[] argumentNodes) {
    this.argumentNodes = argumentNodes;
    this.library = InteropLibrary.getFactory().createDispatched(3);
  }

  @ExplodeLoop
  @Override
  public Object executeGeneric(VirtualFrame frame) {
    CompilerAsserts.compilationConstant(argumentNodes.length);

    Object[] argumentValues = new Object[argumentNodes.length];
    for (int i = 0; i < argumentNodes.length; i++) {
      argumentValues[i] = argumentNodes[i].executeGeneric(frame);
    }

    var handlers = frame.getObject(frame.getFrameDescriptor().findFrameSlot("(handlers)"));

    /*
     * TODO
     * 
     * var handlerCandidates = handlers.findCandidates(staticTypes(argumentNodes)); // TODO cached!
     * var handler = handlerCandidates.getFirst(argumentValues);
     * 
     */
    
    var handler = handlers.find(argumentValues);
    
    /*-
    var function = handler.getInlineableEffect();
    if (function != null) {
      var result = library.execute(function, argumentValues);
      if (!continue(result)) {
        throw new TerminateContinuationException(); // control flow exception
      }
    } else {
      handler.awaitResult(argumentValues); // also may throw control flow exception
    }
    */

    try {
      return library.execute(handler, argumentValues);
    } catch (ArityException | UnsupportedTypeException | UnsupportedMessageException e) {
      throw new PresteTypeException(this, argumentValues);
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
