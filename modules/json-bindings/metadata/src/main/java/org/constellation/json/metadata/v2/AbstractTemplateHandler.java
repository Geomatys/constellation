
package org.constellation.json.metadata.v2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
    public static final Map<Class<?>, Class<?>> DEFAULT_SPECIALIZED;
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
    
    protected Map<Class<?>, Class<?>> specialized;
    
    public AbstractTemplateHandler(final MetadataStandard standard) {
        this.standard = standard;
        this.specialized = DEFAULT_SPECIALIZED;
    }
    
    public AbstractTemplateHandler(final MetadataStandard standard, Map<Class<?>, Class<?>> specialized) {
        this.standard = standard;
        this.specialized = specialized;
    }
    
    protected Map<String,Object> asMap(final Object metadata) {
        return standard.asValueMap(metadata, KeyNamePolicy.UML_IDENTIFIER, ValueExistencePolicy.NON_EMPTY);
    }
    
    protected Map<String,Object> asFullMap(final Object metadata) {
        return standard.asValueMap(metadata, KeyNamePolicy.UML_IDENTIFIER, ValueExistencePolicy.NON_EMPTY);
    }
    
    protected Class readType(ValueNode node) throws ParseException {
        Class type;
        try {
            type = Class.forName(node.type);
        } catch (ClassNotFoundException ex) {
            throw new ParseException("Unable to find a class for type : " + node.type);
        }
        return type;
    }
}
