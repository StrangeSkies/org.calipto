package org.calipto;

import static org.calipto.type.symbol.AtomSymbol.ATOM;
import static org.calipto.type.symbol.CarSymbol.CAR;
import static org.calipto.type.symbol.CdrSymbol.CDR;
import static org.calipto.type.symbol.ConsSymbol.CONS;
import static org.calipto.type.symbol.EqSymbol.EQ;
import static org.calipto.type.symbol.HandleSymbol.HANDLE;
import static org.calipto.type.symbol.NilSymbol.NIL;
import static org.calipto.type.symbol.QuoteSymbol.QUOTE;

import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.calipto.node.AtomNodeGen;
import org.calipto.node.CaliptoNode;
import org.calipto.node.CarNodeGen;
import org.calipto.node.CdrNodeGen;
import org.calipto.node.ConsNodeGen;
import org.calipto.node.EqNodeGen;
import org.calipto.node.QuoteNode;
import org.calipto.type.DataLibrary;

public class CaliptoCompiler {
  private final DataLibrary data;

  private final Map<Object, Function<Object, CaliptoNode>> builtins;
  private final Map<Object, Function<Object, CaliptoNode>> intrinsics;

  public CaliptoCompiler(/* TODO pass in intrinsics */) {
    this.data = DataLibrary.getFactory().getUncached();
    this.builtins = Map
        .of(
            ATOM,
            this::atom,
            CAR,
            this::car,
            CDR,
            this::cdr,
            CONS,
            this::cons,
            EQ,
            this::eq,
            HANDLE,
            this::handler,
            NIL,
            this::nil,
            QUOTE,
            this::quote);
    this.intrinsics = Map.of();
  }

  public CaliptoNode compile(Object input) {
    if (!data.isCons(input)) {
      return invalidSyntax(input, "Unexpected symbol");
    }

    var car = data.car(input);
    var cdr = data.cdr(input);

    if (!data.isSymbol(car)) {
      return invalidSyntax(input, "Unrecognised syntax form");
    }

    return builtins
        .getOrDefault(car, builtin -> invalidSyntax(builtin, "Unrecognised syntax form"))
        .apply(cdr);
  }

  private CaliptoNode invalidSyntax(Object input, String message) {
    // TODO Auto-generated method stub
    return null;
  }

  private CaliptoNode unaryNode(Object input, UnaryOperator<CaliptoNode> factory) {
    var arg1 = data.car(input);
    var tail = data.cdr(input);
    if (!data.equals(tail, NIL)) {
      return invalidSyntax(input, "Argument list improperly terminates");
    }
    return factory.apply(compile(arg1));
  }

  private CaliptoNode binaryNode(Object input, BinaryOperator<CaliptoNode> factory) {
    var arg1 = data.car(input);
    var tail = data.cdr(input);
    return unaryNode(tail, arg2 -> factory.apply(compile(arg1), arg2));
  }

  private CaliptoNode atom(Object input) {
    return unaryNode(input, AtomNodeGen::create);
  }

  private CaliptoNode car(Object input) {
    return unaryNode(input, CarNodeGen::create);
  }

  private CaliptoNode cdr(Object input) {
    return unaryNode(input, CdrNodeGen::create);
  }

  private CaliptoNode cons(Object input) {
    return binaryNode(input, ConsNodeGen::create);
  }

  private CaliptoNode eq(Object input) {
    return binaryNode(input, EqNodeGen::create);
  }

  private CaliptoNode handler(Object input) {
    // TODO Auto-generated method stub
    return null;
  }

  private CaliptoNode nil(Object input) {
    // TODO Auto-generated method stub
    return null;
  }

  private CaliptoNode quote(Object input) {
    return unaryNode(input, QuoteNode::new);
  }
}
