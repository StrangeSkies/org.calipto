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
package org.strum.type;

import org.strum.type.symbol.Bool;
import org.strum.type.symbol.Nil;

import com.oracle.truffle.api.dsl.TypeCast;
import com.oracle.truffle.api.dsl.TypeCheck;
import com.oracle.truffle.api.dsl.TypeSystem;

/**
 * The only <em>primitive</em> types in Preste are cons cells and symbols, which
 * don't provide a very rich basis for specialising our representations for
 * efficient storage and access.
 * <p>
 * On the other hand, any given value in Preste belongs to a
 * potentially-infinite number of type predicates, so can't use those as a basis
 * for selecting a representation for our types either.
 * <p>
 * Instead, representations are chosen based on a few specially-selected forms.
 * 
 * @author eli
 */
@TypeSystem()
public class StrumTypes {

  @TypeCheck(Nil.class)
  public static boolean isNil(Object value) {
    return value == Nil.NIL;
  }

  @TypeCast(Nil.class)
  public static Nil asNil(Object value) {
    assert isNil(value);
    return Nil.NIL;
  }

  @TypeCheck(Bool.class)
  public static boolean isBool(Object value) {
    return value == Bool.TRUE || value == Bool.FALSE;
  }

  @TypeCast(Bool.class)
  public static Bool asBool(Object value) {
    assert isNil(value);
    return value == Bool.TRUE ? Bool.TRUE : Bool.FALSE;
  }
}