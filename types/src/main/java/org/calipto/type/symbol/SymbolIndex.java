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

import static org.calipto.type.symbol.AtomSymbol.ATOM;
import static org.calipto.type.symbol.CarSymbol.CAR;
import static org.calipto.type.symbol.CdrSymbol.CDR;
import static org.calipto.type.symbol.ConsSymbol.CONS;
import static org.calipto.type.symbol.EqSymbol.EQ;
import static org.calipto.type.symbol.HandlerSymbol.HANDLER;
import static org.calipto.type.symbol.NilSymbol.NIL;
import static org.calipto.type.symbol.QuoteSymbol.QUOTE;

import java.util.IdentityHashMap;
import java.util.Map;

import org.calipto.type.DataLibrary;

public class SymbolIndex {
  private final DataLibrary symbolLibrary = DataLibrary.getFactory().createDispatched(5);
  private final Map<String, Object> symbols = new IdentityHashMap<>();

  public SymbolIndex() {
    internSymbol(ATOM);
    internSymbol(true);
    internSymbol(false);
    internSymbol(CAR);
    internSymbol(CDR);
    internSymbol(CONS);
    internSymbol(EQ);
    internSymbol(HANDLER);
    internSymbol(NIL);
    internSymbol(QUOTE);
  }

  public Object internSymbol(String string) {
    string = string.intern();
    return symbols.computeIfAbsent(string, Symbol::new);
  }

  public Object internSymbol(Object symbol) {
    return internSymbol(symbolLibrary.qualifiedName(symbol));
  }

  public boolean isSymbol(Object symbol) {
    return symbolLibrary.isSymbol(symbol);
  }
}
