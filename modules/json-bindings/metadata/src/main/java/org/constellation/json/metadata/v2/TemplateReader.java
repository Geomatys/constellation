
package org.constellation.json.metadata.v2;

import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.metadata.AbstractMetadata;
import org.apache.sis.metadata.KeyNamePolicy;
import org.apache.sis.metadata.MetadataStandard;
import org.apache.sis.metadata.TypeValuePolicy;
import org.apache.sis.metadata.ValueExistencePolicy;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.apache.sis.util.iso.Types;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.NilReason;
import org.constellation.json.JsonMetadataConstants;
import org.constellation.json.metadata.ParseException;
import org.constellation.json.metadata.binding.RootObj;
import org.geotoolkit.gts.xml.PeriodDurationType;
import org.geotoolkit.metadata.MetadataFactory;
import org.geotoolkit.sml.xml.v101.SensorMLStandard;
import org.opengis.metadata.citation.Responsibility;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.constraint.Constraints;
import org.opengis.metadata.constraint.LegalConstraints;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.metadata.identification.DataIdentification;
import org.opengis.metadata.identification.Identification;
import org.opengis.metadata.quality.ConformanceResult;
import org.opengis.metadata.quality.DomainConsistency;
import org.opengis.metadata.quality.Element;
import org.opengis.metadata.quality.Result;
import org.opengis.metadata.spatial.SpatialRepresentation;
import org.opengis.metadata.spatial.VectorSpatialRepresentation;
import org.opengis.temporal.PeriodDuration;
import org.opengis.util.CodeList;
import org.opengis.util.FactoryException;
import org.opengis.util.InternationalString;

/**
 * RootObj ===> Metadata Object
 * 
 * @author guilhem
 */
public class TemplateReader extends AbstractTemplateHandler {
    
    private static final Logger LOGGER = Logging.getLogger(TemplateReader.class);
    
    private static final MetadataFactory
            DEFAULT   = new MetadataFactory(),
            SYSTEM    = new MetadataFactory(SensorMLStandard.SYSTEM),
            COMPONENT = new MetadataFactory(SensorMLStandard.COMPONENT);
    
    /**
     * The default value to give to the {@code specialized} of {@link FormReader} constructor.
     */
    private static final Map<Class<?>, Class<?>> DEFAULT_SPECIALIZED;
    static {
        final Map<Class<?>, Class<?>> specialized = new HashMap<>();
        specialized.put(Responsibility.class,        ResponsibleParty.class);
        specialized.put(Identification.class,        DataIdentification.class);
        specialized.put(GeographicExtent.class,      GeographicBoundingBox.class);
        specialized.put(SpatialRepresentation.class, VectorSpatialRepresentation.class);
        specialized.put(Constraints.class,           LegalConstraints.class);
        specialized.put(Result.class,                ConformanceResult.class);
        specialized.put(Element.class,               DomainConsistency.class);
        DEFAULT_SPECIALIZED = specialized;
    }
    

    /**
     * The metadata factory to use for creating new instances.
     */
    private final MetadataFactory factory;
    
    
    public TemplateReader(final MetadataStandard standard) {
        super(standard);
        if (standard == SensorMLStandard.SYSTEM) {
            factory = SYSTEM;
        } else if (standard == SensorMLStandard.COMPONENT) {
            factory = COMPONENT;
        } else if (standard == MetadataStandard.ISO_19115){
            factory = DEFAULT;
        } else {
            factory = new MetadataFactory(standard, MetadataStandard.ISO_19115);
        }
    }
    
    public Object readTemplate(final RootObj template, final Object metadata) throws ParseException {
        
        final TemplateTree tree  = TemplateTree.getTreeFromRootObj(template);
        
        updateObjectFromRootObj(tree,  tree.getRoot(), metadata);
        
        if (metadata instanceof AbstractMetadata) {
            ((AbstractMetadata)metadata).prune();
        }
        return metadata;
    }

    private void updateObjectFromRootObj(TemplateTree tree, final ValueNode root, final Object metadata) throws ParseException {
        
        
        final List<ValueNode> children = new ArrayList<>(root.children);
        for (ValueNode node : children) {
            
            final Object obj = getValue(node, metadata);
            
            if (obj instanceof Collection) {
                final Collection list = (Collection) obj;
                Object child = get((Collection) obj, node.ordinal);
                if (child != null) {
                    if (node.isField()) {
                        replace(list, node.ordinal, convert(node.name, child.getClass(), node.value));
                    } else {
                        updateObjectFromRootObj(tree, node, child);
                    }
                } else {
                    if (node.isField()) {
                        putValue(node, metadata);
                    } else {
                        Object newValue = buildNewInstance(metadata, node);
                        putValue(node, metadata, newValue);
                        updateObjectFromRootObj(tree, node, newValue);
                    }
                }

            } else if (obj != null){
                if (node.isField()) {
                    putValue(node, obj);
                } else {
                    updateObjectFromRootObj(tree, node, obj);
                }
            } else {
                if (node.isField()) {
                    putValue(node, metadata);
                } else {
                    Object newValue = buildNewInstance(metadata, node);
                    putValue(node, metadata, newValue);
                    updateObjectFromRootObj(tree, node, newValue);
                }
            }
        }
    }
    
    
    private Object getValue(final ValueNode node, Object metadata) {
        if (metadata instanceof AbstractMetadata) {
            AbstractMetadata meta = (AbstractMetadata) metadata;
            return meta.asMap().get(node.name);
        } else {
            // TODO try via getter
            return null;
        }
    }
    
