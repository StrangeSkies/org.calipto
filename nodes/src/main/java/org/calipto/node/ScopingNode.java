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

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.calipto.CaliptoBlockNode;
import org.calipto.CaliptoLexicalScope;
import org.calipto.SLBlockNode;
import org.calipto.type.symbol.Nil;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.nodes.NodeVisitor;
import com.oracle.truffle.api.nodes.RootNode;

public abstract class ScopingNode extends CaliptoNode {
  private static ScopingNode getParentScope(Node node) {
    ScopingNode block;
    Node parent = node.getParent();
    // Find a nearest block node.
    while (parent != null && !(parent instanceof ScopingNode)) {
      parent = parent.getParent();
    }
    if (parent != null) {
      block = (ScopingNode) parent;
    } else {
      block = null;
    }
    return block;
  }

  /**
   * @return the function name for function scope, "block" otherwise.
   */
  public abstract String getName();

  public abstract Object getVariables(Frame frame);

  private Map<String, FrameSlot> getVars() {
    if (varSlots == null) {
      if (current != null) {
        varSlots = collectVars(block, current);
      } else if (block != null) {
        // Provide the arguments only when the current node is above the block
        varSlots = collectArgs(block);
      } else {
        varSlots = Collections.emptyMap();
      }
    }
    return varSlots;
  }

    // Variables are slot-based.
    // To collect declared variables, traverse the block's AST and find slots
    // associated
    // with SLWriteLocalVariableNode. The traversal stops when we hit the current
    // node.
    Map<String, FrameSlot> slots = new LinkedHashMap<>(4);
    NodeUtil.forEachChild(varsBlock, new NodeVisitor() {
      @Override
      public boolean visit(Node node) {
        if (node == currentNode) {
          return false;
        }
        // Do not enter any nested blocks.
        if (!(node instanceof BlockNode)) {
          boolean all = NodeUtil.forEachChild(node, this);
          if (!all) {
            return false;
          }
        }
        // Write to a variable is a declaration unless it exists already in a parent
        // scope.
        if (node instanceof WriteLocalVariableNode) {
          WriteLocalVariableNode wn = (WriteLocalVariableNode) node;
          String name = Objects.toString(wn.getSlot().getIdentifier());
          if (!hasParentVar(name)) {
            slots.put(name, wn.getSlot());
          }
        }
        return true;
      }
    });
    return slots;
  }

  @ExportLibrary(InteropLibrary.class)
  static final class VariablesMapObject implements TruffleObject {
    final Map<String, ? extends FrameSlot> slots;
    final Object[] args;
    final Frame frame;

    private VariablesMapObject(Map<String, ? extends FrameSlot> slots, Object[] args, Frame frame) {
      this.slots = slots;
      this.args = args;
      this.frame = frame;
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

    @ExportMessage
    @TruffleBoundary
    void writeMember(String member, Object value)
        throws UnsupportedMessageException, UnknownIdentifierException {
      if (frame == null) {
        throw UnsupportedMessageException.create();
      }
      FrameSlot slot = slots.get(member);
      if (slot == null) {
        throw UnknownIdentifierException.create(member);
      } else {
        Object info = slot.getInfo();
        if (args != null && info != null) {
          args[(Integer) info] = value;
        } else {
          frame.setObject(slot, value);
        }
      }
    }

    @ExportMessage
    @TruffleBoundary
    Object readMember(String member) throws UnknownIdentifierException {
      if (frame == null) {
        return Nil.NIL;
      }
      FrameSlot slot = slots.get(member);
      if (slot == null) {
        throw UnknownIdentifierException.create(member);
      } else {
        Object value;
        Object info = slot.getInfo();
        if (args != null && info != null) {
          value = args[(Integer) info];
        } else {
          value = frame.getValue(slot);
        }
        return value;
      }
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
  static final class KeysArray implements TruffleObject {
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
