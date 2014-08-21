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
import java.util.MissingResourceException;
import java.io.IOException;
import java.nio.charset.Charset;
import org.opengis.util.Enumerated;
import org.apache.sis.measure.Angle;
import org.apache.sis.util.iso.Types;
import org.apache.sis.util.CharSequences;


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
