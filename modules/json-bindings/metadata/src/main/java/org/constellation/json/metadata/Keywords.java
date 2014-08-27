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


/**
 * The keywords to search for in a JSON file.
 *
 * Current version use static constants. A future version may change them to non-static constants
 * if we want to allow more customizable templates.
 *
 * @author Martin Desruisseaux (Geomatys)
 */
final class Keywords {
    /**
     * The separator character to use in paths.
     */
    static final char PATH_SEPARATOR = '.';

    /**
     * The keyword for the path attribute in JSON file, including quotes.
     */
    static final String PATH = "\"path\"";

    /**
     * The keyword for the "maximum occurrences" attribute in JSON file, including quotes.
     */
    static final String MAX_OCCURRENCES = "\"multiplicity\"";

    /**
     * The keyword for the default value attribute in JSON file, including quotes.
     */
    static final String DEFAULT_VALUE = "\"defaultValue\"";

    /**
     * The keyword for the value attribute in JSON file, including quotes.
     */
    static final String VALUE = "\"value\"";

    /**
     * The keyword for the value attribute in JSON file, including quotes.
     */
    static final String CONTENT = "\"content\"";

    /**
     * Do not allow (for now) instantiation of this class.
     */
    private Keywords() {
    }
}
