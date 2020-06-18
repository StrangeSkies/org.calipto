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

import java.lang.ref.WeakReference;
import java.util.IdentityHashMap;
import java.util.Map;

import org.calipto.type.DataLibrary;

public class Symbols {
  public static final String SYSTEM_NAMESPACE = "";

  public static final Object ATOM = new AtomSymbol();
  public static final Object CALL = new CallSymbol();
  public static final Object CAR = new CarSymbol();
  public static final Object CDR = new CdrSymbol();
  public static final Object CONS = new ConsSymbol();
  public static final Object EQ = new EqSymbol();
  public static final Object HANDLE = new HandleSymbol();
  public static final Object NIL = new NilSymbol();
  public static final Object PERFORM = new PerformSymbol();
  public static final Object QUOTE = new QuoteSymbol();

  private final DataLibrary data = DataLibrary.getFactory().createDispatched(5);
  private final Map<String, WeakReference<Object>> symbols = new IdentityHashMap<>();

  public Symbols() {
    internSymbol(ATOM);
    internSymbol(true);
    internSymbol(false);
    internSymbol(CAR);
    internSymbol(CDR);
    internSymbol(CONS);
    internSymbol(EQ);
    internSymbol(HANDLE);
    internSymbol(NIL);
    internSymbol(QUOTE);
  }

  public Object internSymbol(String string) {
    string = string.intern();
    var reference = symbols.get(string);
    if (reference != null) {
      var symbol = reference.get();
      if (symbol == null) {
        return symbol;
      }
    }
    var symbol = new Symbol(string);
    symbols.put(string, new WeakReference<>(symbol));
    return symbol;
  }

  public Object internSymbol(String namespace, String name) {
    return internSymbol(namespace + "/" + name);
  }

  public Object internSymbol(Object symbol) {
    return internSymbol(data.qualifiedName(symbol));
  }

  public boolean isSymbol(Object symbol) {
    return data.isSymbol(symbol);
  }
}
