package org.strum;

import org.strum.type.CellLibrary;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;

public class StrumContext {
  private final FrameDescriptor globalFrameDescriptor;
  private final Namespace globalNamespace;
  private final MaterializedFrame globalFrame;

  public StrumContext() {
    this.globalFrameDescriptor = new FrameDescriptor();
    this.globalNamespace = new Namespace(this.globalFrameDescriptor);
    this.globalFrame = this.initGlobalFrame();
  }

  private MaterializedFrame initGlobalFrame() {
    VirtualFrame frame = Truffle.getRuntime().createVirtualFrame(null, this.globalFrameDescriptor);
    addGlobalFunctions(frame);
    return frame.materialize();
  }

  private static void addGlobalFunctions(VirtualFrame virtualFrame) {
    FrameDescriptor frameDescriptor = virtualFrame.getFrameDescriptor();

    /*
     * Add built-ins here. Possibly also intrinsics.
     */
  }

  /**
   * @return A {@link MaterializedFrame} on the heap that contains all global
   *         values.
   */
  public MaterializedFrame getGlobalFrame() {
    return this.globalFrame;
  }

  public Namespace getGlobalNamespace() {
    return this.globalNamespace;
  }
}