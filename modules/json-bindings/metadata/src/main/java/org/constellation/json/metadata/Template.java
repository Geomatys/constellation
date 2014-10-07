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
import org.geotoolkit.sml.xml.v101.SensorMLStandard;
import org.constellation.json.metadata.binding.RootObj;


/**
 * A JSON template in which to insert metadata values for given ISO 19115 paths.
 * This class searches for object having a structure like below:
 *
 * <blockquote><pre>{
 *    "root":{
 *        "children":[{
 *            "superblock":{
 *                "path":null,
 *                "children":[{
 *                    "block":{
 *                        "path":null,
 *                        "children":[{
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
 * The only keywords handled by this class are {@code "children"}, {@code "path"}, {@code "defaultValue"}
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
     *
     * <p>By convention, name containing the {@code "sensor"} substring will be assumed to implement the
     * {@link SensorMLStandard#INSTANCE} standard, and all other {@link MetadataStandard#ISO_19115}.</p>
     */
    private static final Map<String,Template> INSTANCES;
    static {
        try {
            INSTANCES = load(new byte[] {4, 6, 6, 6, 6, 10, 10},
                    "profile_import",
                    "profile_default_vector",
                    "profile_default_raster",
                    "profile_inspire_vector",
                    "profile_inspire_raster",
                    "profile_sensorml_component",
                    "profile_sensorml_system");
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e); // Should never happen.
        }
    }

    /**
     * Creates a pre-defined template from resource files of the given names.
     *
     * @param depths The depth of each file enumerated in {@code names}.
     * @param names  The resource file names, without the {@code ".json"} suffix.
     */
    private static Map<String,Template> load(final byte[] depths, final String... names) throws IOException {
        final List<String>         lines       = new ArrayList<>();
        final Map<String,String>   sharedLines = new HashMap<>();
        final Map<String,String[]> sharedPaths = new HashMap<>();
        final Map<String,Template> templates   = new LinkedHashMap<>(4);
        for (int i=0; i<names.length; i++) {
            final String name = names[i];
            try (final BufferedReader in = new BufferedReader(new InputStreamReader(
                    Template.class.getResourceAsStream(name + ".json"), "UTF-8")))
            {
                String line;
                while ((line = in.readLine()) != null) {
                    lines.add(line);
                }
            }
            MetadataStandard standard = MetadataStandard.ISO_19115;
            if (name.contains("sensorml_system")) {
                standard = SensorMLStandard.SYSTEM;
            } else if (name.contains("sensorml_component")) {
                standard = SensorMLStandard.COMPONENT;
            }
            if (templates.put(name, new Template(standard, lines, sharedLines, sharedPaths, depths[i])) != null) {
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
     * The maximal length of {@link TemplateNode#path} arrays.
     */
    final int depth;

    /**
     * Creates a pre-defined template from a resource file of the given name.
     *
     * @param  template    The JSON lines to use as a template.
     * @param  sharedLines An initially empty map to be filled by {@link LineReader}
     *                     for sharing same {@code String} instances when possible.
     */
    private Template(final MetadataStandard standard, final Iterable<String> template, final Map<String,String> sharedLines,
            final Map<String,String[]> sharedPaths, final int depth) throws IOException
    {
        root = new TemplateNode(new LineReader(standard, template, sharedLines, sharedPaths), true, null);
        this.depth = depth;
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
        root = new TemplateNode(new LineReader(standard, template, new HashMap<String,String>(),
                new HashMap<String,String[]>()), true, null);
        depth = root.validatePath(null);
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
        root.write(metadata, out, prune, depth);
    }

    /**
     * Parses the given JSON lines and write the metadata values in the given metadata object.
     *
     * <p>The {@code skipNulls} argument controls whether {@code null} values in the JSON file shall be skipped
     * instead than stored in the metadata object. If {@code false}, null values in the JSON file will overwrite
     * (erase) metadata properties that may have existed before the operation.
     * This is sometime the desired effect when updating an existing {@code DefaultMetadata} instance.
     * However when writing to an initially empty metadata object, a value of {@code true} will reduce
     * the need to call {@link org.apache.sis.metadata.iso.DefaultMetadata#prune()} after parsing.</p>
     *
     * @param  json        Lines of the JSON file to parse.
     * @param  destination Where to store the metadata values.
     * @param  skipNulls   {@code true} for skipping {@code null} values instead than storing null in the metadata object.
     * @throws IOException if an error occurred while parsing.
     */
    public void read(final Iterable<? extends CharSequence> json, final Object destination, final boolean skipNulls) throws IOException {
        final FormReader r = new FormReader(new LineReader(root.standard, json, null, null), depth, skipNulls);
        r.read((String[]) null);
        r.writeToMetadata(root.standard, destination);
    }

    /**
     * Parses the given JSON object and write the metadata values in the given metadata object.
     * The {@code skipNulls} argument is used in the same way than in the
     * {@link #read(Iterable, Object, boolean)} method.
     *
     * @param  json        Lines of the JSON file to parse.
     * @param  destination Where to store the metadata values.
     * @param  skipNulls   {@code true} for skipping {@code null} values instead than storing null in the metadata object.
     * @throws IOException if an error occurred while parsing.
     */
    public void read(final RootObj json, final Object destination, final boolean skipNulls) throws IOException {
        final FormReader r = new FormReader(null, depth, skipNulls);
        r.read(json);
        r.writeToMetadata(root.standard, destination);
    }
    
    public static void addTemplate(final String name, Template t) {
        INSTANCES.put(name, t);
    }
}
