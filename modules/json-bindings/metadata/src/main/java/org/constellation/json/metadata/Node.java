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

import java.util.Date;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.ConcurrentModificationException;
import java.io.IOException;
import org.opengis.util.Enumerated;
import org.apache.sis.measure.Angle;
import org.apache.sis.metadata.KeyNamePolicy;
import org.apache.sis.metadata.TypeValuePolicy;
import org.apache.sis.metadata.ValueExistencePolicy;
import org.apache.sis.metadata.MetadataStandard;
import org.apache.sis.util.ArraysExt;
import org.apache.sis.util.CharSequences;
import org.apache.sis.util.iso.Types;


/**
 * A node in the template. This node contains either {@link String}, to be copied verbatim,
 * or other {@code Node}, thus forming a tree.
 *
 * <p>All instances of {@code Node} shall be immutable in order to allow concurrent use
 * in multi-thread environment.</p>
 *
 * @author Martin Desruisseaux (Geomatys)
 */
final class Node {
    /**
     * The metadata standard for this node.
     */
    private final MetadataStandard standard;

    /**
     * The lines or other nodes contained in this node.
     */
    private final Object[] content;

    /**
     * The value of the {@code path} element found in the node, or {@code null}.
     */
    private final CharSequence[] path;

    /**
     * The value of the {@code defaultValue} element found in the node, or {@code null}.
     */
    private final Object defaultValue;

    /**
     * Index of the line where to format the value, or -1 if none.
     */
    private final int valueIndex;

    /**
     * Creates a new node for the given lines.
     *
     * @param parser An iterator over the lines to parse.
     */
    Node(final Parser parser) throws IOException {
        final List<Object> content = new ArrayList<>();
        String path         = null;
        Object defaultValue = null;
        int    valueIndex   = -1;
        int    level        =  0;
        do {
            /*
             * Increment or decrement the level when we find '{' or '}' character.
             * The loop exit when we reach back the level 0. Remaining lines are ignored.
             */
            final String line = parser.nextLine();
            for (int i=parser.position; i<parser.length; i++) {
                switch (line.charAt(i)) {
                    case '{': level++; break;
                    case '}': level--; break;
                }
            }
            /*
             * Process the "path", "defaultValue", "value" and "content" keys.
             */
            content.add(line);
            if (parser.regionMatches("\"path\"")) {
                path = parser.getValue();
            } else if (parser.regionMatches("\"defaultValue\"")) {
                defaultValue = parser.getValue();
            } else if (parser.regionMatches("\"value\"")) {
                if (parser.hasTrailingComa)    throw new ParseException("Value shall be the last entry in a field.");
                if (parser.getValue() != null) throw new ParseException("Value of \"value\" shall be null.");
                valueIndex = content.size() - 1;
                content.set(valueIndex, line.substring(0, parser.length - 4)); // Skip the "null" letters.
            } else if (parser.regionMatches("\"content\"")) {
                do content.add(new Node(parser));
                while (parser.hasTrailingComa);
            }
        } while (level != 0);
        this.standard     = parser.standard;
        this.content      = content.toArray();
        this.path         = (path != null) ? CharSequences.split(path, '.') : null;
        this.defaultValue = defaultValue;
        this.valueIndex   = valueIndex;
    }

    /**
     * Returns the path for this node, or {@code null} if none.
     */
    private String getPath(final int pathOffset) {
        final StringBuilder buffer = new StringBuilder();
        if (pathOffset != 0) {
            buffer.append('(');
        }
        for (int i=0; i<path.length; i++) {
            if (i == pathOffset) buffer.append(')');
            if (i != 0)          buffer.append('.');
            buffer.append(path[i]);
        }
        return (path != null) ? CharSequences.toString(Arrays.asList(path), ".") : null;
    }

