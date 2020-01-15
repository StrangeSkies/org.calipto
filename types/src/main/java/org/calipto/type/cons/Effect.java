package org.calipto.type.cons;

import java.util.logging.Level;

import org.calipto.type.DataLibrary;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLogger;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.utilities.CyclicAssumption;

@ExportLibrary(DataLibrary.class)
@ExportLibrary(InteropLibrary.class)
public class Effect {
  public static final int INLINE_CACHE_SIZE = 2;

  private static final TruffleLogger LOG = TruffleLogger.getLogger(SLLanguage.ID, SLFunction.class);

  /** The name of the function. */
  private final Object name;

  /** The current implementation of this function. */
  private RootCallTarget callTarget;

  /**
   * Manages the assumption that the {@link #callTarget} is stable. We use the
   * utility class {@link CyclicAssumption}, which automatically creates a new
   * {@link Assumption} when the old one gets invalidated.
   */
  private final CyclicAssumption callTargetStable;

  protected Effect(String name) {
    this.name = name;
    this.callTarget = Truffle
        .getRuntime()
        .createCallTarget(new SLUndefinedFunctionRootNode(language, name));
    this.callTargetStable = new CyclicAssumption(name);
  }

  public String getName() {
    return name;
  }

  @ExportMessage
  static class Equals {
    @Specialization
    static boolean doPair(
        Effect receiver,
        Effect other,
        @CachedLibrary(limit = "3") DataLibrary carData,
        @CachedLibrary(limit = "3") DataLibrary cdrData) {
      return carData.equals(receiver.car, other.car) && cdrData.equals(receiver.cdr, other.cdr);
    }

    @Specialization(guards = "otherData.isCons(other)", limit = "3", replaces = "doPair")
    static boolean doDefault(
        Effect receiver,
        Object other,
        @CachedLibrary("other") DataLibrary otherData,
        @CachedLibrary(limit = "3") DataLibrary carData,
        @CachedLibrary(limit = "3") DataLibrary cdrData) {
      return carData.equals(receiver.car(), otherData.car(other))
          && cdrData.equals(receiver.cdr(), otherData.cdr(other));
    }

    @Fallback
    static boolean doFallback(ConsPair receiver, Object other) {
      return false;
    }
  }

  @ExportMessage
  Object consWith(Object car) {
    return new ConsPair(car, this);
  }

  @ExportMessage
  Object consOntoNil() {
    return new Singleton(this);
  }

  @ExportMessage
  Object car() {
    return car;
  }

  @ExportMessage
  Object cdr() {
    return cdr;
  }

  @ExportMessage
  boolean isCons() {
    return true;
  }

  @ExportMessage
  boolean isData() {
    return true;
  }

  protected void setCallTarget(RootCallTarget callTarget) {
    this.callTarget = callTarget;
    /*
     * We have a new call target. Invalidate all code that speculated that the old
     * call target was stable.
     */
    LOG.log(Level.FINE, "Installed call target for: {0}", name);
    callTargetStable.invalidate();
  }

  public RootCallTarget getCallTarget() {
    return callTarget;
  }

  public Assumption getCallTargetStable() {
    return callTargetStable.getAssumption();
  }

  /**
   * This method is, e.g., called when using a function literal in a string
   * concatenation. So changing it has an effect on SL programs.
   */
  @Override
  public String toString() {
    return name;
  }

  /**
   * {@link SLFunction} instances are always visible as executable to other
   * languages.
   */
  @SuppressWarnings("static-method")
  public SourceSection getDeclaredLocation() {
    return getCallTarget().getRootNode().getSourceSection();
  }

  /**
   * {@link SLFunction} instances are always visible as executable to other
   * languages.
   */
  @SuppressWarnings("static-method")
  @ExportMessage
  boolean isExecutable() {
    return true;
  }

  /**
   * We allow languages to execute this function. We implement the interop execute
   * message that forwards to a function dispatch.
   */
  @ExportMessage
  abstract static class Execute {

    /**
     * Inline cached specialization of the dispatch.
     *
     * <p>
     * Since SL is a quite simple language, the benefit of the inline cache seems
     * small: after checking that the actual function to be executed is the same as
     * the cachedFuntion, we can safely execute the cached call target. You can
     * reasonably argue that caching the call target is overkill, since we could
     * just retrieve it via {@code function.getCallTarget()}. However, caching the
     * call target and using a {@link DirectCallNode} allows Truffle to perform
     * method inlining. In addition, in a more complex language the lookup of the
     * call target is usually much more complicated than in SL.
     * </p>
     *
     * <p>
     * {@code limit = "INLINE_CACHE_SIZE"} Specifies the limit number of inline
     * cache specialization instantiations.
     * </p>
     * <p>
     * {@code guards = "function.getCallTarget() == cachedTarget"} The inline cache
     * check. Note that cachedTarget is a final field so that the compiler can
     * optimize the check.
     * </p>
     * <p>
     * {@code assumptions = "callTargetStable"} Support for function redefinition:
     * When a function is redefined, the call target maintained by the SLFunction
     * object is changed. To avoid a check for that, we use an Assumption that is
     * invalidated by the SLFunction when the change is performed. Since checking an
     * assumption is a no-op in compiled code, the assumption check performed by the
     * DSL does not add any overhead during optimized execution.
     * </p>
     *
     * @see Cached
     * @see Specialization
     *
     * @param function
     *          the dynamically provided function
     * @param cachedFunction
     *          the cached function of the specialization instance
     * @param callNode
     *          the {@link DirectCallNode} specifically created for the
     *          {@link CallTarget} in cachedFunction.
     */
    @Specialization(
        limit = "INLINE_CACHE_SIZE", //
        guards = "function.getCallTarget() == cachedTarget", //
        assumptions = "callTargetStable")
    @SuppressWarnings("unused")
    protected static Object doDirect(
        Effect function,
        Object[] arguments,
        @Cached("function.getCallTargetStable()") Assumption callTargetStable,
        @Cached("function.getCallTarget()") RootCallTarget cachedTarget,
        @Cached("create(cachedTarget)") DirectCallNode callNode) {

      /* Inline cache hit, we are safe to execute the cached call target. */
      Object returnValue = callNode.call(arguments);
      return returnValue;
    }

    /**
     * Slow-path code for a call, used when the polymorphic inline cache exceeded
     * its maximum size specified in <code>INLINE_CACHE_SIZE</code>. Such calls are
     * not optimized any further, e.g., no method inlining is performed.
     */
    @Specialization(replaces = "doDirect")
    protected static Object doIndirect(
        Effect function,
        Object[] arguments,
        @Cached IndirectCallNode callNode) {
      /*
       * SL has a quite simple call lookup: just ask the function for the current call
       * target, and call it.
       */
      return callNode.call(function.getCallTarget(), arguments);
    }
  }

}
