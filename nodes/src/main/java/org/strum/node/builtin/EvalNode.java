package org.strum.node.builtin;

import org.strum.compiler.EvaluationContext;
import org.strum.compiler.StrumEvaluator;
import org.strum.node.intrinsic.IntrinsicNode;
import org.strum.type.ConsLibrary;
import org.strum.type.SymbolLibrary;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo(shortName = "eval")
public abstract class EvalNode extends IntrinsicNode {
  private final StrumEvaluator evaluator;

  public EvalNode(EvaluationContext context) {
    this.evaluator = new StrumEvaluator(context);
  }

  @Specialization(guards = "conses.isCons(cons)", limit = "3")
  Object doDefault(Object cons, @CachedLibrary("cons") ConsLibrary conses) {
    return evaluator.eval(cons);
  }

  @Specialization
  Object doDefault(SymbolLibrary symbol) {
    return evaluator.eval(symbol);
  }
}
