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
import java.util.Arrays;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.MissingResourceException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.IOException;
import java.nio.charset.Charset;
import org.opengis.util.Enumerated;
import org.apache.sis.measure.Angle;
import org.apache.sis.util.iso.Types;
import org.apache.sis.util.CharSequences;
import static org.constellation.json.metadata.Keywords.DATE_READ_ONLY;


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
     * The object to use for parsing dates of the form "2014-09-11".
     * Usage of this format shall be synchronized on {@code DATE_FORMAT}.
     */
    static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    static final DateFormat DATE_HOUR_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * The template for which this node contains a value.
     */
    final TemplateNode template;

    /**
     * The indices of each path element.
     */
    final int[] indices;

    /**
     * The value associated to this node, or {@code null}.
     */
    final Object value;

    /**
     * Creates a new node for the given metadata.
     *
     * @param template The template to apply.
     * @param indices  The indices of each path element.
     * @param value    The value associated to this node, or {@code null}.
     */
    ValueNode(final TemplateNode template, final int[] indices, final Object value) {
        this.template = template;
        this.value    = value;
        this.indices  = (template.path != null) ? Arrays.copyOfRange(indices, 0, template.path.length) : null;
    }

    /**
     * Returns {@code true} if the path is equals to the given numeroted path.
     */
    final boolean pathEquals(final NumerotedPath np) {
        return Arrays.equals(template.path, np.path) &&
               Arrays.equals(indices, np.indices);
    }

    /**
     * Formats the path. Callers must ensure that {@link TemplateNode#path} is non-null
     * before to invoke this method.
     */
    final void formatPath(final Appendable out, int pathOffset) throws IOException {
        out.append('"');
        NumerotedPath.formatPath(out, template.path, pathOffset, indices);
        out.append('"');
    }

    /**
     * Formats the value.
     */
    final void formatValue(final Appendable out) throws IOException {
        final String p;
        if (value == null) {
            p = null;
        } else if (value instanceof Number) {
            p = value.toString();
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
            } else if (value instanceof Date) {
                if (DATE_READ_ONLY.equals(template.render)) {
                    synchronized (DATE_HOUR_FORMAT) {
                        p = DATE_HOUR_FORMAT.format(value);
                    }
                } else {
                    synchronized (DATE_FORMAT) {
                        p = DATE_FORMAT.format(value);
                    }
                }
                out.append(p);
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
                CharSequence cs = value.toString();
                cs = CharSequences.replace(cs, "\"", "\\\"");
                cs = CharSequences.replace(cs, "\t", "\\t");
                cs = CharSequences.replace(cs, "\n", "\\n");
                out.append(cs);
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
        boolean hasValue = template.isField();
        buffer.append(CharSequences.spaces(indentation)).append(hasValue ? "Field" : "Node").append('[');
        hasValue &= (value != null);
        if (template.path != null) {
            buffer.append("path:");
            try {
                formatPath(buffer, pathOffset);
            } catch (IOException e) {
                throw new AssertionError(e); // Should never happen, since we are writting to a StringBuilder.
            }
            if (hasValue) {
                buffer.append(", ");
            }
            pathOffset += template.path.length; // For the iteration over children.
        }
        if (hasValue) {
            buffer.append("value:\"").append(value).append('"');
        }
        buffer.append("]\n");
        indentation += 4;
        for (final ValueNode child : this) {
            child.toString(buffer, indentation, pathOffset);
        }
    }
}
