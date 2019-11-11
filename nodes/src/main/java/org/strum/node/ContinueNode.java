package org.strum.node;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;

/**
 * Pass control back from an enclosing side-effecting scope to whichever
 * continuation it was yielded from.
 */
@GenerateNodeFactory
public abstract class ContinueNode extends StrumNode {}
