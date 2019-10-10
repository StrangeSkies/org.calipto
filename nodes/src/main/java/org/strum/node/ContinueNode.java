package org.strum.node;

import org.strum.type.Symbol;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeChild;

/**
 * Pass control back from an enclosing side-effecting scope to whichever
 * continuation it was yielded from.
 */
@GenerateNodeFactory
public class ContinueNode extends StrumNode {}
