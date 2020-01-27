package org.calipto;

import static org.calipto.type.symbol.Symbols.ATOM;
import static org.calipto.type.symbol.Symbols.CALL;
import static org.calipto.type.symbol.Symbols.CAR;
import static org.calipto.type.symbol.Symbols.CDR;
import static org.calipto.type.symbol.Symbols.CONS;
import static org.calipto.type.symbol.Symbols.EQ;
import static org.calipto.type.symbol.Symbols.HANDLE;
import static org.calipto.type.symbol.Symbols.NIL;
import static org.calipto.type.symbol.Symbols.PERFORM;
import static org.calipto.type.symbol.Symbols.QUOTE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.calipto.node.AtomNodeGen;
import org.calipto.node.CaliptoNode;
import org.calipto.node.CallNodeGen;
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

  public CaliptoCompiler(Map<Object, Function<Object, CaliptoNode>> intrinsics) {
    this.data = DataLibrary.getFactory().getUncached();
    this.builtins = Map
        .of(
            ATOM,
            this::atom,
            CALL,
            this::call,
            CAR,
            this::car,
            CDR,
            this::cdr,
            CONS,
            this::cons,
            EQ,
            this::eq,
            HANDLE,
            this::handle,
            NIL,
            this::nil,
            PERFORM,
            this::perform,
            QUOTE,
            this::quote);
    this.intrinsics = Map.copyOf(intrinsics);
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

    return Optional
        .ofNullable(builtins.get(car))
        .or(() -> Optional.ofNullable(intrinsics.get(car)))
        .map(f -> f.apply(cdr))
        .orElseGet(() -> invalidSyntax(input, "Unrecognised syntax form"));
  }

  private CaliptoNode invalidSyntax(Object input, String message) {
    // TODO Auto-generated method stub
    return null;
  }

  private CaliptoNode incorrectArgumentCount(Object input) {
    return invalidSyntax(input, "Argument list size incorrect");
  }

  private CaliptoNode unaryNode(Object input, UnaryOperator<CaliptoNode> factory) {
    return naryNode(input, args -> {
      if (args.size() != 1) {
        return incorrectArgumentCount(input);
      }
      return factory.apply(args.get(0));
    });
  }

  private CaliptoNode binaryNode(Object input, BinaryOperator<CaliptoNode> factory) {
    return naryNode(input, args -> {
      if (args.size() != 2) {
        return incorrectArgumentCount(input);
      }
      return factory.apply(args.get(0), args.get(1));
    });
  }

  private CaliptoNode naryNode(Object input, Function<List<CaliptoNode>, CaliptoNode> factory) {
    var args = new ArrayList<CaliptoNode>();
    var tail = input;
    while (data.isCons(tail)) {
      args.add(compile(data.car(input)));
      tail = data.cdr(input);
    }
    if (!data.equals(tail, NIL)) {
      return invalidSyntax(input, "Argument list improperly terminates");
    }
    return factory.apply(args);
  }

  private CaliptoNode atom(Object input) {
    return unaryNode(input, AtomNodeGen::create);
  }

  private CaliptoNode call(Object input) {
    return unaryNode(input, CallNodeGen::create);
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

  private CaliptoNode handle(Object input) {
    // TODO Auto-generated method stub
    return null;
  }

  private CaliptoNode nil(Object input) {
    // TODO Auto-generated method stub
    return null;
  }

  private CaliptoNode perform(Object input) {
    return naryNode(input, PerformNodeGen::create);
  }

  private CaliptoNode quote(Object input) {
    return unaryNode(input, QuoteNode::new);
  }
}
