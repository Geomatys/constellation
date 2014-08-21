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
import java.util.Iterator;
import java.io.EOFException;
import org.apache.sis.util.Numbers;
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
     * The {@code null} string.
     */
    private static final String NULL = "null";

    /**
     * The metadata standard for the {@link TemplateNode} to create.
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
    private int length;

    /**
     * The current position in {@link #line}.
     */
    private int position;

    /**
     * Non-null if the current {@linkplain #line} has a trailing comma, ignoring whitespaces.
     * If non-null, the value is either "," or ",{".
     *
     * @see #skipComma()
     */
    private String trailingComma;

    /**
     * Creates a new parser.
     *
     * @param  standard The standard used by the metadata objects to write.
     * @param  template The JSON lines to use as a template.
     * @param  pool     An initially empty map to be filled by {@code Parser}
     *                  for sharing same {@code String} instances when possible.
     */
    Parser(final MetadataStandard standard, final Iterable<String> lines, final Map<String,String> pool) {
        this.standard = standard;
        this.pool     = pool;
        this.lines    = lines.iterator();
    }

    /**
     * Returns the next line.
     */
    String nextLine() throws EOFException {
        trailingComma = null;
        if (!lines.hasNext()) {
            throw new EOFException("Unexpected end of file.");
        }
        line     = putIfAbsent(lines.next());
        length   = CharSequences.skipTrailingWhitespaces(line, 0, line.length());
        position = CharSequences.skipLeadingWhitespaces (line, 0, length);
        final int remaining = length - position;
        if (remaining >= 1) {
            switch (line.charAt(length - 1)) {
                case ',': {
                    trailingComma = ",";
                    break;
                }
                case '{': {
                    if (remaining >= 2 && line.charAt(length - 2) == ',') {
                        trailingComma = ",{";
                    }
                    break;
                }
            }
            if (trailingComma != null) {
                length = CharSequences.skipTrailingWhitespaces(line, position, length - trailingComma.length());
            }
        }
        return line;
    }

    /**
     * Returns the line length, excluding the trailing comma (if any) and trailing whitespaces.
     */
    short length() throws ParseException {
        if ((length & ~Short.MAX_VALUE) == 0) {
            return (short) length;
        }
        throw new ParseException("Line too long.");
    }

    /**
     * Returns {@code true} if the current line is empty or contains only whitespaces, ignoring the trailing comma.
     */
    boolean isEmpty() {
        return position >= length;
    }

    /**
     * Returns {@code true} if the current line starting at the current position contains the given substring.
     * If there is a match, then {@link #position} is set to the first character after the substring.
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
     * The position is set after the value, which is usually {@link #length}.
     *
     * @return The value (may be {@code null}, a {@link String} or a {@link Number}).
     * @throws ParseException If the line doesn't have the expected syntax.
     */
    Object getValue() throws ParseException {
        Exception cause = null;
        position = CharSequences.skipLeadingWhitespaces(line, position, length);
        if (position < length && line.charAt(position) == ':') {
            position = CharSequences.skipLeadingWhitespaces(line, position+1, length);
            if (position < length) {
                if (regionMatches(NULL)) {
                    if (position == length) {
                        return null;
                    }
                } else if (line.charAt(position) == '"' && line.charAt(length-1) == '"') {
                    final int p = position + 1;
                    position = length;
                    return CharSequences.replace(line.substring(p, length - 1), "\\\"", "\"").toString();
                } else try {
                    final Number value = Numbers.narrowestNumber(line.substring(position, length));
                    position = length;
                    return value;
                } catch (NumberFormatException e) {
                    cause = e;
                }
            }
        }
        throw new ParseException("Invalid \"name\":\"value\" pair:\n" + line, cause);
    }

    /**
     * Returns {@code true} if the current line has a trailing comma, ignoring whitespaces.
     * The comma may be "," alone, or the comma followed by an opening bracket ",{".
     */
    boolean hasTrailingComma() {
        return trailingComma != null;
    }

    /**
     * Skips the comma, if presents. Leading whitespaces are ignored.
     *
     * @return Non-null if a comma was present.
     */
    String skipComma() {
        position = CharSequences.skipLeadingWhitespaces(line, position, length);
        if (position >= length) {
            return trailingComma;
        }
        if (line.charAt(position) == ',') {
            position++;
            return ",";
        }
        return null;
    }

    /**
     * Increment or decrement the given level depending on the amount of { or } characters
     * in the current portion of the current line. The {@linkplain #position} is advanced
     * until after the } character of level 0, or until the end of line.
     */
    int updateLevel(int level) {
        final int upper = line.length();
        boolean quote = false;
scan:   while (position < upper) {
            switch (line.charAt(position++)) {
                case '"': {
                    if (position <= 1 || line.charAt(position - 2) != '\\') {
                        quote = !quote;
                    }
                    break;
                }
                case '{': {
                    if (!quote) {
                        level++;
                    }
                    break;
                }
                case '}': {
                    if (!quote && --level == 0) {
                        break scan;
                    }
                    break;
                }
            }
        }
        return level;
    }

    /**
     * Returns the current line (in full, not only the current portion) without the trailing "null".
     */
    String currentLineWithoutNull() throws ParseException {
        if (trailingComma != null) throw new ParseException("Value shall be the last entry in a field.");
        if (getValue()    != null) throw new ParseException("Value of \"value\" shall be null.");
        return putIfAbsent(line.substring(0, length - NULL.length()));
    }

    /**
     * Returns the currently selected part of current line.
     * For debugging purpose only.
     */
    @Override
    public String toString() {
        return (line != null) ? line.substring(position, length) : "";
    }

    /**
     * Adds the given line to the pool if absent, or returns the existing line otherwise.
     *
     * @todo This is a helper method to be removed when we will upgrade to JDK8.
     */
    private String putIfAbsent(final String newLine) {
        final String existing = pool.get(newLine);
        if (existing != null) {
            return existing;
        }
        pool.put(newLine, newLine);
        return newLine;
    }
}
