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

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opengis.util.FactoryException;
import org.apache.sis.metadata.AbstractMetadata;
import org.apache.sis.metadata.MetadataStandard;
import org.apache.sis.util.CharSequences;
import org.apache.sis.util.logging.Logging;
import org.constellation.json.metadata.binding.*;


/**
 * Reads a JSON file containing the values provided by end user from the web interfaces,
 * and store those values in a metadata object.
 *
 * @author Martin Desruisseaux (Geomatys)
 */
final class FormReader {
    /**
     * @deprecated "Log and continue" is not appropriate since the user can not know that his data is lost
     * (especially when the client is a web browser and the logging occurs on the server). This field needs
     * to be deleted and the logging replaced by an exception or any other mechanism reporting error to the
     * user.
     */
    private final static Logger LOGGER = Logging.getLogger(FormReader.class);

    /**
     * For iterating over the lines of the JSON file to parse.
     */
    private final LineReader parser;

    /**
     * {@code true} for invoking {@link LineReader#nextLine()} for the first line, or
     * {@code false} for continuing the parsing from the current {@link LineReader} content.
     */
    private boolean isNextLineRequested;

    /**
     * Indices of path elements. The 0 value means that the corresponding path element does
     * not specify any index.
     */
    private final int[] indices;

    /**
     * {@code true} for skipping {@code null} values instead than storing null in the metadata object.
     * See the {@code skipNulls} argument of {@link Template#read(Iterable, Object, boolean)}
     * for more information.
     */
    private final boolean skipNulls;

    /**
     * The values for each path found in the file. Values can only be instances of {@link String},
     * {@link Number} or {@code List<Object>}.
     *
     * If the list of legal types is modified, consider revisiting {@link MetadataUpdater#value}.
     */
    private final SortedMap<NumerotedPath,Object> values;

    /**
     * The GeoAPI interfaces substitution.
     * For example {@code Identification} is typically interpreted as {@code DataIdentification}.
     */
    private final Map<Class<?>, Class<?>> specialized;

    /**
     * Creates a new form reader.
     */
    FormReader(final LineReader parser, final int maxDepth, final boolean skipNulls,
            final Map<Class<?>, Class<?>> specialized)
    {
        this.parser      = parser;
        this.indices     = new int[maxDepth];
        this.skipNulls   = skipNulls;
        this.specialized = specialized;
        isNextLineRequested = true;
        values = new TreeMap<>();
    }

