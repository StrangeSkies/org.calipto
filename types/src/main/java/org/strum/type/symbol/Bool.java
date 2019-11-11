/*
 * Strum Types - The Strum types
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
package org.strum.type.symbol;

import org.strum.type.cons.ConsLibrary;
import org.strum.type.cons.IntTo32;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

// TODO value type
@ExportLibrary(SymbolLibrary.class)
@ExportLibrary(InteropLibrary.class)
public final class Bool implements TruffleObject {
  public static final Bool TRUE = new Bool(true);
  public static final Bool FALSE = new Bool(false);

  private final boolean value;

  private Bool(boolean value) {
    this.value = value;
  }

  @ExportMessage
  public String namespace() {
    return "";
  }

  @ExportMessage
  public String name() {
    return Boolean.toString(value);
  }

  @Override
  @ExportMessage
  public String toString() {
    return "/" + name();
  }

  @ExportMessage
  abstract static class Cons {
    @Specialization(guards = "conses.isCons(cdr)", limit = "3")
    static Object doCons(Bool car, Object cdr, @CachedLibrary("cdr") ConsLibrary conses) {
      return new org.strum.type.cons.Cons(car, cdr);
    }

    @Specialization(guards = "symbols.isSymbol(cdr)", limit = "3")
    static Object doSymbol(Bool car, Object cdr, @CachedLibrary("cdr") SymbolLibrary symbols) {
      return new org.strum.type.cons.Cons(car, cdr);
    }

    @Specialization(replaces = "doCons")
    static Object doIntTo(Bool car, IntTo32 cdr) {
      return null;
    }
  }

  @Override
  @ExportMessage
  public boolean equals(Object obj) {
    if (!(obj instanceof Bool)) {
      return false;
    }
    Bool that = (Bool) obj;
    return this.value == that.value;
  }

  @ExportMessage
  boolean isNull() {
    return true;
  }

  @ExportMessage
  boolean isBoolean() {
    return true;
  }

  @ExportMessage
  boolean asBoolean() {
    return value;
  }
}
