/*
 * Calipto Nodes - The text API
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
package org.calipto.node;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;

import org.calipto.type.symbol.Nil;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.Node;

public abstract class ScopingNode extends CaliptoNode {
  public static ScopingNode findEnclosingScope(Node node) {
    Node parent = node.getParent();
    // Find a nearest block node.
    while (parent != null && !(parent instanceof ScopingNode)) {
      parent = parent.getParent();
    }
    return (ScopingNode) parent;
  }

  /**
   * @return the function name for function scope, "block" otherwise.
   */
  public abstract String getName();

  public abstract Object getVariables(Frame frame);

  public abstract Object getArguments(Frame frame);

  @ExportLibrary(InteropLibrary.class)
  protected static final class ArrayVariablesMapObject implements TruffleObject {
    final String[] parameters;
    final Object[] arguments;
    final Map<String, Integer> slots;

    private ArrayVariablesMapObject(String[] parameters, Object[] arguments) {
      this.parameters = parameters;
      this.arguments = arguments;

      @SuppressWarnings("unchecked")
      Entry<String, Integer>[] entries = new Entry[parameters.length];
      for (int i = 0; i < parameters.length; i++) {
        entries[i] = new SimpleEntry<>(parameters[i], i);
      }
      this.slots = Map.ofEntries(entries);
    }

    @SuppressWarnings("static-method")
    @ExportMessage
    boolean hasMembers() {
      return true;
    }

    @ExportMessage
    @TruffleBoundary
    Object getMembers(@SuppressWarnings("unused") boolean includeInternal) {
      return new KeysArray(slots.keySet().toArray(new String[0]));
    }

    int getArgumentSlot(String member) throws UnknownIdentifierException {
      int argumentSlot = slots.getOrDefault(member, -1);
      if (argumentSlot == -1) {
        throw UnknownIdentifierException.create(member);
      }
      return argumentSlot;
    }

    @ExportMessage
    @TruffleBoundary
    void writeMember(String member, Object value) throws UnknownIdentifierException {
      arguments[getArgumentSlot(member)] = value;
    }

    @ExportMessage
    @TruffleBoundary
    Object readMember(String member) throws UnknownIdentifierException {
      return arguments[getArgumentSlot(member)];
    }

    @SuppressWarnings("static-method")
    @ExportMessage
    boolean isMemberInsertable(@SuppressWarnings("unused") String member) {
      return false;
    }

    @ExportMessage
    @TruffleBoundary
    boolean isMemberModifiable(String member) {
      return slots.containsKey(member);
    }

    @ExportMessage
    @TruffleBoundary
    boolean isMemberReadable(String member) {
      return slots.containsKey(member);
    }
  }

  @ExportLibrary(InteropLibrary.class)
  protected static final class FrameVariablesMapObject implements TruffleObject {
    final Frame frame;
    final Map<String, ? extends FrameSlot> slots;

    private FrameVariablesMapObject(Frame frame, Map<String, ? extends FrameSlot> slots) {
      this.frame = frame;
      this.slots = slots;
    }

    @SuppressWarnings("static-method")
    @ExportMessage
    boolean hasMembers() {
      return true;
    }

    @ExportMessage
    @TruffleBoundary
    Object getMembers(@SuppressWarnings("unused") boolean includeInternal) {
      return new KeysArray(slots.keySet().toArray(new String[0]));
    }

    FrameSlot getArgumentSlot(String member) throws UnknownIdentifierException {
      FrameSlot argumentSlot = slots.get(member);
      if (argumentSlot == null) {
        throw UnknownIdentifierException.create(member);
      }
      return argumentSlot;
    }

    @ExportMessage
    @TruffleBoundary
    void writeMember(String member, Object value)
        throws UnsupportedMessageException, UnknownIdentifierException {
      if (frame == null) {
        throw UnsupportedMessageException.create();
      }
      frame.setObject(getArgumentSlot(member), value);
    }

    @ExportMessage
    @TruffleBoundary
    Object readMember(String member) throws UnknownIdentifierException {
      if (frame == null) {
        return Nil.NIL;
      }
      return frame.getValue(getArgumentSlot(member));
    }

    @SuppressWarnings("static-method")
    @ExportMessage
    boolean isMemberInsertable(@SuppressWarnings("unused") String member) {
      return false;
    }

    @ExportMessage
    @TruffleBoundary
    boolean isMemberModifiable(String member) {
      return slots.containsKey(member);
    }

    @ExportMessage
    @TruffleBoundary
    boolean isMemberReadable(String member) {
      return frame == null || slots.containsKey(member);
    }
  }

  @ExportLibrary(InteropLibrary.class)
  protected static final class KeysArray implements TruffleObject {
    private final String[] keys;

    KeysArray(String[] keys) {
      this.keys = keys;
    }

    @SuppressWarnings("static-method")
    @ExportMessage
    boolean hasArrayElements() {
      return true;
    }

    @ExportMessage
    boolean isArrayElementReadable(long index) {
      return index >= 0 && index < keys.length;
    }

    @ExportMessage
    long getArraySize() {
      return keys.length;
    }

    @ExportMessage
    Object readArrayElement(long index) throws InvalidArrayIndexException {
      if (!isArrayElementReadable(index)) {
        throw InvalidArrayIndexException.create(index);
      }
      return keys[(int) index];
    }
  }
}
