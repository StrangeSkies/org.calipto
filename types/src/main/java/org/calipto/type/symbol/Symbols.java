package org.calipto.type.symbol;

public class Symbols {
  public static String SYSTEM_NAMESPACE = "";

  public static final Object ATOM = new AtomSymbol();
  public static final Object CALL = new CallSymbol();
  public static final Object CAR = new CarSymbol();
  public static final Object CDR = new CdrSymbol();
  public static final Object CONS = new ConsSymbol();
  public static final Object EQ = new EqSymbol();
  public static final Object HANDLE = new HandleSymbol();
  public static final Object NIL = new NilSymbol();
  public static final Object PERFORM = new PerformSymbol();
  public static final Object QUOTE = new QuoteSymbol();
}
