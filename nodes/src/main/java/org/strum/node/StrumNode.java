package org.strum.node;

import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;

@TypeSystemReference(StrumTypes.class)
@NodeInfo(language = "Strum Language", description = "The abstract base node for all expressions")
public abstract class StrumNode extends Node {}
}
