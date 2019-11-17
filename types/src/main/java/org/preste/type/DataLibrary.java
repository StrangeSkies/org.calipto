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
package org.preste.type;

import org.preste.type.cons.Cons;
import org.preste.type.cons.Int32;
import org.preste.type.cons.Singleton;
import org.preste.type.symbol.Bool;

import com.oracle.truffle.api.library.GenerateLibrary;
import com.oracle.truffle.api.library.GenerateLibrary.Abstract;
import com.oracle.truffle.api.library.GenerateLibrary.DefaultExport;
import com.oracle.truffle.api.library.Library;
import com.oracle.truffle.api.library.LibraryFactory;

@DefaultExport(Bool.class)
@DefaultExport(Int32.class)
@GenerateLibrary
public abstract class DataLibrary extends Library {
  public static LibraryFactory<DataLibrary> getFactory() {
    return LibraryFactory.resolve(DataLibrary.class);
  }

  /*
   * General messages
   */

  @Abstract(ifExported = { "isSymbol", "isCons" })
  public boolean isData(Object receiver) {
    return false;
  }

  public abstract boolean equals(Object first, Object second);

  /**
   * Cons the given value onto the receiver.
   * 
   * @param receiver
   *          the receiver, which will be the new cdr in the resulting cons cell
   * @param value
   *          the value to be the new car in the resulting cons cell
   * @return the cons of the value onto the receiver
   */
  public Object consWith(Object receiver, Object value) {
    return new Cons(receiver, value);
  }

  public Object consOntoNil(Object receiver) {
    return new Singleton(receiver);
  }

  /*
   * Symbol messages.
   */

  @Abstract(ifExported = { "namespace", "name" })
  public boolean isSymbol(Object receiver) {
    return false;
  }

  @Abstract(ifExported = { "isSymbol" })
  public String namespace(Object receiver) {
    throw new UnsupportedOperationException();
  }

  @Abstract(ifExported = { "isSymbol" })
  public String name(Object receiver) {
    throw new UnsupportedOperationException();
  }

  /*
   * Cons messages
   */

  @Abstract(ifExported = { "car", "cdr", "get" })
  public boolean isCons(Object receiver) {
    return false;
  }

  @Abstract(ifExported = { "isCons" })
  public Object car(Object receiver) {
    throw new UnsupportedOperationException();
  }

  @Abstract(ifExported = { "isCons" })
  public Object cdr(Object receiver) {
    throw new UnsupportedOperationException();
  }

  public Object get(Object receiver, Object key) {
    var entry = car(receiver);
    var tail = cdr(receiver);

    var library = getFactory().getUncached();
    while (true) {
      if (library.isCons(entry) && library.equals(library.car(entry), key)) {
        return library.cdr(entry);
      }
      if (!library.isCons(tail)) {
        break;
      }
      entry = library.car(tail);
      tail = library.cdr(tail);
    }
    throw new UnsupportedOperationException();
  }
}
