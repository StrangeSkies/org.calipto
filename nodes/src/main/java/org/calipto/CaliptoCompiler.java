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
            null,
            CAR,
            null,
            CDR,
            null,
            CONS,
            null,
            EQ,
            null,
            HANDLER,
            null,
            NIL,
            null,
            QUOTE,
            null);
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
}
