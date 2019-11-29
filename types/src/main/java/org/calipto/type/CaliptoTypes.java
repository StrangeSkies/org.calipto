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

import org.calipto.type.symbol.Nil;

import com.oracle.truffle.api.dsl.TypeCast;
import com.oracle.truffle.api.dsl.TypeCheck;
import com.oracle.truffle.api.dsl.TypeSystem;

/**
 * The only <em>primitive</em> types in Calipto are cons cells and symbols, which
 * don't provide a very rich basis for specialising our representations for
 * efficient storage and access.
 * <p>
 * On the other hand, any given value in Calipto belongs to a
 * potentially-infinite number of type predicates, so can't use those as a basis
 * for selecting a representation for our types either.
 * <p>
 * Instead, representations are chosen based on a few specially-selected forms.
 * <p>
 * TODO The primitive types included in the type system are all shadowed by
 * ConsLibrary or SymbolLibrary exports. They are only included here to allow
 * graal to specialise over them properly, since Truffle libraries don't
 * currently support unboxed primitives. If this restriction is lifted they can
 * be removed from here.
 * 
 * @author Elias N Vasylenko
 */
@TypeSystem({ boolean.class, int.class })
public class CaliptoTypes {

  @TypeCheck(Nil.class)
  public static boolean isNil(Object value) {
    return value == Nil.NIL;
  }

  @TypeCast(Nil.class)
  public static Nil asNil(Object value) {
    assert isNil(value);
    return Nil.NIL;
  }
}