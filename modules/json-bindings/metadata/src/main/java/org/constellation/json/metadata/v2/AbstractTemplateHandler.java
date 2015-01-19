
package org.constellation.json.metadata.v2;

import java.util.HashMap;
import java.util.Map;
import org.apache.sis.metadata.KeyNamePolicy;
import org.apache.sis.metadata.MetadataStandard;
import org.apache.sis.metadata.TypeValuePolicy;
import org.apache.sis.metadata.ValueExistencePolicy;
import org.constellation.json.metadata.ParseException;
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

/**
 *
 * @author guilhem
 */
public class AbstractTemplateHandler {
    
    /**
     * The metadata standard.
     */
    protected final MetadataStandard standard;
    
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
    
    public AbstractTemplateHandler(final MetadataStandard standard) {
        this.standard = standard;
    }
    
    protected Map<String,Object> asMap(final Object metadata) {
        return standard.asValueMap(metadata, KeyNamePolicy.UML_IDENTIFIER, ValueExistencePolicy.NON_EMPTY);
    }
    
    protected Map<String,Object> asFullMap(final Object metadata) {
        return standard.asValueMap(metadata, KeyNamePolicy.UML_IDENTIFIER, ValueExistencePolicy.NON_EMPTY);
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
        Class type= standard.asTypeMap(metadata.getClass(), KeyNamePolicy.UML_IDENTIFIER, TypeValuePolicy.ELEMENT_TYPE).get(node.name);
        final Class<?> special = DEFAULT_SPECIALIZED.get(type);
        if (special != null) {
            return special;
        }
        return type;
    }
}
