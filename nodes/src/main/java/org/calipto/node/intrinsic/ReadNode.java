package org.calipto.node.intrinsic;

import java.util.function.IntPredicate;

import org.calipto.reader.CaliptoData;
import org.calipto.reader.CaliptoReader;
import org.calipto.reader.ReaderMacro;
import org.calipto.reader.ReadingContext;
import org.calipto.scanner.Cursor;
import org.calipto.scanner.Scanner;
import org.calipto.type.DataLibrary;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo(shortName = "read")
public abstract class ReadNode extends IntrinsicNode {
  private static class CaliptoDataWrapper implements CaliptoData {
    private final Object data;

    public CaliptoDataWrapper(Object data) {
      this.data = data;
    }

    public Object getValue() {
      return data;
    }
  }

  private static final DataLibrary dataLibrary = DataLibrary.getFactory().createDispatched(5);

  private final FrameSlot dynamicScopeSlot;
  private final FrameSlot readTableSlot;

  public ReadNode(FrameSlot dynamicScopeSlot, FrameSlot readTableSlot) {
    this.dynamicScopeSlot = dynamicScopeSlot;
    this.readTableSlot = readTableSlot;
  }

  @Override
  protected Object execute(VirtualFrame frame) {
    try {
      Object dynamicScope = frame.getObject(dynamicScopeSlot);
      DataLibrary dynamicScopeLibrary = DataLibrary.getFactory().create(dynamicScope);
      dynamicScopeLibrary.adoptChildren();

      Object readTable = frame.getObject(readTableSlot);
      DataLibrary readTableLibrary = DataLibrary.getFactory().create(readTable);
      readTableLibrary.adoptChildren();

      new CaliptoReader(new ReadingContext() {
        @Override
        public CaliptoData makeCons(Object car, Object cdr) {
          return new CaliptoDataWrapper(valueLibrary.consWith(cdr, car));
        }

        @Override
        public CaliptoData makeSymbol(String namespace, String name) {
          return new CaliptoDataWrapper(symbolLibrary);
        }

        @Override
        public ReaderMacro resolveReaderMacro(CaliptoData symbol) {
          return readTableLibrary.get(readTable, symbol);
        }
      }, new Scanner() {
        @Override
        public String takeBufferTo(long position) {
          // TODO Auto-generated method stub
          return null;
        }

        @Override
        public Cursor peekInput() {
          // TODO Auto-generated method stub
          return null;
        }

        @Override
        public long inputPosition() {
          // TODO Auto-generated method stub
          return 0;
        }

        @Override
        public void discardBufferTo(long position) {
          // TODO Auto-generated method stub

        }

        @Override
        public long bufferPosition() {
          // TODO Auto-generated method stub
          return 0;
        }

        @Override
        public Cursor advanceInputWhile(IntPredicate condition) {
          // TODO Auto-generated method stub
          return null;
        }

        @Override
        public Cursor advanceInput() {
          // TODO Auto-generated method stub
          return null;
        }
      });

      return null;
    } catch (FrameSlotTypeException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}