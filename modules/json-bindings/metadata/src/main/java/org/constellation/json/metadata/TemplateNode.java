/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.json.metadata;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import org.apache.sis.metadata.MetadataStandard;
import org.apache.sis.util.CharSequences;


/**
 * An immutable node in the template. This node contains either {@link String}s, to be copied verbatim,
 * or other {@code TemplateNode}s, thus forming a tree.
 *
 * <p>All instances of {@code TemplateNode} shall be immutable in order to allow concurrent use
 * in multi-thread environment.</p>
 *
 * @author Martin Desruisseaux (Geomatys)
 */
final class TemplateNode {
    /**
     * The metadata standard for this node.
     */
    final MetadataStandard standard;

    /**
     * The lines or other nodes contained in this node.
     * Elements in this array are either {@link String} or other {@code TemplateNode}.
     * <strong>Do not modify the content of this array.</strong>
     */
    private final Object[] content;

    /**
     * A subset of {@link #content} containing only the {@code TemplateNode} instances,
     * or {@code null} if none. <strong>Do not modify the content of this array.</strong>
     */
    final TemplateNode[] children;

    /**
     * The components of the {@code "path"} value found in the node, or {@code null} if none.
     * <strong>Do not modify the content of this array.</strong>
     */
    final String[] path;

    /**
     * If there is a path or ignore, that path. Otherwise {@code null}.
     */
    final NumerotedPath ignore;

    /**
     * The value of the {@code "defaultValue"} element found in the node, or {@code null}.
     */
    final Object defaultValue;

    /**
     * Index of the line where to format the value, or -1 if none.
     * The {@code content[valueIndex]} line shall be a {@link String} containing {@code "value:"}.
     */
    private final int valueIndex;

    /**
     * Index of the line where to format the path, or -1 if none.
     * The {@code content[pathIndex]} line shall be a {@link String} containing {@code "path:"}.
     */
    private final int pathIndex;

    /**
     * Maximum number of occurrences allowed for this node.
     * Must not be negative or zero.
     */
    final int maxOccurs;

    /**
     * {@code true} if the last line has a trailing comma, ignoring whitespaces.
     * The comma may be "," alone, or the comma followed by an opening bracket ",{".
     */
    private final boolean hasTrailingComma;

    /**
     * The length of the last line of {@link #content} when we want to omit the trailing comma.
     * This is used when the template contains other nodes after this node, but we do not want
     * to write those nodes because the remaining nodes are empty and pruned.
     */
    private final short lengthWithoutComma;

    /**
     * The separator between this node and an other occurrence of the same node.
     * This field is never {@code null}. Its value is the comma ({@code ','}),
     * sometime followed by an opening bracket if the first line of {@link #content}
     * does not have an opening bracket.
     */
    private final String separator;