    /**
     * Parses the given JSON lines.
     *
     * <p>This method invokes itself recursively.</p>
     *
     * @param  parent The parent path, or {@code null} if none.
     * @throws IOException if an error occurred while parsing.
     */
    final void read(final String[] parent) throws IOException {
        String[] path = null;
        int level = 0;
        while (true) {
            if (isNextLineRequested) {
                parser.nextLine();
            }
            if (parser.regionMatches(Keywords.PATH)) {
                path = parsePath(parent, (String) parser.getValue());
            } else if (parser.regionMatches(Keywords.VALUE)) {
                addValue(path, parser.getValue());
            } else if (parser.regionMatches(Keywords.CHILDREN)) {
                do {
                    isNextLineRequested = false;
                    read(path);
                } while (parser.skipComma() != null);
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
    }

    /**
     * Parses the given JSON object.
     *
     * @throws ParseException if an error occurred while parsing.
     */
    final void read(final RootObj root) throws ParseException {
        for (final SuperBlockObj sb : root.getRoot().getChildren()) {
            for (final BlockObj bc : sb.getSuperblock().getChildren()) {
                final Block block = bc.getBlock();
                final String[] parent = parsePath(null, block.getPath());
                for (final ComponentObj fc : block.getChildren()) {
                    final Field field = ((FieldObj)fc).getField();
                    final String[] path = parsePath(parent, field.getPath());
                    addValue(path, field.value);
                }
            }
        }
    }

    /**
     * Parses the given path and returns its component.
     * This method returns the path components and stores the index values in the {@link #indices} array.
     *
     * @param  parent The component of the parent path, or {@code null} if none.
     * @param  path   The path to parse.
     * @return The path components.
     */
    private String[] parsePath(final String[] parent, final String path) throws ParseException {
        if (path == null) {
            return null;
        }
        final String[] components = (String[]) CharSequences.split(path, Keywords.PATH_SEPARATOR);
        if (components.length > indices.length) {
            // TODO: "log and continue" is not appropriate here, since the user can not know that his data is lost.
            LOGGER.log(Level.WARNING, "Error parsing path:{0}", path);
        }
        Arrays.fill(indices, 0, components.length, 0);
        NumberFormatException cause = null;
        for (int i=0; i<components.length; i++) {
            String p = components[i];
            final int lower = p.lastIndexOf('[');
            if (lower >= 0) {
                final int upper = p.lastIndexOf(']');
                boolean failed = (upper <= lower);
                if (!failed) try {
                    indices[i] = Integer.parseInt(p.substring(lower + 1, upper));
                } catch (NumberFormatException e) {
                    cause = e;
                    failed = true;
                }
                if (failed) {
                    throw new ParseException(formatPath("Illegal path syntax: \"", components, "\"."), cause);
                }
                components[i] = p.substring(0, lower); // Set only in case of success.
            }
        }
        if (!TemplateNode.startsWith(components, parent)) {
            throw new ParseException(formatPath("Path \"", components, "\" is inconsistent with parent."));
        }
        return components;
    }

    /**
     * Adds the given metadata value to the {@link #values} map.
     */
    private void addValue(final String[] path, Object value) throws ParseException {
        if (path == null) {
            throw new ParseException("Missing path for value: " + value);
        }
        if (value instanceof CharSequence && CharSequences.skipTrailingWhitespaces(
                (CharSequence) value, 0, ((CharSequence) value).length()) <= 0)
        {
            value = null;
        }
        if (value != null || !skipNulls) {
            final NumerotedPath key = new NumerotedPath(path, indices);
            key.ignoreLastIndex();
            if (key.isMultiOccurrenceAllowed()) {
                @SuppressWarnings("unchecked") // 'values' javadoc.
                List<Object> list = (List<Object>) values.get(key);
                if (list == null) {
                    list = new ArrayList<>(2);
                    if (values.put(key, list) != null) {
                        throw new ConcurrentModificationException();
                    }
                }
                if (value != null) { // 'null' can mean an empty list, but not null element in the list.
                    list.add(value);
                }
            } else if (values.put(key, value) != null) {
                throw new ParseException(formatPath("Path \"", path, "\" is repeated twice."));
            }
        }
    }

    /**
     * Writes the content of the {@link #values} map in the given metadata object.
     */
    final void writeToMetadata(final MetadataStandard standard, final Object destination) throws ParseException {
        if (!values.isEmpty()) {
            final MetadataUpdater updater = new MetadataUpdater(standard, values, specialized);
            try {
                updater.update(null, destination);
            } catch (IllegalArgumentException | ClassCastException | FactoryException e) {
                System.arraycopy(updater.np.indices, 0, indices, 0, updater.np.indices.length);
                throw new ParseException(formatPath("Can not store value at path \"", updater.np.path, "\"."), e);
            }
        }
        if (destination instanceof AbstractMetadata) {
            ((AbstractMetadata) destination).prune();
        }
    }

    /**
     * Formats an error message with the given path.
     */
    private String formatPath(final String before, final String[] path, final String after) {
        final StringBuilder buffer = new StringBuilder(before);
        try {
            NumerotedPath.formatPath(buffer, path, 0, indices);
        } catch (IOException e) {
            throw new AssertionError(e); // Should never happen, since we are writting to a StringBuilder.
        }
        return buffer.append(after).toString();
    }
}
