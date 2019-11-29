package org.calipto;

import java.util.HashSet;
import java.util.Set;

import com.oracle.truffle.api.interop.TruffleObject;

final class Definition implements TruffleObject {
  /*
   * Things which
   */
  private final Set<String> mentions = new HashSet<>();

  Definition() {}
}
