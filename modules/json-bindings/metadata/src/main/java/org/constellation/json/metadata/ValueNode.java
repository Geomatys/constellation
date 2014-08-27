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
import java.util.Locale;
import java.util.ArrayList;
import java.util.Collection;
import java.util.MissingResourceException;
import java.io.IOException;
import java.nio.charset.Charset;
import org.opengis.util.Enumerated;
import org.apache.sis.measure.Angle;
import org.apache.sis.util.iso.Types;
import org.apache.sis.util.ArraysExt;
import org.apache.sis.util.CharSequences;
import org.apache.sis.metadata.MetadataStandard;
import org.apache.sis.metadata.KeyNamePolicy;
import org.apache.sis.metadata.TypeValuePolicy;
import org.apache.sis.metadata.ValueExistencePolicy;


/**
 * A node containing both a {@link TemplateNode} and its associated value.
 * This node extends {@code ArrayList} for opportunist reasons only.
 * The list elements are children.
 *
 * @author Martin Desruisseaux (Geomatys)
 */
@SuppressWarnings("serial")
final class ValueNode extends ArrayList<ValueNode> {
    /**
     * The template for which this node contains a value.
     */
    final TemplateNode template;

    /**
     * The values associated to this node, or {@code null}.
     */
    final Object[] values;

    /**
     * Creates a new node for the given metadata.
     *
     * @param template The template to apply.
     * @param values   The values associated to this node, or {@code null}.
     */
    ValueNode(final TemplateNode template, final Object[] values) {
        this.template = template;
        this.values   = values;
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
    static Object[] getValues(final MetadataStandard standard, Object metadata,
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
            if (value instanceof Collection<?>) {
                final Class<?> type = standard.asTypeMap(metadata.getClass(),
                        KeyNamePolicy.UML_IDENTIFIER, TypeValuePolicy.PROPERTY_TYPE).get(identifier);
                if (Collection.class.isAssignableFrom(type)) {
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
     * Formats a single value.
     *
     * @param value The value to format.
     */
    static void format(final Object value, final Appendable out) throws IOException {
        final String p;
        if (value == null) {
            p = null;
        } else if (value instanceof Number) {
            p = value.toString();
        } else if (value instanceof Date) {
            p = Long.toString(((Date) value).getTime());
        } else if (value instanceof Angle) {
            p = Double.toString(((Angle) value).degrees());
        } else {
            /*
             * Above were unquoted cases. Below are texts to quote.
             */
            out.append('"');
            if (value instanceof Enumerated) {
                out.append(Types.getStandardName(value.getClass())).append('.')
                   .append(Types.getCodeName((Enumerated) value));
            } else if (value instanceof Locale) {
                String language;
                try {
                    language = ((Locale) value).getISO3Language();
                } catch (MissingResourceException e) {
                    language = ((Locale) value).getLanguage();
                }
                out.append("LanguageCode.").append(language);
            } else if (value instanceof Charset) {
                out.append(((Charset) value).name());
            } else {
                out.append(CharSequences.replace(value.toString(), "\"", "\\\"").toString());
            }
            out.append('"');
            return;
        }
        out.append(p);
    }

    /**
     * Returns a string representation for debugging purpose only.
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder(60);
        toString(buffer, 0, 0);
        return buffer.toString();
    }

    /**
     * Implementation of {@link #toString()} to be invoked recursively by children.
     */
    private void toString(final StringBuilder buffer, int indentation, int pathOffset) {
        template.toString(buffer, indentation, pathOffset, "values", values);
        buffer.append('\n');
        indentation += 4;
        pathOffset += template.getPathDepth();
        for (final ValueNode child : this) {
            child.toString(buffer, indentation, pathOffset);
        }
    }
}
