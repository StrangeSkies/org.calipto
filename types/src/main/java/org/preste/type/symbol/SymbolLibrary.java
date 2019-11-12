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
package org.preste.type.symbol;

import java.util.Objects;

import org.preste.type.cons.Cons;

import com.oracle.truffle.api.library.GenerateLibrary;
import com.oracle.truffle.api.library.Library;
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.library.GenerateLibrary.DefaultExport;

@DefaultExport(Bool.class)
@GenerateLibrary
public abstract class SymbolLibrary extends Library {
  public static LibraryFactory<SymbolLibrary> getFactory() {
    return LibraryFactory.resolve(SymbolLibrary.class);
  }

  public boolean isSymbol(Object receiver) {
    return false;
  }

  public abstract String namespace(Object receiver);

  public abstract String name(Object receiver);

  public String toString(Object receiver) {
    return namespace(receiver) + "/" + name(receiver);
  }

  public boolean equals(Object first, Object second) {
    if (!isSymbol(first) || !isSymbol(second)) {
      return false;
    }

    return Objects.equals(name(first), name(second))
        && Objects.equals(namespace(first), namespace(second));
  }

  /**
   * Cons the given value onto the receiver.
   * 
   * @param receiver the receiver, which will be the new cdr in the resulting cons
   *                 cell
   * @param value    the value to be the new car in the resulting cons cell
   * @return the cons of the value onto the receiver
   */
  public Object consWith(Object receiver, Object value) {
    return new Cons(receiver, value);
  }
}
