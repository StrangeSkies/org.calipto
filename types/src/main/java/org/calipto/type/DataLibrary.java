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
package org.calipto.type;

import org.calipto.type.cons.Int16;
import org.calipto.type.cons.Int32;
import org.calipto.type.cons.Int64;
import org.calipto.type.cons.Int8;
import org.calipto.type.symbol.BoolSymbol;
import org.calipto.type.symbol.NilSymbol;

import com.oracle.truffle.api.library.GenerateLibrary;
import com.oracle.truffle.api.library.GenerateLibrary.Abstract;
import com.oracle.truffle.api.library.GenerateLibrary.DefaultExport;
import com.oracle.truffle.api.library.Library;
import com.oracle.truffle.api.library.LibraryFactory;

@DefaultExport(BoolSymbol.class)
@DefaultExport(Int8.class)
@DefaultExport(Int16.class)
@DefaultExport(Int32.class)
@DefaultExport(Int64.class)
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
   * Cons the receiver onto the given value. This should always be called in
   * preference to {@link #consWith(Object, Object)} on the value.
   * 
   * @param receiver
   *          the receiver, which will be the new car in the resulting cons cell
   * @param value
   *          the value to be the new cdr in the resulting cons cell
   * @return the cons of the receiver onto the value, or null to delegate to
   *         {@link #consWith(Object, Object)}
   */
  public Object consOnto(Object receiver, Object value) {
    return null;
  }

  /**
   * Cons the given value onto the receiver. This should only be called in the
   * case that there is no appropriate specialization of
   * {@link #consOnto(Object, Object)} on the value.
   * 
   * @param receiver
   *          the receiver, which will be the new cdr in the resulting cons cell
   * @param value
   *          the value to be the new car in the resulting cons cell
   * @return the cons of the value onto the receiver
   */
  public abstract Object consWith(Object receiver, Object value);

  public DataIterator iterator(Object receiver) {
    return new DataIterator() {
      private DataLibrary tailLibrary = DataLibrary.this;
      private Object tail = receiver;

      @Override
      public Object next() {
        if (!hasNext()) {
          throw new IndexOutOfBoundsException();
        }

        Object car = tailLibrary.car(tail);
        tail = tailLibrary.cdr(tail);

        if (tailLibrary == DataLibrary.this && !tailLibrary.accepts(tail)) {
          tailLibrary = getFactory().createDispatched(3);
        }

        return car;
      }

      @Override
      public boolean hasNext() {
        return tailLibrary.isCons(tail);
      }

      @Override
      public Object terminal() {
        if (hasNext()) {
          throw new IllegalStateException();
        }
        return tail;
      }

      @Override
      public boolean isTerminalKnown() {
        return !hasNext();
      }

      @Override
      public boolean isProper() {
        if (hasNext()) {
          throw new IllegalStateException();
        }
        return tail == NilSymbol.NIL;
      }
    };
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

  public String qualifiedName(Object receiver) {
    return namespace(receiver) + "/" + name(receiver);
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
