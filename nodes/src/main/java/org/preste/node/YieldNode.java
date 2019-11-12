package org.preste.node;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;

/**
 * Yield control from the current continuation to an enclosing scope to perform
 * some side effect.
 */
@GenerateNodeFactory
public abstract class YieldNode extends PresteNode {}
