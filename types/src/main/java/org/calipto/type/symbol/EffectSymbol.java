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
package org.calipto.type.symbol;

import org.calipto.type.DataLibrary;
import org.calipto.type.cons.ConsPair;
import org.calipto.type.cons.Singleton;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

// TODO value type
@ExportLibrary(DataLibrary.class)
@ExportLibrary(InteropLibrary.class)
public final class EffectSymbol implements TruffleObject {
  public static final EffectSymbol NIL = new EffectSymbol();

  private EffectSymbol() {}

  @ExportMessage
  public boolean isData() {
    return true;
  }

  @ExportMessage
  public boolean isSymbol() {
    return true;
  }

  @ExportMessage
  public String namespace() {
    return "";
  }

  @ExportMessage
  public String name() {
    return "nil";
  }

  @ExportMessage
  Object consOntoNil() {
    return new Singleton(this);
  }

  @ExportMessage
  Object consWith(Object car) {
    return new ConsPair(car, this);
  }

  @Override
  @ExportMessage
  public boolean equals(Object obj) {
    return obj == this;
  }

  @ExportMessage
  boolean isNull() {
    return true;
  }
}
