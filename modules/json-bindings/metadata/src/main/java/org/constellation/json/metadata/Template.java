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

import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.sis.metadata.MetadataStandard;


/**
 * A JSON template in which to insert metadata values for given ISO 19115 paths.
 * This class searches for object having a structure like below:
 *
 * <blockquote><pre>{
 *    "root":{
 *        "content":[{
 *            "superblock":{
 *                "path":null,
 *                "content":[{
 *                    "block":{
 *                        "path":null,
 *                        "content":[{
 *                            "field":{
 *                                "path":"identificationInfo.citation.title",
 *                                "defaultValue":null
 *                            }
 *                        },{
 *                            "field":{
 *                                // etc...
 *                            }
 *                        }
 *                    ]}
 *                }
 *            ]}
 *        }
 *    }
 *}</pre></blockquote>
 *
 * The only keywords handled by this class are {@code "content"}, {@code "path"}, {@code "defaultValue"}
 * and {@code "value"}. All other entries will be copied verbatim. This allow templates to provide additional
 * (key:value) pairs without the need to modify the {@code Template} code.
 *
 * <p><b>Multi-threading</b><br>
 * This class is thread safe.</p>
 *
 * <p><b>Limitations</b></p>
 * <ul>
 *   <li>The current implementation requires that comma is the last character on a line (ignoring whitespaces),
 *       or the character just before an opening {.</li>
 *   <li>The current implementation has only limited tolerance to the location where {, }, [ and ] characters can be placed
 *       (this is no a full JSON parser). We recommend to stay close to the above formatting.</li>
 * </ul>
 *
 * @author Martin Desruisseaux (Geomatys)
 */
public class Template {
    /**
     * Pre-defined instances. This map shall not be modified after class initialization,
     * in order to allow concurrent access without synchronization.
     */
    private static final Map<String,Template> INSTANCES;
    static {
        try {
            INSTANCES = load("profile_inspire_vector", "profile_inspire_raster");
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e); // Should never happen.
        }
    }

    /**
     * Creates a pre-defined template from resource files of the given names.
     *
     * @param names The resource file names, without the {@code ".json"} suffix.
     */
    private static Map<String,Template> load(final String... names) throws IOException {
        final List<String>         lines       = new ArrayList<>();
        final Map<String,String>   sharedLines = new HashMap<>();
        final Map<String,String[]> sharedPaths = new HashMap<>();
        final Map<String,Template> templates   = new LinkedHashMap<>(4);
        for (final String name : names) {
            try (final BufferedReader in = new BufferedReader(new InputStreamReader(
                    Template.class.getResourceAsStream(name + ".json"), "UTF-8")))
            {
                String line;
                while ((line = in.readLine()) != null) {
                    lines.add(line);
                }
            }
            if (templates.put(name, new Template(lines, sharedLines, sharedPaths)) != null) {
                throw new AssertionError(name);
            }
            lines.clear();
        }
        return templates;
    }

    /**
     * The root node of the JSON tree to use as a template.
     */
    final TemplateNode root;

    /**
     * Creates a pre-defined template from a resource file of the given name.
     *
     * @param  template    The JSON lines to use as a template.
     * @param  sharedLines An initially empty map to be filled by {@link Parser}
     *                     for sharing same {@code String} instances when possible.
     */
    private Template(final Iterable<String> template, final Map<String,String> sharedLines,
            final Map<String,String[]> sharedPaths) throws IOException
    {
        root = new TemplateNode(new Parser(MetadataStandard.ISO_19115, template, sharedLines, sharedPaths), true, null);
        /*
         * Do not validate the path (root.validatePath(null)). We will do that in JUnit tests instead,
         * in order to avoid consuming CPU for a verification of a static resource.
         */
    }

    /**
     * Creates a new template with the given lines.
     * This constructor is for use of custom templates.
     * Consider using one of the predefined templates returned by {@link #getInstance(String)} instead.
     *
     * @param  standard The standard used by the metadata objects to write.
     * @param  template The JSON lines to use as a template.
     * @throws IOException if an error occurred while parsing the JSON template.
     *
     * @see #getInstance(String)
     */
    public Template(final MetadataStandard standard, final Iterable<String> template) throws IOException {
        root = new TemplateNode(new Parser(standard, template, new HashMap<String,String>(),
                new HashMap<String,String[]>()), true, null);
        root.validatePath(null);
    }

    /**
     * Returns the names of available templates.
     *
     * @return Available templates.
     */
    public static Set<String> getAvailableNames() {
        return Collections.unmodifiableSet(INSTANCES.keySet());
    }

    /**
     * Returns the template of the given name.
     * Currently recognized names are:
     *
     * <ul>
     *   <li>profile_inspire_vector</li>
     *   <li>profile_inspire_raster</li>
     * </ul>
     *
     * @param  name Name of the template to get.
     * @return The template of the given name.
     * @throws IllegalArgumentException if the given name is not a known template name.
     */
    public static Template getInstance(final String name) throws IllegalArgumentException {
        final Template instance = INSTANCES.get(name);
        if (instance == null) {
            throw new IllegalArgumentException("Undefined template: " + name);
        }
        return instance;
    }

    /**
     * Writes the values of the given metadata object using the template given at construction time.
     *
     * @param metadata The metadata object to write.
     * @param out      Where to write the JSO file.
     * @param prune    {@code true} for omitting empty nodes.
     *
     * @throws IOException if an error occurred while writing to {@code out}.
     */
    public void write(final Object metadata, final Appendable out, final boolean prune) throws IOException {
        root.write(metadata, out, prune);
    }
}
