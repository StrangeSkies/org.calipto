/*
 * Preste Types - The Preste types
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
package org.preste.type.cons;

import org.preste.type.DataLibrary;

import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

@ExportLibrary(DataLibrary.class)
@ExportLibrary(InteropLibrary.class)
public final class Cons implements TruffleObject {
  private final Object car;
  private final Object cdr;

  public Cons(Object car, Object cdr) {
    this.car = car;
    this.cdr = cdr;
  }

  @ExportMessage
  static class Equals {
    @Fallback
    public static boolean doFallback(Cons receiver, Object other) {
      return false;
    }

    @Specialization
    public static boolean doCons(
        Cons receiver,
        Cons other,
        @CachedLibrary(limit = "3") DataLibrary carData,
        @CachedLibrary(limit = "3") DataLibrary cdrData) {
      return carData.equals(receiver.car(), other.car())
          && cdrData.equals(receiver.cdr(), other.cdr());
    }

    @Specialization(guards = "otherData.isCons(other)", limit = "3", replaces = "doCons")
    public static boolean doDefault(
        Cons receiver,
        Object other,
        @CachedLibrary("other") DataLibrary otherData,
        @CachedLibrary(limit = "3") DataLibrary carData,
        @CachedLibrary(limit = "3") DataLibrary cdrData) {
      return carData.equals(receiver.car(), otherData.car(other))
          && cdrData.equals(receiver.cdr(), otherData.cdr(other));
    }
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