    private void putValue(final ValueNode node, final Object metadata) throws ParseException {
        putValue(node, metadata, node.value);
    }
    
    private void putValue(final ValueNode node, final Object metadata, Object value) throws ParseException {
        Class type = getType(metadata, node);
        value      = convert(node.name, type, value);
        final Map<String,Object> values = asMap(metadata);
        values.put(node.name, value);
    }
    
    /**
     * Returns the type of values for the given property in the given metadata.
     */
    private Class<?> getType(final Object metadata, final ValueNode node) throws ParseException {
        if (node.type != null) {
            try {
                return Class.forName(node.type);
            } catch (ClassNotFoundException ex) {
                throw new ParseException("Unable to find a class for type : " + node.type);
            }
        }
        Class type = standard.asTypeMap(metadata.getClass(), KeyNamePolicy.UML_IDENTIFIER, TypeValuePolicy.ELEMENT_TYPE).get(node.name);
        final Class<?> special = DEFAULT_SPECIALIZED.get(type);
        if (special != null) {
            return special;
        }
        return type;
    }
    
    private Object buildNewInstance(final Object metadata, final ValueNode node) throws ParseException {
        try {
            Class type = getType(metadata, node);
            return factory.create(type, Collections.<String,Object>emptyMap());
        } catch (FactoryException ex) {
            throw new ParseException("Error while building empty instance from factory");
        }
    }
    
    /**
     * Converts the given value to an instance of the given class before to store in the metadata object.
     */
    private static Object convert(final String identifier, final Class<?> type, Object value) throws ParseException {
        if (type == Date.class) {
            return toDate(value, identifier);
        }
        if (type == PeriodDuration.class && value instanceof String) {
            try {
                return new PeriodDurationType((String) value);
            } catch (IllegalArgumentException ex) {
                // TODO: "log and continue" is not appropriate here, since the user can not know that his data is lost.
                LOGGER.log(Level.WARNING, "Bad period duration value: {0} (property: {1})",
                        new Object[] {value, identifier});
            }
        }
        if (InternationalString.class.isAssignableFrom(type) && value instanceof String) {
            return new SimpleInternationalString(value.toString());
        }
        if (!CharSequence.class.isAssignableFrom(type) && (value instanceof CharSequence)) {
            String text = value.toString();
            if (text.startsWith("nilReason:")) {
                try {
                    value = NilReason.valueOf(text.substring("nilReason:".length())).createNilObject(type);
                } catch (URISyntaxException | IllegalArgumentException e) {
                    throw new ParseException("Illegal value: \"" + text + "\".(property:" + identifier + ")", e);
                }
            } else {
                final boolean isCodeList = CodeList.class.isAssignableFrom(type);
                if (isCodeList || type == Locale.class || type == Charset.class || type.isEnum()) {
                    text = text.substring(text.indexOf('.') + 1).trim();
                    if (isCodeList) {
                        value = Types.forCodeName(type.asSubclass(CodeList.class), text, false);
                    } else {
                        value = text;
                    }
                }
            }
        }
        return value;
    }

    /**
     * Returns the given value as a date.
     */
    private static Date toDate(final Object value, final String identifier) throws ParseException {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return new Date(((Number) value).longValue());
        }
        final String t = (String) value;
        if (t.indexOf('-') < 0) try {
            return new Date(Long.valueOf(t));
        } catch (NumberFormatException e) {
            throw new ParseException("Illegal date: " + value + " (property:" + identifier +")", e);
        }
        try {
            synchronized (JsonMetadataConstants.DATE_FORMAT) {
                return JsonMetadataConstants.DATE_FORMAT.parse((String) value);
            }
        } catch (java.text.ParseException e) {
            throw new ParseException("Illegal date: " + value + " (property:" + identifier +")", e);
        }
    }
    
    
    private Object get(Collection c , int ordinal) {
        if (c.size() > ordinal) {
            final Iterator it = c.iterator();
            for (int i = 0; i < c.size(); i++) {
                Object o = it.next();
                if (i == ordinal) {
                    return o;
                }
            }
        }
        return null;
    }
    
    private void replace(Collection c , int ordinal, Object newValue) {
        if (c instanceof List) {
            List list = (List) c;
            list.set(ordinal, newValue);
        } else {
            throw new IllegalArgumentException("COllection is not a List");
        }
    }
}
