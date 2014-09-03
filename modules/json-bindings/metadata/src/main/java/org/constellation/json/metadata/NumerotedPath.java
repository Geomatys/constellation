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

import java.util.Arrays;
import java.io.IOException;
import org.apache.sis.util.CharSequences;


/**
 * A path together with an index for each component in the path.
 * This is used as keys for storing metadata values in a map.
 *
 * <p><b>Implementation note:</b>
 * We need the full path, not only the last UML identifier, because the same metadata object could be reached
 * by different paths (e.g. {@code "contact.party"} and {@code "identificationInfo.pointOfContact.party"}).</p>
 *
 * @author Martin Desruisseaux (Geomatys)
 */
final class NumerotedPath implements Comparable<NumerotedPath> {
    /**
     * The path elements.
     */
    final String[] path;

    /**
     * The index of each elements in the path.
     */
    final int[] indices;

    /**
     * Creates a new key for the given node and indices specified in a space-separated list.
     *
     * @param path    The template containing the path.
     * @param indices The index of each elements in the path.
     */
    NumerotedPath(final String[] path, final String indices) throws ParseException {
        this.path = path;
        Throwable cause = null;
        try {
            this.indices = CharSequences.parseInts(indices, ' ', 10);
            if (this.indices.length == path.length) {
                return;
            }
        } catch (NumberFormatException e) {
            cause = e;
        }
        throw new ParseException("Illegal indices: \"" + indices + "\".", cause);
    }

    /**
     * Creates a new key for the given node and indices.
     *
     * @param path    The template containing the path.
     * @param indices The index of each elements in the path. This array will be partially copied.
     */
    NumerotedPath(final String[] path, final int[] indices) {
        this.path    = path;
        this.indices = Arrays.copyOfRange(indices, 0, path.length);
    }

    /**
     * Invoked when the last index is irrelevant for us, except for differentiating singletons from collections.
     */
    final void ignoreLastIndex() {
        final int last = path.length - 1;
        if (indices[last] != 0) {
            indices[last] = 1;
        }
    }

    /**
     * Returns {@code true} if the values can be multiple.
     */
    final boolean isMultiOccurrenceAllowed() {
        return indices[indices.length - 1] != 0;
    }

    /**
     * Returns a sub-path containing only the given number of components.
     */
    final NumerotedPath head(final int depth) {
        if (depth == path.length) {
            return this;
        }
        return new NumerotedPath(Arrays.copyOfRange(path, 0, depth), indices);
    }

    /**
     * Returns {@code true} if this path is a child of the given path.
     */
    final boolean isChildOf(final NumerotedPath other) {
        if (other != null) { // Special case for the needs of FormReader.writeMetadata.
            if (path.length <= other.path.length) {
                return false;
            }
            for (int i = other.path.length; --i >= 0;) {
                if (!path[i].equals(other.path[i]) || indices[i] != other.indices[i]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Compares the given path with this one for order.
     */
    @Override
    public int compareTo(final NumerotedPath other) {
        final int length = Math.min(path.length, other.path.length);
        for (int i=0; i<length; i++) {
            int c = path[i].compareTo(other.path[i]);
            if (c == 0) {
                c = indices[i] - other.indices[i];
                if (c == 0) {
                    continue;
                }
            }
            return c;
        }
        return path.length - other.path.length;
    }

    /**
     * Returns {@code true} if the given key is equals to the given object.
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof NumerotedPath) &&
               Arrays.equals(path,    ((NumerotedPath) other).path) &&
               Arrays.equals(indices, ((NumerotedPath) other).indices);
    }

    /**
     * Returns a hash code value for this key.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(path) ^ Arrays.hashCode(indices);
    }

    /**
     * Returns a string representation for debugging purpose.
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        try {
            formatPath(buffer, path, 0, indices);
        } catch (IOException e) {
            throw new AssertionError(e); // Should never happen, since we are writting to a StringBuilder.
        }
        return buffer.toString();
    }

    /**
     * Formats the given path, without quotes.
     */
    static void formatPath(final Appendable out, final CharSequence[] path, int pathOffset, final int[] indices) throws IOException {
        while (pathOffset < path.length) {
            if (pathOffset != 0) {
                out.append(Keywords.PATH_SEPARATOR);
            }
            out.append(path[pathOffset]);
            if (indices != null) {
                final int index = indices[pathOffset];
                if (index != 0) {
                    out.append('[').append(Integer.toString(index)).append(']');
                }
            }
            pathOffset++;
        }
    }
}
