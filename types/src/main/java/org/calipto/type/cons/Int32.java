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
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

/*
 * TODO when we have value types, this should be a custom int32
 * value type implementation without an explicit receiverType.
 * 
 * (hopefully we will be able to specialize generics over values,
 * so we can have a single Int<X> type to specialize as Int<32>,
 * Int<31>, etc.)
 */
@ExportLibrary(value = DataLibrary.class, receiverType = Integer.class)
public final class Int32 implements TruffleObject {
  @ExportMessage
  static class Equals {
    @Specialization
    static boolean doInt32(Integer receiver, Integer other) {
      return receiver.equals(other);
    }

    @Specialization(guards = "otherData.isCons(other)", limit = "3", replaces = "doInt32")
    static boolean doGeneral(
        Integer receiver,
        Object other,
        @CachedLibrary("other") DataLibrary otherData,
        @CachedLibrary(limit = "3") DataLibrary carData,
        @CachedLibrary(limit = "3") DataLibrary cdrData) {
      return carData.equals(car(receiver), otherData.car(other))
          && cdrData.equals(cdr(receiver), otherData.cdr(other));
    }

    @Specialization(replaces = "doGeneral")
    static boolean doFallback(Integer receiver, Object other) {
      return false;
    }
  }

  @ExportMessage
  static class ConsWith {
    @Specialization
    static Object doBoolean(Integer receiver, Boolean car) {
      return new IntTo64(receiver.longValue() | ((car ? 1l : 0l) << 32), 33);
    }

    @Fallback
    static Object doFallback(Integer receiver, Object car) {
      return new ConsPair(car, receiver);
    }
  }

  @ExportMessage
  static Object consOnto(Integer receiver, Object cdr) {
    if (cdr == NIL) {
      return new Singleton(receiver);
    }
    return null;
  }

  @ExportMessage
  static boolean car(Integer receiver) {
    return receiver >> 31 > 0;
  }

  @ExportMessage
  static Object cdr(Integer receiver) {
    return new IntTo32(receiver, 31);
  }

  @ExportMessage
  static boolean isCons(Integer receiver) {
    return true;
  }

  @ExportMessage
  static boolean isData(Integer receiver) {
    return true;
  }
}
