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

import static org.calipto.type.symbol.Symbols.NIL;

import org.calipto.type.DataLibrary;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

@ExportLibrary(value = DataLibrary.class)
@ExportLibrary(value = InteropLibrary.class)
public final class IntTo64 implements TruffleObject {
  private final long value;
  private final int bits;

  public IntTo64(long value, int bits) {
    this.value = value;
    this.bits = bits;
  }

  @ExportMessage
  boolean car() {
    return value >> (bits - 1) > 0;
  }

  @ExportMessage
  Object cdr() {
    return new IntTo64(value, bits - 1);
  }

  @ExportMessage
  static class Equals {
    @Specialization
    public static boolean doSingleton(
        IntTo64 receiver,
        IntTo64 other,
        @CachedLibrary(limit = "3") DataLibrary carData) {
      return receiver.value == other.value;
    }

    @Specialization(replaces = "doSingleton")
    public static boolean doFallback(
        IntTo64 receiver,
        Object other,
        @CachedLibrary(limit = "3") DataLibrary otherData) {
      var otherIterator = otherData.iterator(other);
      for (int i = 0; i < receiver.bits; i++) {
        if (!otherIterator.hasNext()) {
          return false;
        }
        var next = otherIterator.next();
        if (!(next instanceof Boolean)) {
          return false;
        }
        boolean nextBoolean = ((boolean) next);
        if (!((receiver.value >> i & 1) > 0) != nextBoolean) {
          return false;
        }
      }
      if (otherIterator.hasNext() || otherIterator.terminal() != NIL) {
        return false;
      }
      return true;
    }
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
