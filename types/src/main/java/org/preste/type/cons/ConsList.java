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

import org.preste.type.DataIterator;
import org.preste.type.DataLibrary;
import org.preste.type.symbol.Nil;

import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

@ExportLibrary(DataLibrary.class)
@ExportLibrary(InteropLibrary.class)
public final class ConsList implements TruffleObject {
  private final Object[] elements;
  private final int size;
  private volatile int consCounter;

  private ConsList(Object[] elements, int size) {
    this.elements = elements;
    this.size = size;
    this.consCounter = (elements.length == size) ? 1 : 0;
  }

  @ExportMessage
  static class Equals {
    @Specialization
    static boolean doList(
        ConsList receiver,
        ConsList other,
        @CachedLibrary(limit = "3") DataLibrary elementData) {
      if (receiver.size != other.size) {
        return false;
      }
      for (int i = 0; i < receiver.size; i++) {
        if (!elementData.equals(receiver.elements[i], other.elements[i])) {
          return false;
        }
      }
      return true;
    }

    @Specialization(guards = "elementData.isCons(other)", replaces = "doList")
    static boolean doDefault(
        ConsList receiver,
        Object other,
        @CachedLibrary(limit = "3") DataLibrary elementData) {
      DataIterator otherIterator = elementData.iterator(other);
      for (int i = 0; i < receiver.size; i++) {
        if (!otherIterator.hasNext()) {
          return false;
        }
        if (!elementData.equals(receiver.elements[i], otherIterator.next())) {
          return false;
        }
      }
      if (otherIterator.hasNext() || otherIterator.terminal() != Nil.NIL) {
        return false;
      }
      return true;
    }

    @Fallback
    static boolean doFallback(ConsList receiver, Object other) {
      return false;
    }
  }

  @ExportMessage
  Object consWith(Object car) {
    Object[] newElements;
    int newSize = size + 1;

    consCounter++;
    if (consCounter == 1) { // at most one parallel call can pass this
      newElements = elements;
    } else {
      consCounter--; // TODO not sure if this is safe
      int newLength = (newSize <= elements.length) ? newSize : (int) (newSize * 1.3) + 1;
      newElements = new Object[newLength];
      System.arraycopy(elements, 0, newElements, 0, size);
    }

    newElements[size] = car;
    return new ConsList(newElements, newSize);
  }

  @ExportMessage
  Object consOntoNil() {
    return new Singleton(this);
  }

  @ExportMessage
  Object car() {
    return elements[size - 1];
  }

  @ExportMessage
  Object cdr() {
    return new ConsList(elements, size - 1);
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
