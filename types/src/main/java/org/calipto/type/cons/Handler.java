package org.calipto.type.cons;

import org.calipto.type.DataLibrary;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;

@ExportLibrary(DataLibrary.class)
@ExportLibrary(InteropLibrary.class)
public final class Handler implements TruffleObject {}
