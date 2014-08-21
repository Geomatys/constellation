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
import java.util.Collection;
import java.io.IOException;
import org.apache.sis.metadata.KeyNamePolicy;
import org.apache.sis.metadata.TypeValuePolicy;
import org.apache.sis.metadata.ValueExistencePolicy;
import org.apache.sis.metadata.MetadataStandard;
import org.apache.sis.util.ArraysExt;
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
    private final MetadataStandard standard;

    /**
     * The lines or other nodes contained in this node.
     * Elements in this array are either {@link String} or other {@code TemplateNode}.
     */
    private final Object[] content;

    /**
     * The components of the {@code "path"} value found in the node, or {@code null} if none.
     */
    private final String[] path;

    /**
     * The value of the {@code "defaultValue"} element found in the node, or {@code null}.
     */
    private final Object defaultValue;

    /**
     * Index of the line where to format the value, or -1 if none.
     * The {@code content[valueIndex]} line shall be a {@link String} containing {@code "value:"}.
     */
    private final int valueIndex;

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
     * @param nextLine  {@code true} for invoking {@link Parser#nextLine()} for the first line, or
     *                  {@code false} for continuing the parsing from the current {@link Parser} content.
     * @param separator The separator between this node and an other occurrence of the same node,
     *                  or {@code null} if unknown.
     */
    TemplateNode(final Parser parser, boolean nextLine, String separator) throws IOException {
        final List<Object> content = new ArrayList<>();
        String  path         = null;
        Object  defaultValue = null;
        int     valueIndex   =   -1;
        int     level        =    0;
        while (true) {
            if (nextLine) {
                content.add(parser.nextLine());
            }
            if (parser.regionMatches("\"path\"")) {
                final Object p = parser.getValue();
                if (p != null) path = p.toString();
            } else if (parser.regionMatches("\"defaultValue\"")) {
                defaultValue = parser.getValue();
            } else if (parser.regionMatches("\"value\"")) {
                valueIndex = content.size() - 1;
                content.set(valueIndex, parser.currentLineWithoutNull());
            } else if (parser.regionMatches("\"content\"")) {
                String childSeparator = null;
                do {
                    content.add(new TemplateNode(parser, false, childSeparator));
                    childSeparator = parser.skipComma();
                }
                while (childSeparator != null);
            }
            /*
             * Increment or decrement the level when we find '{' or '}' character. The loop exits when we reach
             * back the level 0, except if the line was not part of this node (nextLine == false), because the
             * opening bracket may be on the next line. Remaining lines are ignored.
             */
            level = parser.updateLevel(level);
            if (level == 0 && nextLine) {
                break;
            }
            nextLine = parser.isEmpty();
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
        this.path               = (path != null) ? split(path) : null;
        this.defaultValue       = defaultValue;
        this.valueIndex         = valueIndex;
        this.hasTrailingComma   = parser.hasTrailingComma();
        this.lengthWithoutComma = parser.length();
        this.separator          = separator;
    }

    /**
     * Split the given path and {@linkplain String#intern() internalize} the components.
     * We internalize the components because they usually already exists elsewhere in the JVM,
     * as field names or annotation values.
     */
    private static String[] split(final String path) {
        final CharSequence[] c = CharSequences.split(path, '.');
        final String[] cs = new String[c.length];
        for (int i=0; i<c.length; i++) {
            cs[i] = c[i].toString().intern();
        }
        return cs;
    }

    /**
     * Validate the {@link #path} of this node and all child nodes.
     * This method shall be invoked only on the root node after we finished to build the whole tree.
     */
    void validatePath(CharSequence[] prefix) throws ParseException {
        if (path != null) {
            if (prefix != null) {
                for (int i=0; i<prefix.length; i++) {
                    if (i >= path.length - 1 || !path[i].equals(prefix[i])) {
                        final StringBuilder buffer = new StringBuilder("Path \"");
                        appendPath(0, buffer);
                        throw new ParseException(buffer.append("\" is inconsistent with parent.").toString());
                    }
                }
            }
            prefix = path;
        }
        for (final Object line : content) {
            if (line instanceof TemplateNode) {
                ((TemplateNode) line).validatePath(prefix);
            }
        }
    }

    /**
     * Returns the depth of {@link #path}.
     */
    int getPathDepth() {
        return (path != null) ? path.length : 0;
    }

    /**
     * Appends the path for this node in the given buffer.
     */
    private void appendPath(final int pathOffset, final StringBuilder appendTo) {
        if (pathOffset != 0) {
            appendTo.append('(');
        }
        for (int i=0; i<path.length; i++) {
            if (i != 0) {
                if (i == pathOffset) {
                    appendTo.append(')');
                }
                appendTo.append('.');
            }
            appendTo.append(path[i]);
        }
    }

    /**
     * Fetches all occurrences of metadata values at the path given by {@link #path}.
     * This method search only the metadata values for this {@code TemplateNode} - it
     * does not perform any search for children {@code TemplateNode}s.
     *
     * @param  metadata   The metadata from where to get the values.
     * @param  pathOffset Index of the first {@link #path} element to use.
     * @return The values (often an array of length 1), or {@code null} if none.
     */
    private Object[] getValues(Object metadata, int pathOffset) throws ClassCastException {
        if (path == null) {
            return null;
        }
        Object value;
        do {
            final String identifier = path[pathOffset];
            value = standard.asValueMap(metadata, KeyNamePolicy.UML_IDENTIFIER, ValueExistencePolicy.NON_EMPTY).get(identifier);
            if (value == null) {
                return null;
            }
            /*
             * Verify if the value is a collection. We do not rely on (value instanceof Collection)
             * only because it may not be reliable if the value implements more than one interface.
             * Instead, we rely on the method contract.
             */
            if (value instanceof Collection<?>) {
                final Class<?> type = standard.asTypeMap(metadata.getClass(), KeyNamePolicy.UML_IDENTIFIER, TypeValuePolicy.PROPERTY_TYPE).get(identifier);
                if (Collection.class.isAssignableFrom(type)) {
                    Object[] values = ((Collection<?>) value).toArray();
                    if (++pathOffset < path.length) {
                        final Object[][] arrays = new Object[values.length][];
                        for (int i=0; i<values.length; i++) {
                            arrays[i] = getValues(values[i], pathOffset);
                        }
                        values = ArraysExt.concatenate(arrays);
                    }
                    return values;
                }
            }
            metadata = value;
        } while (++pathOffset < path.length);
        return new Object[] {value};
    }

    /**
     * Builds a tree of values for this node and all children nodes.
     *
     * @param metadata   The root metadata from which to get the values.
     * @param pathOffset Index of the first {@link #path} element to use.
     */
    private ValueNode createValueTree(final Object metadata, int pathOffset) throws ParseException {
        ValueNode root = null;
        /*
         * If this node does not declare any path, we can not get a metadata value for this node.
         * However maybe some chidren may have a path allowing them to fetch metadata values.
         */
        if (path == null) {
            for (final Object line : content) {
                if (line instanceof TemplateNode) {
                    root = addTo(root, ((TemplateNode) line).createValueTree(metadata, pathOffset));
                }
            }
        } else {
            /*
             * If this node declares a path, then get the values for this node. The values may be other
             * metadata objects, in which case we will need to invoke this method recursively for them.
             */
            final Object[] values;
            try {
                values = getValues(metadata, pathOffset);
            } catch (ClassCastException e) {
                final StringBuilder buffer = new StringBuilder("Illegal path: \"");
                appendPath(pathOffset, buffer);
                throw new ParseException(buffer.append("\".").toString(), e);
            }
            if (values != null) {
                /*
                 * If this node is a field, store the value. Otherwise delegate to the child nodes
                 * for creating sub-tree.
                 */
                if (valueIndex >= 0) {
                    root = new ValueNode(this, values);
                } else {
                    pathOffset += path.length;
                    for (final Object line : content) {
                        if (line instanceof TemplateNode) {
                            for (final Object value : values) {
                                root = addTo(root, ((TemplateNode) line).createValueTree(value, pathOffset));
                            }
                        }
                    }
                }
            }
        }
        return root;
    }

    /**
     * If the given child is non-null, adds it to the given parent.
     * The parent will be created when first needed.
     *
     * @param  parent The parent, or {@code null} if not yet created.
     * @param  child  The child to add to the parent, or {@code null}.
     * @return The parent, newly created if the given {@code parent} was null.
     */
    private ValueNode addTo(ValueNode parent, final ValueNode child) {
        if (child != null) {
            if (parent == null) {
                parent = new ValueNode(this, null);
            }
            parent.add(child);
        }
        return parent;
    }

    /**
     * Writes the given metadata to the given output using this node as a template.
     *
     * @param  metadata The metadata to write.
     * @param  out Where to write the JSON file.
     * @throws IOException If an error occurred while writing the JSON file.
     */
    void write(final Object metadata, final Appendable out) throws IOException {
        ValueNode root = createValueTree(metadata, 0);
        if (root == null) {
            root = new ValueNode(this, null);
        }
        writeTree(root, out, true);
    }

    /**
     * Writes the tree for the given node. The given node must have {@code this} has its template.
     * This method handles multi-occurrences of field values only.  Multi-occurrences of metadata
     * shall be handled by the caller.
     */
    private void writeTree(final ValueNode node, final Appendable out, final boolean isLastNode) throws IOException {
        assert node.template == this;
        final Object[] values = node.values;
        if (values == null) {
            /*
             * The node is "root", "superblock" or "block": write the node content and all its children.
             * Note that the caller may invoke this method many time for the same TemplateNode if there
             * is multi-occurrences. The multi-occurrence is not handled by this method.
             */
            writeTree(node, out, isLastNode, null);
        } else {
            /*
             * The node is "field" and may contain an arbitrary amount of values. Repeats the whole node
             * for each value. Note that multi-occurrences of values (handled here) is not the same than
             * multi-occurrences of metadata (handled by the caller).
             */
            for (int i=0; i < values.length; i++) {
                writeTree(node, out, isLastNode & ((i+1) == values.length), values[i]);
            }
        }
    }

    /**
     * Writes the given metadata to the given output using this node as a template.
     *
     * @param  metadata The metadata to write.
     * @param  children The list of children computed by {@link #createValueTree(Object, int)}.
     * @param  out      Where to write the JSON file.
     * @throws IOException If an error occurred while writing the JSON file.
     */
    private void writeTree(final ValueNode children, final Appendable out,
            final boolean isLastNode, final Object value) throws IOException
    {
        for (int i=0; i<content.length; i++) {
            final Object line = content[i];
            if (line instanceof TemplateNode) {
                /*
                 * If the "line" is actually a "superblock", "block" or "field", we may have many occurrences of
                 * that node. Formats an occurrence for each value.  Note that if we have more than one occurrence
                 * but the node was used to be the last array element, we will need to append a separator. This is
                 * usually "," but can also be ",{" if the node has no opening bracket.
                 */
                final int n = children.size();
                for (int j=0; j<n; j++) {
                    final ValueNode child = children.get(j);
                    if (child.template == line) {
                        ((TemplateNode) line).writeTree(child, out, (j+1) == n);
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
                if (i == valueIndex) {
                    ValueNode.format(value, out);
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
     * This method does not add the EOL character - it is caller responsibility to append it.
     */
    private void toString(final StringBuilder buffer, final int indentation, int pathOffset) {
        toString(buffer, indentation, pathOffset, "defaultValue",
                (defaultValue != null) ? new Object[] {defaultValue} : null);
        pathOffset += getPathDepth();
        boolean hasChildren = false;
        for (final Object line : content) {
            if (line instanceof TemplateNode) {
                ((TemplateNode) line).toString(buffer.append('\n'), indentation + 4, pathOffset);
                hasChildren = true;
            }
        }
        if (hasTrailingComma) {
            if (hasChildren) {
                buffer.append('\n').append(CharSequences.spaces(indentation));
            }
            buffer.append(',');
        }
    }

    /**
     * Partial implementation of {@link #toString(StringBuilder, int, int)} to be shared by {@link ValueNode}.
     * This method does not add the EOL character - it is caller responsibility to append it.
     */
    void toString(final StringBuilder buffer, final int indentation, final int pathOffset, final String label, final Object[] values) {
        buffer.append(CharSequences.spaces(indentation)).append(valueIndex < 0 ? "Node" : "Field").append('[');
        if (path != null) {
            appendPath(pathOffset, buffer.append("path:\""));
            buffer.append('"');
        }
        if (values != null) {
            if (path != null) {
                buffer.append(", ");
            }
            buffer.append(label).append(':');
            for (int i=0; i<values.length; i++) {
                if (i != 0) buffer.append(", ");
                buffer.append('"').append(values[i]).append('"');
            }
        }
        buffer.append(']');
    }
}