    /**
     * Fetches all occurrences of metadata values at the path given by {@link #path}.
     * This method search only the metadata values for this {@code Node} - it does not
     * perform any search for children {@code Node}s.
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
            final String identifier = path[pathOffset].toString();
            value = standard.asValueMap(metadata, KeyNamePolicy.UML_IDENTIFIER, ValueExistencePolicy.NON_EMPTY).get(identifier);
            if (value == null) {
                return null;
            }
            /*
             * Verify if the value is a collection. We do not perform a (value instanceof Collection)
             * check because it may not be reliable if the value implements more than one interface.
             * Instead, we rely on the method contract.
             */
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
            metadata = value;
        } while (++pathOffset < path.length);
        return new Object[] {value};
    }

    /**
     * Builds a map of values for this node and all children nodes.
     *
     * @param metadata   The root metadata from which to get the values.
     * @param pathOffset Index of the first {@link #path} element to use.
     * @param addTo      Where to add the values.
     */
    private void getAllValues(final Object metadata, int pathOffset, final Map<Node,Object[]> addTo) throws ParseException {
        /*
         * If this node does not declare any path, we can not get a metadata value for this node.
         * However maybe some chidren have a path allowing them to fetch metadata values.
         */
        if (path == null) {
            for (final Object line : content) {
                if (line instanceof Node) {
                    ((Node) line).getAllValues(metadata, pathOffset, addTo);
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
                throw new ParseException("Illegal path: \"" + getPath(pathOffset) + "\".", e);
            }
            if (values != null) {
                if (valueIndex < 0) {
                    pathOffset += path.length;
                    for (final Object value : values) {
                        getAllValues(value, pathOffset, addTo);
                    }
                } else if (addTo.put(this, values) != null) {
                    throw new ConcurrentModificationException(getPath(pathOffset));
                }
            }
        }
    }

    /**
     * Returns {@code true} if this node or a children of this node contains at least one value.
     *
     * @param  nodeValues A map of all values computed by {@link #getAllValues(Object, int, Map)}.
     * @return {@code true} if this node or a children contains at least one value.
     */
    private boolean containsValue(final Map<Node,Object[]> nodeValues) {
        if (nodeValues.containsKey(this)) {
            return true;
        }
        for (final Object line : content) {
            if (line instanceof Node && ((Node) line).containsValue(nodeValues)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Writes the given metadata to the given output using this node as a template.
     *
     * @param  metadata The metadata to write.
     * @param  out Where to write the JSON file.
     * @throws IOException If an error occurred while writing the JSON file.
     */
    void write(final Object metadata, final Appendable out) throws IOException {
        final Map<Node,Object[]> values = new IdentityHashMap<>();
        getAllValues(metadata, 0, values);
        write(metadata, values, out);
    }

    /**
     * Writes the given metadata to the given output using this node as a template.
     *
     * @param  metadata The metadata to write.
     * @param  nodeValues A map of all values computed by {@link #getAllValues(Object, int, Map)}.
     * @param  out Where to write the JSON file.
     * @throws IOException If an error occurred while writing the JSON file.
     */
    private void write(final Object metadata, final Map<Node,Object[]> nodeValues, final Appendable out) throws IOException {
        if (!containsValue(nodeValues)) {
            return;
        }
        for (int i=0; i<content.length; i++) {
            final Object line = content[i];
            if (line instanceof Node) {
                ((Node) line).write(metadata, nodeValues, out);
            } else {
                out.append((String) line);
                if (i == valueIndex) {
                    final Object[] values = nodeValues.get(this);
                    final Object value = (values != null) ? values[0] : null; // TODO: duplicate "field" for all values.
                    if (value != null) out.append('"');
                    out.append(format(value));
                    if (value != null) out.append('"');
                }
                out.append('\n');
            }
        }
    }

    /**
     * Formats a single value.
     *
     * @param  value The value to format.
     */
    private static String format(final Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof Enumerated) {
            return Types.getStandardName(value.getClass()) + '.' + Types.getCodeName((Enumerated) value);
        } else if (value instanceof Date) {
            return Long.toString(((Date) value).getTime());
        } else if (value instanceof Angle) {
            return Double.toString(((Angle) value).degrees());
        } else {
            return CharSequences.replace(value.toString(), "\"", "\\\"").toString();
        }
    }

    /**
     * Implementation of {@link #toString()} to be invoked recursively for children.
     * This is for debugging purpose only.
     */
    private void toString(final StringBuilder buffer) {
        for (final Object line : content) {
            if (line instanceof Node) {
                ((Node) line).toString(buffer);
            } else {
                buffer.append(line).append('\n');
            }
        }
    }

    /**
     * Return the content, for debugging purpose only.
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder(64000);
        toString(buffer);
        return buffer.toString();
    }
}
