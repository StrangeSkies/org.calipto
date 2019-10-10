package org.strum;

import java.util.HashMap;
import java.util.Map;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

@ExportLibrary(InteropLibrary.class)
@SuppressWarnings("static-method")
final class DefinitionsObject implements TruffleObject {
  final Map<String, Object> objects = new HashMap<>();

  DefinitionsObject() {}

  @ExportMessage
  boolean hasMembers() {
    return true;
  }

  @ExportMessage
  @TruffleBoundary
  Object readMember(String member) {
    return objects.get(member);
  }

  @ExportMessage
  @TruffleBoundary
  boolean isMemberReadable(String member) {
    return objects.containsKey(member);
  }

  @ExportMessage
  @TruffleBoundary
  Object getMembers(@SuppressWarnings("unused") boolean includeInternal) {
    return new DefinitionNamesObject(objects.keySet().toArray());
  }

  public static boolean isInstance(TruffleObject obj) {
    return obj instanceof DefinitionsObject;
  }

  @ExportLibrary(InteropLibrary.class)
  static final class DefinitionNamesObject implements TruffleObject {

    private final Object[] names;

    DefinitionNamesObject(Object[] names) {
      this.names = names;
    }

    @ExportMessage
    boolean hasArrayElements() {
      return true;
    }

    @ExportMessage
    boolean isArrayElementReadable(long index) {
      return index >= 0 && index < names.length;
    }

    @ExportMessage
    long getArraySize() {
      return names.length;
    }

    @ExportMessage
    Object readArrayElement(long index) throws InvalidArrayIndexException {
      if (!isArrayElementReadable(index)) {
        CompilerDirectives.transferToInterpreter();
        throw InvalidArrayIndexException.create(index);
      }
      return names[(int) index];
    }
  }
}
