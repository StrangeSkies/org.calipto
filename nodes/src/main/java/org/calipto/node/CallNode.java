package org.calipto.node;

import java.util.List;

import org.calipto.CaliptoTypeException;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo(shortName = "call")
public final class CallNode extends CaliptoNode {
  @Child
  private CaliptoNode targetNode;
  @Children
  private final CaliptoNode[] argumentNodes;
  @Child
  private InteropLibrary library;

  public CallNode(List<CaliptoNode> childNodes) {
    this(childNodes.get(0), childNodes.subList(1, childNodes.size()));
  }

  public CallNode(CaliptoNode targetNode, List<CaliptoNode> argumentNodes) {
    this.targetNode = targetNode;
    this.argumentNodes = argumentNodes.toArray(CaliptoNode[]::new);
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

    var handlers = InvokeHandlerNode.HANDLERS.get();

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
    
    
    TODO we can specialise for a handler per thread. Currently a new handler
    context is run in a new thread so this works fine. If we use continuations
    in the future we will need to invalidate these assumptions wherever the
    handler is shadowed.  
    
    
    */

    try {
      return library.execute(handler, argumentValues);
    } catch (ArityException | UnsupportedTypeException
        | UnsupportedMessageException e) {
      throw new CaliptoTypeException(this, argumentValues);
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
