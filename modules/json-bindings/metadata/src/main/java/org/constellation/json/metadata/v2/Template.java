
package org.constellation.json.metadata.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.metadata.MetadataStandard;
import org.apache.sis.util.logging.Logging;
import org.constellation.json.metadata.binding.BlockObj;
import org.constellation.json.metadata.binding.ComponentObj;
import org.constellation.json.metadata.binding.Field;
import org.constellation.json.metadata.binding.FieldObj;
import org.constellation.json.metadata.binding.RootObj;
import org.constellation.json.metadata.binding.SuperBlockObj;
import org.geotoolkit.sml.xml.v101.SensorMLStandard;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class Template {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.json.metadata.v2");

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
            INSTANCES = load(
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
    private static Map<String,Template> load(final String... names) throws IOException {
        final ObjectMapper objectMapper      = new ObjectMapper();
        final Map<String,Template> templates = new LinkedHashMap<>();
        for (int i = 0; i < names.length; i++) {
            final String name         = names[i];
            final InputStream stream  = Template.class.getResourceAsStream(name + ".json");
            MetadataStandard standard = MetadataStandard.ISO_19115;
            if (name.contains("sensorml_system")) {
                standard = SensorMLStandard.SYSTEM;
            } else if (name.contains("sensorml_component")) {
                standard = SensorMLStandard.COMPONENT;
            }
            final RootObj root =  objectMapper.readValue(stream, RootObj.class);
            if (templates.put(name, new Template(standard, root)) != null) {
                throw new AssertionError(name);
            }
        }
        return templates;
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
     * Returns the names of available templates.
     *
     * @return Available templates.
     */
    public static Set<String> getAvailableNames() {
        return Collections.unmodifiableSet(INSTANCES.keySet());
    }

    /**
     * @todo current implementation is unsafe (not thread-safe, no check for existing instances).
     */
    public static void addTemplate(final String name, Template t) {
        INSTANCES.put(name, t);
    }


    private final MetadataStandard standard;

    private final RootObj rootObj;

    private final Map<Class<?>, Class<?>> specialized;

    public Template(final MetadataStandard standard, final RootObj rootObj) {
        this.standard = standard;
        this.rootObj  = rootObj;
        this.specialized = AbstractTemplateHandler.DEFAULT_SPECIALIZED;
    }

    public Template(final MetadataStandard standard, final RootObj rootObj, Map<Class<?>, Class<?>> specialized) {
        this.standard    = standard;
        this.rootObj     = rootObj;
        this.specialized = specialized;
    }

    /**
     * Writes the values of the given metadata object using the template given at construction time.
     *
     * @param metadata The metadata object to write.
     * @param out      Where to write the JSO file.
     * @param prune    {@code true} for omitting empty nodes.
     * @param overwrite {@code true} for overwriting read-only nodes.
     *
     * @throws IOException if an error occurred while writing to {@code out}.
     */
    public void write(final Object metadata, final Writer out, final boolean prune, final boolean overwrite) throws IOException {
        final TemplateWriter writer     = new TemplateWriter(standard);
        final RootObj rootFilled        = writer.writeTemplate(rootObj, metadata, prune, overwrite);
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(out, rootFilled);
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
        TemplateReader reader = new TemplateReader(standard, specialized);
        //fix missing node types unsent by the UI
        json.setNodeTypes(rootObj.getNodeTypes());
        reader.readTemplate(json, destination);
    }

    public String getCompletion(final Object metadata) throws IOException {
        final TemplateWriter writer = new TemplateWriter(standard);
        final RootObj rootFilled    = writer.writeTemplate(rootObj, metadata, false, false);
        return getCompletion(rootFilled);
    }

    public String getCompletion(final RootObj metadataValues) {
        Map<String, Boolean> completions = new HashMap<>();
        completions.put("ELEMENTARY", Boolean.TRUE);
        completions.put("EXTENDED",   Boolean.TRUE);
        completions.put("COMPLETE",   Boolean.TRUE);

        final List<SuperBlockObj> superblocks = metadataValues.getRoot().getChildren();
        for(final SuperBlockObj sb:superblocks){
            final List<BlockObj> blocks = sb.getSuperblock().getChildren();
            for(final BlockObj b:blocks){
                final List<ComponentObj> fields = b.getBlock().getChildren();
                for(final ComponentObj f:fields){
                    final Field field = ((FieldObj)f).getField();
                    final String value = field.value;
                    final String completion = field.getCompletion();
                    if (completion != null) {
                        if (value == null || value.isEmpty()) {
                            if (completions.containsKey(completion)) {
                                completions.put(completion, false);
                            } else {
                                LOGGER.log(Level.WARNING, "unrecognized completion:{0}", completion);
                            }
                        }
                    } else {
                        LOGGER.log(Level.WARNING, "No completion for field: {0}", field.getName());
                    }

                }
            }
        }
        if (completions.get("ELEMENTARY")) {
            if (completions.get("EXTENDED")) {
                if (completions.get("COMPLETE")) {
                    return "COMPLETE";
                }
                return "EXTENDED";
            }
            return "ELEMENTARY";
        }
        return "NONE";
    }

    public int calculateMDCompletion(final Object metadata) throws IOException {
        final TemplateWriter writer = new TemplateWriter(standard);
        final RootObj rootFilled    = writer.writeTemplate(rootObj, metadata, false, false);
        return calculateMDCompletion(rootFilled);
    }

    public int calculateMDCompletion(final RootObj metadataValues) {
        int result = 0;
        int fieldsCount=0;
        int fieldValueCount=0;
        final List<SuperBlockObj> superblocks = metadataValues.getRoot().getChildren();
        for(final SuperBlockObj sb:superblocks){
            final List<BlockObj> blocks = sb.getSuperblock().getChildren();
            for(final BlockObj b:blocks){
                final List<ComponentObj> fields = b.getBlock().getChildren();
                for(final ComponentObj f:fields){
                    fieldsCount++;
                    final String value = ((FieldObj)f).getField().value;
                    if(value != null && !value.isEmpty()){
                        fieldValueCount++;
                    }

                }
            }
        }
        if(fieldsCount>0){
            result = Math.round(fieldValueCount*100/fieldsCount);
        }
        return result;
    }
}
