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
import java.util.HashMap;
import java.util.Iterator;
import java.io.EOFException;
import org.apache.sis.util.CharSequences;
import org.apache.sis.metadata.MetadataStandard;


/**
 * Helper class for parsing the lines of a JSON file in the format described by {@link Template}.
 * The parsed file may be either the template, or a file obtained after insertion of values in the template.
 *
 * @author Martin Desruisseaux (Geomatys)
 */
final class Parser {
    /**
     * The metadata standard for the {@link Node} to create.
     */
    final MetadataStandard standard;

    /**
     * Pool of string, for replacing duplicated instances (which are numerous) by a single instance.
     */
    private final Map<String,String> pool;

    /**
     * An iterator over the lines to read.
     */
    private final Iterator<String> lines;

    /**
     * The line currently being parsed.
     */
    private String line;

    /**
     * The line length, excluding the trailing comma (if any) and trailing whitespaces.
     */
    int length;

    /**
     * The current position in {@link #line}.
     */
    int position;

    /**
     * {@code true} if the current {@linkplain #line} has a trailing comma, ignoring whitespaces.
     */
    boolean hasTrailingComa;

    /**
     * Creates a new parser.
     */
    Parser(final MetadataStandard standard, final Iterable<String> lines) {
        this.standard = standard;
        this.pool     = new HashMap<>();
        this.lines    = lines.iterator();
    }

    /**
     * Returns the next line.
     */
    String nextLine() throws EOFException {
        hasTrailingComa = false;
        if (!lines.hasNext()) {
            throw new EOFException("Unexpected end of file.");
        }
        line     = putIfAbsent(lines.next());
        length   = CharSequences.skipTrailingWhitespaces(line, 0, line.length());
        position = CharSequences.skipLeadingWhitespaces (line, 0, length);
        if (position < length) {
            hasTrailingComa = (line.charAt(length - 1) == ',');
            if (hasTrailingComa) {
                length = CharSequences.skipTrailingWhitespaces(line, position, length - 1); // Skip trailing comma.
            }
        }
        return line;
    }

    /**
     * Adds the given line to the pool if absent, or returns the existing line otherwise.
     */
    private String putIfAbsent(final String newLine) {
        final String existing = pool.get(newLine);
        if (existing != null) {
            return existing;
        }
        pool.put(newLine, newLine);
        return newLine;
    }

    /**
     * Returns {@code true} if the current line starting at the current position contains the given substring.
     * If there is a match, the {@link #position} is set to the first character after the substring.
     */
    boolean regionMatches(final String s) {
        if (line.regionMatches(position, s, 0, s.length())) {
            position += s.length();
            return true;
        }
        return false;
    }

    /**
     * Skips the {@code ':'} separator in the current line, then returns the value.
     *
     * @return The value (may be {@code null}).
     * @throws ParseException If the line doesn't have the expected syntax.
     */
    String getValue() throws ParseException {
        position = CharSequences.skipLeadingWhitespaces(line, position, length);
        if (position < length && line.charAt(position) == ':') {
            position = CharSequences.skipLeadingWhitespaces(line, position+1, length);
            if (position < length) {
                if (regionMatches("null")) {
                    if (position == length) {
                        return null;
                    }
                } else if (line.charAt(position) == '"' && line.charAt(length-1) == '"') {
                    return CharSequences.replace(line.substring(position + 1, length - 1), "\\\"", "\"").toString();
                }
            }
        }
        throw new ParseException("Invalid \"name\":\"value\" pair:\n" + line);
    }

    /**
     * Returns the currently selected part of current line.
     * For debugging purpose only.
     */
    @Override
    public String toString() {
        return line.substring(position, length);
    }
}
