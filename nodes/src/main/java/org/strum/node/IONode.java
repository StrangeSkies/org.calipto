package org.strum.node;

import org.strum.type.Symbol;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;

/**
 * A node for built-in side effects. This represents the enclosing context which
 * can be yielded to in order to perform some platform-provided side effect.
 */
@NodeChild(value = "arguments", type = StrumNode[].class)
@GenerateNodeFactory
public class IONode extends StrumNode {
  private final Symbol symbol;

  public IONode(Symbol symbol) {
    this.symbol = symbol;
  }
}
