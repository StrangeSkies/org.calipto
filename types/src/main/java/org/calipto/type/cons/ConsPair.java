/*
 * Calipto Types - The Calipto types
 *
 * Copyright © 2018 Strange Skies (elias@vasylenko.uk)
 *     __   _______  ____           _       __     _      __       __
 *   ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *  ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *   `._ `.   | |   | |-. L      / / \ \   | |\ \ | || |    _ | '-~.
 *  _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +~-'
 * \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *  `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                  __    _         _      __      __
 *                ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *               ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                `._ `. | L-' L   | || '-~.     `._ `.
 *               _   `. \| ,.-^.`. | || +~-'    _   `. \
 *              \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *               `.__.-` |_|    |_||_|  `-.__.J `.__.-`
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.calipto.type.cons;

import static org.calipto.type.symbol.NilSymbol.NIL;

import org.calipto.type.DataLibrary;

import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

@ExportLibrary(DataLibrary.class)
@ExportLibrary(InteropLibrary.class)
public final class ConsPair implements TruffleObject {
  private final Object car;
  private final Object cdr;

  public ConsPair(Object car, Object cdr) {
    this.car = car;
    this.cdr = cdr;
  }

  @ExportMessage
  static class Equals {
    @Specialization
    static boolean doPair(
        ConsPair receiver,
        ConsPair other,
        @CachedLibrary(limit = "3") DataLibrary carData,
        @CachedLibrary(limit = "3") DataLibrary cdrData) {
      return carData.equals(receiver.car, other.car) && cdrData.equals(receiver.cdr, other.cdr);
    }

    @Specialization(guards = "otherData.isCons(other)", limit = "3", replaces = "doPair")
    static boolean doDefault(
        ConsPair receiver,
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
  Object consOnto(Object cdr) {
    if (cdr == NIL) {
      return new Singleton(this);
    }
    return null;
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
}
