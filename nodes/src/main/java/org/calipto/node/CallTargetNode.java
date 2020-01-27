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

import org.calipto.CaliptoLanguage;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.SourceSection;

@NodeInfo(language = "Calipto", description = "The root of all execution trees")
public class CallTargetNode extends RootNode {
  @Children
  private CaliptoNode bodyNode;

  private final String name;

  @CompilationFinal
  private boolean cloningAllowed;

  private final SourceSection sourceSection;

  public CallTargetNode(
      String name,
      CaliptoLanguage language,
      FrameDescriptor frameDescriptor,
      CaliptoNode bodyNode,
      SourceSection sourceSection) {
    super(language, frameDescriptor);

    this.bodyNode = bodyNode;
    this.sourceSection = sourceSection;
    this.name = name;
  }

  @Override
  public SourceSection getSourceSection() {
    return sourceSection;
  }

  @Override
  @ExplodeLoop
  public Object execute(VirtualFrame frame) {
    assert lookupContextReference(CaliptoLanguage.class).get() != null;
    return this.bodyNode.executeGeneric(frame);
  }

  public CaliptoNode getBodyNode() {
    return bodyNode;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setCloningAllowed(boolean cloningAllowed) {
    this.cloningAllowed = cloningAllowed;
  }

  @Override
  public boolean isCloningAllowed() {
    return cloningAllowed;
  }

  @Override
  public String toString() {
    return name;
  }
}