    /**
     * Creates a new node for the given lines.
     *
     * @param parser    An iterator over the lines to parse.
     * @param isNextLineRequested {@code true} for invoking {@link LineReader#nextLine()} for the first line, or
     *                  {@code false} for continuing the parsing from the current {@link LineReader} content.
     * @param separator The separator between this node and an other occurrence of the same node,
     *                  or {@code null} if unknown.
     */
    TemplateNode(final LineReader parser, boolean isNextLineRequested, String separator) throws IOException {
        final List<Object>       content  = new ArrayList<>();
        final List<TemplateNode> children = new ArrayList<>();
        String  path         = null;
        String  ignore       = null;
        Object  defaultValue = null;
        int     valueIndex   = -1;
        int     pathIndex    = -1;
        int     maxOccurs    = Integer.MAX_VALUE;
        int     level        = 0;
        while (true) {
            if (isNextLineRequested) {
                content.add(parser.nextLine());
            }
            if (parser.regionMatches(Keywords.PATH)) {
                if (pathIndex >= 0) {
                    throw new ParseException("Duplicated " + Keywords.PATH + '.');
                }
                final Object p = parser.getValue();
                if (p != null) {
                    path = p.toString();
                    pathIndex = content.size() - 1;
                    content.set(pathIndex, parser.fullLineWithoutValue());
                }
            } else if (parser.regionMatches(Keywords.IGNORE)) {
                ignore = (String) parser.getValue();
            } else if (parser.regionMatches(Keywords.DEFAULT_VALUE)) {
                defaultValue = parser.getValue();
            } else if (parser.regionMatches(Keywords.VALUE)) {
                if (valueIndex >= 0) {
                    throw new ParseException("Duplicated " + Keywords.VALUE + '.');
                }
                valueIndex = content.size() - 1;
                content.set(valueIndex, parser.fullLineWithoutNull());
            } else if (parser.regionMatches(Keywords.MAX_OCCURRENCES)) {
                final Object n = parser.getValue();
                if (!(n instanceof Number) || (maxOccurs = ((Number) n).intValue()) < 1) {
                    throw new ParseException("Invalid multiplicity: " + n);
                }
            } else if (parser.regionMatches(Keywords.CHILDREN)) {
                String childSeparator = null;
                do {
                    final TemplateNode child = new TemplateNode(parser, false, childSeparator);
                    content .add(child);
                    children.add(child);
                    childSeparator = parser.skipComma();
                } while (childSeparator != null);
            }
            /*
             * Increment or decrement the level when we find '{' or '}' character. The loop exits when we reach
             * back the level 0, except if the line was not part of this node (isNextLineRequested == false),
             * because the opening bracket may be on the next line. Remaining lines are ignored.
             */
            level = parser.updateLevel(level);
            if (level == 0 && isNextLineRequested) {
                break;
            }
            isNextLineRequested = parser.isEmpty();
        }
        /*
         * If the separator was not declared (which happen only for the first element of a new array),
         * infers it from whether or not the first non-white line in this node has an opening bracket.
         */
        if (separator == null) {
            separator = ",";
            for (final Object e : content) {
                if (e instanceof TemplateNode) {
                    separator = ",{";
                    break;
                }
                final String line = (String) e;
                final int i = CharSequences.skipLeadingWhitespaces(line, 0, line.length());
                if (i < line.length()) {
                    if (line.charAt(i) != '{') {
                        separator = ",{";
                    }
                    break;
                }
            }
        }
        /*
         * Finally store all the information we just computed.
         */
        this.standard           = parser.standard;
        this.content            = content.toArray();
        this.children           = children.isEmpty() ? null : children.toArray(new TemplateNode[children.size()]);
        this.path               = (path != null) ? parser.sharedPath(path) : null;
        this.ignore             = (ignore != null) ? new NumerotedPath(this.path, ignore) : null;
        this.defaultValue       = defaultValue;
        this.valueIndex         = valueIndex;
        this.pathIndex          = pathIndex;
        this.maxOccurs          = maxOccurs;
        this.hasTrailingComma   = parser.hasTrailingComma();
        this.lengthWithoutComma = parser.length();
        this.separator          = separator;
    }

