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

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import org.apache.sis.metadata.MetadataStandard;


/**
 * A JSON template in which to insert metadata values for given ISO 19115 paths.
 * This class searches for object having a structure like below:
 *
 * <blockquote><pre>{
 *    "root":{
 *        "content":[
 *            {
 *                "superblock":{
 *                    "path":null,
 *                    "content":[{
 *                        "block":{
 *                            "path":null,
 *                            "content":[
 *                                {
 *                                    "field":{
 *                                        "path":"identificationInfo.citation.title",
 *                                        "defaultValue":null
 *                                    }
 *                                },
 *                                {
 *                                    "field":{
 *                                        // etc...
 *                                    }
 *                                }
 *                            ]
 *                        }
 *                    }]
 *                }
 *            }
 *        }
 *    }
 *}</pre></blockquote>
 *
 * The file may have more (key:value) pairs - all unknown entries will be copied verbatim.
 * However the current implementation requires the { and } characters to be always like above,
 * ignoring whitespaces. In particular, we are currently not allowing to put content after {
 * or before }, and comma must be always the last character on a line (ignoring whitespaces).
 *
 * @author Martin Desruisseaux (Geomatys)
 */
public class Template {
    /**
     * The root node of the JSON tree to use as a template.
     */
    private final Node root;

    /**
     * Creates a new template from the given JSON file.
     *
     * @param  standard The standard used by the metadata objects to write.
     * @param  template Path to a file containing the JSON lines to use as a template.
     * @throws IOException if an error occurred while reading the template.
     */
    public Template(final MetadataStandard standard, final URI template) throws IOException {
        this(standard, Files.readAllLines(Paths.get(template)));
    }

    /**
     * Creates a new template with the given lines.
     *
     * @param  standard The standard used by the metadata objects to write.
     * @param  template The JSON lines to use as a template.
     * @throws IOException if an error occurred while parsing the JSON template.
     */
    public Template(final MetadataStandard standard, final Iterable<String> template) throws IOException {
        this.root = new Node(new Parser(standard, template));
    }

    /**
     * Writes the values of the given metadata object using the template given at construction time.
     *
     * @param metadata The metadata object to write.
     * @param out Where to write the JSO file.
     *
     * @throws IOException if an error occurred while writing to {@code out}.
     */
    public void write(final Object metadata, final Appendable out) throws IOException {
        root.write(metadata, out);
    }
}
