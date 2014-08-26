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
import java.util.Arrays;
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
     * <strong>Do not modify the content of this array.</strong>
     */
    private final Object[] content;

    /**
     * The components of the {@code "path"} value found in the node, or {@code null} if none.
     * <strong>Do not modify the content of this array.</strong>
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
     * Maximum number of occurrences allowed for this node.
     * Must not be negative or zero.
     */
    private final int maxOccurs;

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
     * @param nextLine  {@code true} for invoking {@link LineReader#nextLine()} for the first line, or
     *                  {@code false} for continuing the parsing from the current {@link LineReader} content.
     * @param separator The separator between this node and an other occurrence of the same node,
     *                  or {@code null} if unknown.
     */
    TemplateNode(final LineReader parser, boolean nextLine, String separator) throws IOException {
        final List<Object> content = new ArrayList<>();
        String  path         = null;
        Object  defaultValue = null;
        int     valueIndex   = -1;
        int     maxOccurs    = Integer.MAX_VALUE;
        int     level        = 0;
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
                content.set(valueIndex, parser.fullLineWithoutNull());
            } else if (parser.regionMatches("\"multiplicity\"")) {
                final Object n = parser.getValue();
                if (!(n instanceof Number) || (maxOccurs = ((Number) n).intValue()) < 1) {
                    throw new ParseException("Invalid multiplicity: " + n);
                }
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
        this.path               = (path != null) ? parser.sharedPath(path) : null;
        this.defaultValue       = defaultValue;
        this.valueIndex         = valueIndex;
        this.maxOccurs          = maxOccurs;
        this.hasTrailingComma   = parser.hasTrailingComma();
        this.lengthWithoutComma = parser.length();
        this.separator          = separator;
    }

    /**
     * Validate the {@link #path} of this node and all child nodes.
     * This method shall be invoked on the root node after we finished to build the whole tree.
     * This method invokes itself recursively for validating children too.
     */
    final void validatePath(CharSequence[] prefix) throws ParseException {
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
    final int getPathDepth() {
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
     * Returns {@code true} if this node contains a "value" property.
     */
    private boolean isField() {
        return valueIndex >= 0;
    }

    /**
     * Given a newly computed set of values, returns the values accepted by the current node.
     *
     * <p>The current version only ensures that the number of elements is not greater than {@link #maxOccurs}.
     * However if we want to apply a more sophisticated filter in a future version, it could be applied here.</p>
     *
     * @param  values The values fetched from the metadata object.
     * @return The values to write for the current {@code TemplateNode}.
     */
    private Object[] filterNewValues(Object[] values) {
        if (values != null && values.length > maxOccurs) {
            values = Arrays.copyOfRange(values, 0, maxOccurs);
        }
        return values;
    }

    /**
     * Fetches all occurrences of metadata values at the path given by {@link #path}.
     * This method search only the metadata values for this {@code TemplateNode} - it
     * does not perform any search for children {@code TemplateNode}s.
     *
     * @param  metadata   The metadata from where to get the values.
     * @param  pathOffset Index of the first {@link #path} element to use.
     * @return The values (often an array of length 1), or {@code null} if none.
     * @throws ClassCastException if {@code metadata} is not an instance of the expected standard.
     */
    private Object[] getValues(Object metadata, int pathOffset) throws ClassCastException {
        if (metadata == null || path == null) {
            return null;
        }
        Object value;
        do {
            // Fetch the value from the metadata object.
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
                final Class<?> type = standard.asTypeMap(metadata.getClass(),
                        KeyNamePolicy.UML_IDENTIFIER, TypeValuePolicy.PROPERTY_TYPE).get(identifier);
                if (Collection.class.isAssignableFrom(type)) {
                    Object[] values = ((Collection<?>) value).toArray();
                    if (++pathOffset < path.length) {
                        final Object[][] arrays = new Object[values.length][];
                        for (int i=0; i<values.length; i++) {
                            arrays[i] = getValues(values[i], pathOffset);
                        }
                        values = ArraysExt.concatenate(arrays);
                    }
                    return filterNewValues(values);
                }
            }
            /*
             * The value is not a collection. Continue the loop for each components in the path. For example
             * if the path is "identificationInfo.extent.geographicElement.southBoundLatitude", then the loop
             * would be executed for "identificationInfo", then "extent", etc. if all components were singleton.
             */
            metadata = value;
        } while (++pathOffset < path.length);
        return filterNewValues(new Object[] {value});
    }

    /**
     * Builds a tree of values for this node and all children nodes.
     *
     * @param  metadata   The metadata from which to get the values.
     * @param  pathOffset Index of the first {@link #path} element to use.
     * @param  prune      {@code true} for omitting empty nodes.
     * @return The roots of tree nodes creates by this method (not necessarily the root of the whole tree to be
     *         written), or {@code null} if none. The array may contain null elements, which shall be ignored.
     */
    private ValueNode[] createValueTree(final Object metadata, int pathOffset, final boolean prune) throws ParseException {
        ValueNode node;
        if (path == null) {
            /*
             * If this node does not declare any path, we can not get a metadata value for this node.
             * However maybe some chidren may have a path allowing them to fetch metadata values.
             */
            node = null;
            for (final Object line : content) {
                if (line instanceof TemplateNode) {
                    node = addTo(node, ((TemplateNode) line).createValueTree(metadata, pathOffset, prune));
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
                 * If we have a value and this node is a field, store the value.
                 * Otherwise delegate to the child nodes for creating sub-trees.
                 */
                if (isField()) {
                    node = new ValueNode(this, values);
                } else {
                    /*
                     * If we have at least one value and this node may contains children,
                     * we have to invoke recursively this method for the children. Note
                     * that this may result in the creation of more than one "root" node.
                     * This block is the reason why 'createValueTree' needs to return an array.
                     */
                    pathOffset += path.length;
                    final ValueNode[] nodes = new ValueNode[values.length];
                    for (int i=0; i<values.length; i++) {
                        node = null;
                        for (final Object line : content) {
                            if (line instanceof TemplateNode) {
                                node = addTo(node, ((TemplateNode) line).createValueTree(values[i], pathOffset, prune));
                            }
                        }
                        nodes[i] = node;
                    }
                    return nodes;
                }
            } else if (prune) {
                /*
                 * If there is no value and the user asked us to prune empty nodes,
                 * returns 'null' immediately (avoid the creation of an array).
                 */
                return null;
            } else {
                /*
                 * If there is no value, write anyway if the user asked us to not prune empty nodes.
                 * We will format the default values, which may be {@code null}. Note that in the
                 * later case we really want an array of values containing the 'null' element.
                 */
                if (isField()) {
                    node = new ValueNode(this, new Object[] {defaultValue});
                } else {
                    node = null;
                    pathOffset += path.length;
                    for (final Object line : content) {
                        if (line instanceof TemplateNode) {
                            node = addTo(node, ((TemplateNode) line).createValueTree(null, pathOffset, prune));
                        }
                    }
                }
            }
        }
        return new ValueNode[] {node};
    }

    /**
     * Adds the non-null children to the given parent.
     * The parent will be created when first needed.
     *
     * @param  parent   The parent, or {@code null} if not yet created.
     * @param  children The children to add to the parent, or {@code null}.
     * @return The parent, newly created if the given {@code parent} was null.
     */
    private ValueNode addTo(ValueNode parent, final ValueNode[] children) {
        if (children != null) {
            for (final ValueNode child : children) {
                if (child != null) {
                    if (parent == null) {
                        parent = new ValueNode(this, null);
                    }
                    parent.add(child);
                }
            }
        }
        return parent;
    }

    /**
     * Writes the given metadata to the given output using this node as a template.
     *
     * @param  metadata The metadata to write.
     * @param  out      Where to write the JSON file.
     * @param  prune    {@code true} for omitting empty nodes.
     * @param  maxDepth The maximal length of {@link #path}.
     * @throws IOException If an error occurred while writing the JSON file.
     */
    final void write(final Object metadata, final Appendable out, final boolean prune) throws IOException {
        for (final ValueNode root : createValueTree(metadata, 0, prune)) {
            if (root != null) {
                writeTree(root, out, true);
            }
        }
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
    final void toString(final StringBuilder buffer, final int indentation, final int pathOffset, final String label, final Object[] values) {
        buffer.append(CharSequences.spaces(indentation)).append(isField() ? "Field" :  "Node").append('[');
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