    /**
     * Returns {@code true} if the given path starts with the given prefix.
     * A null {@code prefix} is considered synonymous to an empty prefix.
     */
    static boolean startsWith(final CharSequence[] path, final CharSequence[] prefix) {
        if (prefix != null) {
            if (prefix.length > path.length) {
                return false;
            }
            for (int i=0; i<prefix.length; i++) {
                if (!path[i].equals(prefix[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Validates the {@link #path} of this node and all child nodes.
     * This method shall be invoked on the root node after we finished to build the whole tree.
     * This method invokes itself recursively for validating children too.
     *
     * @return The maximal length of {@code path} arrays found in the tree.
     */
    final int validatePath(CharSequence[] prefix) throws ParseException {
        int depth = 0;
        if (path != null) {
            if (!startsWith(path, prefix)) {
                final StringBuilder buffer = new StringBuilder("Path ");
                appendPath(0, buffer);
                throw new ParseException(buffer.append(" is inconsistent with parent.").toString());
            }
            prefix = path;
            depth = prefix.length;
        }
        if (children != null) {
            for (final TemplateNode template : children) {
                final int c = template.validatePath(prefix);
                if (c > depth) {
                    depth = c;
                }
            }
        }
        return depth;
    }

    /**
     * Appends the path (including quotes) for this node in the given buffer.
     * Callers must ensure that {@link #path} is non-null before to invoke this method.
     */
    final void appendPath(final int pathOffset, final StringBuilder appendTo) {
        appendTo.append('"');
        if (pathOffset != 0) {
            appendTo.append('(');
        }
        for (int i=0; i<path.length; i++) {
            if (i != 0) {
                if (i == pathOffset) {
                    appendTo.append(')');
                }
                appendTo.append(Keywords.PATH_SEPARATOR);
            }
            appendTo.append(path[i]);
        }
        appendTo.append('"');
    }

    /**
     * Returns {@code true} if this node contains a "value" property.
     */
    final boolean isField() {
        return valueIndex >= 0;
    }

    /**
     * Writes the given metadata to the given output using this node as a template.
     *
     * @param  metadata The metadata to write.
     * @param  out      Where to write the JSON file.
     * @param  prune    {@code true} for omitting empty nodes.
     * @param  maxDepth The maximal length of {@link #path} in this node and child nodes.
     * @throws IOException If an error occurred while writing the JSON file.
     */
    final void write(final Object metadata, final Appendable out, final boolean prune, final int maxDepth) throws IOException {
        final TemplateApplicator f = new TemplateApplicator(prune, maxDepth);
        for (final ValueNode root : f.createValueTree(this, metadata)) {
            writeTree(root, out, true);
        }
    }

    /**
     * Writes the given metadata to the given output using this node as a template.
     *
     * @param  metadata The metadata to write.
     * @param  node     The node and its list of children computed by {@link TemplateApplicator#createValueTree}.
     * @param  out      Where to write the JSON file.
     * @throws IOException If an error occurred while writing the JSON file.
     */
    private void writeTree(final ValueNode node, final Appendable out, final boolean isLastNode) throws IOException {
        for (int i=0; i<content.length; i++) {
            final Object line = content[i];
            if (line instanceof TemplateNode) {
                /*
                 * If the "line" is actually a "superblock", "block" or "field", we may have many occurrences of
                 * that node. Formats an occurrence for each value.  Note that if we have more than one occurrence
                 * but the node was used to be the last array element, we will need to append a separator. This is
                 * usually "," but can also be ",{" if the node has no opening bracket.
                 */
                final int n = node.size();
                for (int j=0; j<n; j++) {
                    final ValueNode child = node.get(j);
                    if (child.template == line) {
                        child.template.writeTree(child, out, (j+1) == n);
                    }
                }
            } else {
                /*
                 * If the line is an ordinary line, write that line with the following special cases:
                 *
                 * - Append the value if the line is the one which is expected to contain the value.
                 * - Append "," or ",{" if there is no separator while we are expecting to write more nodes.
                 * - Or (opposite of above), remove separator if present while we formatted the last node.
                 */
                final boolean isLastLine = (i+1) == content.length;
                if (isLastLine && isLastNode) {
                    out.append((String) line, 0, lengthWithoutComma);
                } else {
                    out.append((String) line);
                }
                if (i == pathIndex) { // Implies non-null path.
                    node.formatPath(out, 0);
                    out.append(',');
                }
                if (i == valueIndex) {
                    node.formatValue(out);
                }
                if (isLastLine && !isLastNode && !hasTrailingComma) {
                    out.append(separator);
                }
                out.append('\n');
            }
        }
    }

    /**
     * Return the content, for debugging purpose only.
     */
    @Override
    public String toString() {
        if (content == null) {
            return "<init>"; // When invoked from the constructor by IDE debugger.
        }
        final StringBuilder buffer = new StringBuilder(4000);
        toString(buffer, 0, 0);
        return buffer.append('\n').toString();
    }

    /**
     * Implementation of {@link #toString()} to be invoked recursively by children.
     * This method does not add the final EOL character - it is caller responsibility to append it.
     */
    private void toString(final StringBuilder buffer, final int indentation, int pathOffset) {
        buffer.append(CharSequences.spaces(indentation)).append(isField() ? "Field" : "Node").append('[');
        if (path != null) {
            appendPath(pathOffset, buffer.append("path:"));
            pathOffset += path.length; // For the iteration over children.
        }
        if (defaultValue != null) {
            if (path != null) buffer.append(", ");
            buffer.append("defaultValue:\"").append(defaultValue).append('"');
        }
        buffer.append(']');
        if (children != null) {
            for (final TemplateNode template : children) {
                template.toString(buffer.append('\n'), indentation + 4, pathOffset);
            }
        }
        if (hasTrailingComma) {
            if (children != null) {
                buffer.append('\n').append(CharSequences.spaces(indentation));
            }
            buffer.append(',');
        }
    }
}
