package org.preste.node.builtin;

import org.preste.compiler.EvaluationContext;
import org.preste.compiler.PresteEvaluator;
import org.preste.node.intrinsic.IntrinsicNode;
import org.preste.type.cons.ConsLibrary;
import org.preste.type.symbol.SymbolLibrary;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo(shortName = "eval")
public abstract class EvalNode extends IntrinsicNode {
  private final PresteEvaluator evaluator;

  public EvalNode(EvaluationContext context) {
    this.evaluator = new PresteEvaluator(context);
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
