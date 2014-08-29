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

import java.util.Map;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.io.IOException;
import java.nio.charset.Charset;
import org.opengis.metadata.constraint.Constraints;
import org.opengis.metadata.constraint.LegalConstraints;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.metadata.identification.Identification;
import org.opengis.metadata.identification.DataIdentification;
import org.opengis.util.CodeList;
import org.opengis.util.FactoryException;
import org.apache.sis.util.iso.Types;
import org.apache.sis.util.ArraysExt;
import org.apache.sis.util.CharSequences;
import org.apache.sis.metadata.AbstractMetadata;
import org.apache.sis.metadata.KeyNamePolicy;
import org.apache.sis.metadata.TypeValuePolicy;
import org.geotoolkit.metadata.MetadataFactory;
import org.apache.sis.metadata.MetadataStandard;
import org.apache.sis.metadata.ValueExistencePolicy;


/**
 * Reads a JSON file containing the values provided by end user from the web interfaces,
 * and store those values in a metadata object.
 *
 * @author Martin Desruisseaux (Geomatys)
 */
final class FormReader {
    /**
     * The metadata factory to use for creating new instances.
     */
    private static final MetadataFactory FACTORY = new MetadataFactory();

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
     * {@code true} for skipping {@code null} values instead than storing null in the metadata object.
     * See the {@code skipNulls} argument of {@link Template#read(Iterable, AbstractMetadata, boolean)}
     * for more information.
     */
    private final boolean skipNulls;

    /**
     * Creates a new form reader.
     */
    FormReader(final LineReader parser, final boolean skipNulls) {
        this.parser    = parser;
        this.skipNulls = skipNulls;
        isNextLineRequested = true;
    }

    /**
     * Parses the given JSON lines and write the metadata values in the given metadata object.
     *
     * @param  parent The parent path, or {@code null} if none.
     * @param  destination Where to store the metadata values.
     * @throws IOException if an error occurred while parsing.
     */
    final void read(final CharSequence[] parent, final AbstractMetadata destination) throws IOException {
        boolean multiOccurs = false;
        CharSequence[] path = null;
        int pathOffset      = 0;
        int level           = 0;
        while (true) {
            if (isNextLineRequested) {
                parser.nextLine();
            }
            if (parser.regionMatches(Keywords.PATH)) {
                /*
                 * The 'path' is the location in metadata structure where we will store the value.
                 * If the same path is found more than once, we have a multi-occurrence (typically
                 * values to be stored in a collection). Note that this simple algorithm assumes
                 * that multi-occurrences appears in consecutive fields or blocks in the JSON file.
                 * If the path change, and later change again to the original path, this code will
                 * fail to detect that multi-occurrence.
                 */
                final Object p = parser.getValue();
                final CharSequence[] oldPath = path;
                if (p != null) {
                    path = CharSequences.split(p.toString(), Keywords.PATH_SEPARATOR);
                    if (parent != null) {
                        pathOffset = parent.length;
                        for (int i=0; i<pathOffset; i++) {
                            if (i >= path.length || !parent[i].equals(path[i])) {
                                throw new ParseException("Path \"" + toString(path) + "\" is inconsistent with parent.");
                            }
                        }
                    }
                } else {
                    path = null;
                    pathOffset = 0;
                }
                multiOccurs = Arrays.equals(oldPath, path);
            } else if (parser.regionMatches(Keywords.VALUE)) {
                /*
                 * For a value to store in the metadata structure. If this is the first time that we
                 * have a value for the current path, clears all previous value.  If this is not the
                 * first time (multi-occurrence), append the new value to previous one.
                 */
                final Object value = parser.getValue();
                if (path == null) {
                    throw new ParseException("Missing path for value: " + value);
                }
                if (value != null || !skipNulls) try {
                    put(getOrCreateSingleton(destination, path, pathOffset, path.length - 1),
                            path[path.length - 1].toString(), value, multiOccurs);
                } catch (IllegalArgumentException | ClassCastException | FactoryException e) {
                    throw new ParseException("Can not store value at path \"" + toString(path) + "\".", e);
                }
            } else if (parser.regionMatches(Keywords.CHILDREN)) {
                /*
                 * Found a block with children. If there is a path, get the corresponding metadata.
                 * Child values will be relative to that child metadata.
                 */
                AbstractMetadata child = destination;
                if (path != null) {
                    try {
                        child = getOrCreateSingleton(destination, path, pathOffset, path.length);
                    } catch (IllegalArgumentException | ClassCastException | FactoryException e) {
                        throw new ParseException("Can not store value at path \"" + toString(path) + "\".", e);
                    }
                }
                do {
                    isNextLineRequested = false;
                    read(path, child);
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
     * Returns the metadata at the given path if an instance exists, or create a new metadata instance otherwise.
     * Current implementation requires that no more than 1 instance exists, otherwise we have an ambiguity.
     *
     * @param  metadata The metadata in which to search for child metadata.
     * @param  lower    Index of the first {@code path} element to use.
     * @param  upper    Index after the last {@code path} element to use.
     * @throws ClassCastException if {@code metadata} is not an instance of the expected standard.
     */
    private static AbstractMetadata getOrCreateSingleton(AbstractMetadata metadata, final CharSequence[] path,
            final int lower, final int upper) throws ClassCastException, FactoryException, ParseException
    {
        AbstractMetadata child;
        if (lower == upper) {
            child = metadata;
        } else {
            child = null;
            final Object[] values = getValues(metadata.getStandard(), metadata, path, lower, upper);
            if (values != null) {
                for (final Object value : values) {
                    if (value instanceof AbstractMetadata) {
                        if (child != null) {
                            throw new ParseException("Expected a single element at path \"" +
                                    CharSequences.toString(Arrays.asList(path).subList(lower, upper),
                                            String.valueOf(Keywords.PATH_SEPARATOR)) + "\".");
                        }
                        child = (AbstractMetadata) value;
                    }
                }
            }
        }
        if (child == null) {
            metadata = getOrCreateSingleton(metadata, path, lower, upper - 1);
            final String identifier = path[upper - 1].toString();
            Class<?> type = getType(metadata, identifier);
            if (type == Identification.class)   type = DataIdentification.class; // TODO: needs a more generic mechanism.
            if (type == GeographicExtent.class) type = GeographicBoundingBox.class;
            if (type == Constraints.class)      type = LegalConstraints.class;
            child = (AbstractMetadata) FACTORY.create(type, Collections.<String,Object>emptyMap());
            metadata.asMap().put(identifier, child);
        }
        return child;
    }

    /**
     * Fetches all occurrences of metadata values at the given path.
     *
     * @param  metadata   The metadata from where to get the values.
     * @param  pathOffset Index of the first {@code path} element to use.
     * @param  upper      Index after the last {@code path} element to use.
     * @return The values (often an array of length 1), or {@code null} if none.
     * @throws ClassCastException if {@code metadata} is not an instance of the expected standard.
     */
    private static Object[] getValues(final MetadataStandard standard, Object metadata,
            final CharSequence[] path, int pathOffset, final int upper) throws ClassCastException, ParseException
    {
        if (pathOffset >= upper) {
            throw new ParseException("Path is empty.");
        }
        if (metadata == null || path == null) {
            return null;
        }
        Object value;
        do {
            // Fetch the value from the metadata object.
            final CharSequence identifier = path[pathOffset];
            value = standard.asValueMap(metadata, KeyNamePolicy.UML_IDENTIFIER, ValueExistencePolicy.NON_EMPTY).get(identifier);
            if (value == null) {
                return null;
            }
            /*
             * Verify if the value is a collection. We do not rely on (value instanceof Collection)
             * only because it may not be reliable if the value implements more than one interface.
             * Instead, we rely on the method contract.
             */
            if (value instanceof Collection<?> && TemplateApplicator.isCollection(standard, metadata, identifier)) {
                Object[] values = ((Collection<?>) value).toArray();
                if (++pathOffset < upper) {
                    final Object[][] arrays = new Object[values.length][];
                    for (int i=0; i<values.length; i++) {
                        arrays[i] = getValues(standard, values[i], path, pathOffset, upper);
                    }
                    values = ArraysExt.concatenate(arrays);
                }
                return values;
            }
            /*
             * The value is not a collection. Continue the loop for each components in the path. For example
             * if the path is "identificationInfo.extent.geographicElement.southBoundLatitude", then the loop
             * would be executed for "identificationInfo", then "extent", etc. if all components were singleton.
             */
            metadata = value;
        } while (++pathOffset < upper);
        return new Object[] {value};
    }

    /**
     * Puts the given value for the given property in the given metadata object.
     */
    private static void put(final AbstractMetadata metadata, final String identifier, Object value, final boolean multiOccurs) {
        final Map<String,Object> values = metadata.asMap();
        if (!multiOccurs) {
            // TODO
        }
        if (value instanceof CharSequence && ((CharSequence) value).length() == 0) {
            value = null;
        } else if (value != null) {
            final Class<?> type = getType(metadata, identifier);
            /*
             * Note: if (type == null), then the call to 'values.put(identifier, value)' at the end of this
             * method is likely to fail. However that 'put' method provides a more accurate error message.
             */
            if (type != null) {
                final boolean isCodeList = CodeList.class.isAssignableFrom(type);
                if (isCodeList || type == Locale.class || type == Charset.class) {
                    String text = value.toString();
                    text = text.substring(text.indexOf('.') + 1).trim();
                    if (isCodeList) {
                        value = Types.forCodeName(type.asSubclass(CodeList.class), text, false);
                    } else {
                        value = text;
                    }
                }
            } else {
                /*
                 * TODO: HACK!!!!!! The current "profile_inspire_raster.json" file defines properties which
                 * are both in LegalConstraints and SecurityConstraints, but Apache SIS can not implement both
                 * in same time. Arbitrarily discard this SecurityConstraints for now.
                 */
                if (identifier.equals("classification")) {
                    return;
                }
            }
        }
        values.put(identifier, value); // See "multi-occurrences" in AbstractMetadata.asMap() javadoc.
    }

    /**
     * Returns the type of values for the given property in the given metadata.
     */
    private static Class<?> getType(final AbstractMetadata metadata, final String identifier) {
        return metadata.getStandard().asTypeMap(metadata.getClass(),
                KeyNamePolicy.UML_IDENTIFIER, TypeValuePolicy.ELEMENT_TYPE).get(identifier);
    }

    /**
     * Formats the given path.
     */
    private static String toString(final CharSequence[] path) {
        return CharSequences.toString(Arrays.asList(path), String.valueOf(Keywords.PATH_SEPARATOR));
    }
}
