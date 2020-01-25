package org.calipto;

import static org.calipto.type.symbol.AtomSymbol.ATOM;
import static org.calipto.type.symbol.CarSymbol.CAR;
import static org.calipto.type.symbol.CdrSymbol.CDR;
import static org.calipto.type.symbol.ConsSymbol.CONS;
import static org.calipto.type.symbol.EqSymbol.EQ;
import static org.calipto.type.symbol.HandlerSymbol.HANDLER;
import static org.calipto.type.symbol.NilSymbol.NIL;
import static org.calipto.type.symbol.QuoteSymbol.QUOTE;

import java.util.Map;
import java.util.function.Function;

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
            HANDLER,
            this::handler,
            NIL,
            this::nil,
            QUOTE,
            this::quote);
    this.intrinsics = Map.of();
  }

  public CaliptoNode compile(Object input) {
    if (!data.isCons(input)) {
      return invalidSyntax(input);
    }

    var car = data.car(input);
    var cdr = data.cdr(input);

    if (data.isSymbol(car)) {
      builtins.getOrDefault(car, this::invalidSyntax).apply(cdr);

    } else {
      compile(car);

    }
  }

  private CaliptoNode invalidSyntax(Object input) {
    // TODO Auto-generated method stub
    return null;
  }

  private CaliptoNode atom(Object input) {
    // TODO Auto-generated method stub
    return null;
  }

  private CaliptoNode car(Object input) {
    return CarNodeGen.create(compile(data.car(input)));
  }

  private CaliptoNode cdr(Object input) {
    return CdrNodeGen.create(compile(data.car(input)));
  }

  private CaliptoNode cons(Object input) {
    return ConsNodeGen.create(compile(data.car(input)), compile(data.car(data.cdr(input))));
  }

  private CaliptoNode eq(Object input) {
    return EqNodeGen.create(compile(data.car(input)), compile(data.car(data.cdr(input))));
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
    return new QuoteNode(input);
  }
}
