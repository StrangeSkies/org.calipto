/*
 * Preste Nodes - The text API
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
package org.preste.node;

import org.preste.type.PresteTypes;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.GenerateWrapper;
import com.oracle.truffle.api.instrumentation.InstrumentableNode;
import com.oracle.truffle.api.instrumentation.InstrumentableNode.WrapperNode;
import com.oracle.truffle.api.instrumentation.ProbeNode;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;

@TypeSystemReference(PresteTypes.class)
@NodeInfo(language = "Preste", description = "The abstract base node for all expressions")
@GenerateWrapper
@ReportPolymorphism
public abstract class PresteNode extends Node implements InstrumentableNode {
  private static final int NO_SOURCE = -1;
  private static final int UNAVAILABLE_SOURCE = -2;

  private int sourceCharIndex = NO_SOURCE;
  private int sourceLength;

  private boolean hasExpressionTag;
  private boolean hasStatementTag;
  private boolean hasRootTag;

  /*
   * The creation of source section can be implemented lazily by looking up the
   * root node source and then creating the source section object using the
   * indices stored in the node. This avoids the eager creation of source section
   * objects during parsing and creates them only when they are needed.
   * Alternatively, if the language uses source sections to implement language
   * semantics, then it might be more efficient to eagerly create source sections
   * and store it in the AST.
   *
   * For more details see {@link InstrumentableNode}.
   */
  @Override
  @TruffleBoundary
  public final SourceSection getSourceSection() {
    if (sourceCharIndex == NO_SOURCE) {
      // AST node without source
      return null;
    }
    RootNode rootNode = getRootNode();
    if (rootNode == null) {
      // not yet adopted yet
      return null;
    }
    SourceSection rootSourceSection = rootNode.getSourceSection();
    if (rootSourceSection == null) {
      return null;
    }
    Source source = rootSourceSection.getSource();
    if (sourceCharIndex == UNAVAILABLE_SOURCE) {
      return source.createUnavailableSection();
    } else {
      return source.createSection(sourceCharIndex, sourceLength);
    }
  }

  public final boolean hasSource() {
    return sourceCharIndex != NO_SOURCE;
  }

  public final boolean isInstrumentable() {
    return hasSource();
  }

  public final int getSourceCharIndex() {
    return sourceCharIndex;
  }

  public final int getSourceEndIndex() {
    return sourceCharIndex + sourceLength;
  }

  public final int getSourceLength() {
    return sourceLength;
  }

  // invoked by the parser to set the source
  public final void setSourceSection(int charIndex, int length) {
    assert sourceCharIndex == NO_SOURCE : "source must only be set once";
    if (charIndex < 0) {
      throw new IllegalArgumentException("charIndex < 0");
    } else if (length < 0) {
      throw new IllegalArgumentException("length < 0");
    }
    this.sourceCharIndex = charIndex;
    this.sourceLength = length;
  }

  public final void setUnavailableSourceSection() {
    assert sourceCharIndex == NO_SOURCE : "source must only be set once";
    this.sourceCharIndex = UNAVAILABLE_SOURCE;
  }

  public boolean hasTag(Class<? extends Tag> tag) {
    if (tag == StandardTags.ExpressionTag.class) {
      return hasExpressionTag;

    } else if (tag == StandardTags.StatementTag.class) {
      return hasStatementTag;

    } else if (tag == StandardTags.RootTag.class || tag == StandardTags.RootBodyTag.class) {
      return hasRootTag;
    }
    return false;
  }

  public WrapperNode createWrapper(ProbeNode probe) {
    return new PresteNodeWrapper(this, probe);
  }

  /**
   * Marks this node as being a {@link StandardTags.StatementTag} for
   * instrumentation purposes.
   */
  public final void addStatementTag() {
    hasStatementTag = true;
  }

  /**
   * Marks this node as being a {@link StandardTags.RootTag} and
   * {@link StandardTags.RootBodyTag} for instrumentation purposes.
   */
  public final void addRootTag() {
    hasRootTag = true;
  }

  @Override
  public String toString() {
    return formatSourceSection(this);
  }

  /**
   * Formats a source section of a node in human readable form. If no source
   * section could be found it looks up the parent hierarchy until it finds a
   * source section. Nodes where this was required append a <code>'~'</code> at
   * the end.
   *
   * @param node the node to format.
   * @return a formatted source section string
   */
  public static String formatSourceSection(Node node) {
    if (node == null) {
      return "<unknown>";
    }
    SourceSection section = node.getSourceSection();
    boolean estimated = false;
    if (section == null) {
      section = node.getEncapsulatingSourceSection();
      estimated = true;
    }

    if (section == null || section.getSource() == null) {
      return "<unknown source>";
    } else {
      String sourceName = section.getSource().getName();
      int startLine = section.getStartLine();
      return String.format("%s:%d%s", sourceName, startLine, estimated ? "~" : "");
    }
  }

  /**
   * The execute method when no specialization is possible. This is the most
   * general case, therefore it must be provided by all subclasses.
   */
  public abstract Object executeGeneric(VirtualFrame frame);

  /**
   * Marks this node as being a {@link StandardTags.ExpressionTag} for
   * instrumentation purposes.
   */
  public final void addExpressionTag() {
    hasExpressionTag = true;
  }
}
