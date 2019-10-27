package org.strum.compiler;

import static java.util.stream.Collectors.toList;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StrumEvaluator {
  private final Deque<List<StrumNode>> compilerStack = new ArrayDeque<>();

  private final EvaluationContext context;

  public StrumEvaluator(EvaluationContext context) {
    this.context = context;
  }

  public Object eval(Object expression) {
    compile(expression).eval();
  }

  StrumNode compile(Object expression) {
    StrumNode node;

    if (context.isSymbol(expression)) {
      node = compileResolution(context.getNamespace(expression), context.getName(expression));
    } else {
      node = compileList(context.getCar(expression), context.getCdr(expression));
    }

    return node;
  }

  StrumNode compileList(Object car, Object cdr) {
    List<StrumNode> list = new ArrayList<>();
    compilerStack.push(list);

    if (context.isSymbol(car)) {
      String namespace = context.getNamespace(car);
      String name = context.getName(car);

      context
          .getMacro(namespace, name)
          .map(macro -> compileMacro(macro, cdr))
          .orElseGet(() -> compileInvocation(compileResolution(namespace, name), cdr));
    } else {
      compileInvocation(compile(car), cdr);
    }
  }

  StrumNode compileInvocation(StrumNode target, Object cdr) {
    getArguments(cdr).forEach(this::compile);

    var invoke = context.makeInvoke(target, compilerStack.pop());
    addNode(invoke);

    return invoke;
  }

  void compileMacro(StrumNode node, Object arguments) {
    context
        .makeInvoke(node, getArguments(arguments).map(context::makeQuote).collect(toList()))
        .eval();
  }

  private Stream<Object> getArguments(Object tail) {
    List<Object> list = new ArrayList<>();

    while (!context.isSymbol(tail)) {
      Object head = context.getCar(tail);
      tail = context.getCdr(tail);

      list.add(head);
    }

    String namespace = context.getNamespace(tail);
    String name = context.getNamespace(tail);
    if (!isNil(namespace, name)) {
      throw new StrumEvaluatorException("Expression is not well formed, expected proper list");
    }

    return list.stream();
  }

  private boolean isNil(String namespace, String name) {
    // TODO Auto-generated method stub
    return false;
  }

  StrumNode compileResolution(String namespace, String name) {
    StrumNode resolve = context.makeResolve(namespace, name);
    addNode(resolve);

    return resolve;
  }

  private void addNode(StrumNode node) {
    if (!compilerStack.isEmpty()) {
      compilerStack.peek().add(node);
    }
  }
}
