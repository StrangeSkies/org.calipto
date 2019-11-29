package org.calipto.node.intrinsic;

import org.calipto.CaliptoContext;
import org.calipto.CaliptoLanguage;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.source.Source;

@NodeInfo(shortName = "eval")
public abstract class EvalNode extends IntrinsicNode {

  @Specialization(guards = { "stringsEqual(cachedId, id)", "stringsEqual(cachedCode, code)" })
  public Object evalCached(
      String id,
      String code,
      @Cached("id") String cachedId,
      @Cached("code") String cachedCode,
      @CachedContext(CaliptoLanguage.class) CaliptoContext context,
      @Cached("create(parse(id, code, context))") DirectCallNode callNode) {
    return callNode.call(new Object[] {});
  }

  @TruffleBoundary
  @Specialization(replaces = "evalCached")
  public Object evalUncached(
      String id,
      String code,
      @CachedContext(CaliptoLanguage.class) CaliptoContext context) {
    return parse(id, code, context).call();
  }

  protected CallTarget parse(String id, String code, CaliptoContext context) {
    final Source source = Source.newBuilder(id, code, "(eval)").build();
    return context.parse(source);
  }

  /* Work around findbugs warning in generate code. */
  protected static boolean stringsEqual(String a, String b) {
    return a.equals(b);
  }
}
