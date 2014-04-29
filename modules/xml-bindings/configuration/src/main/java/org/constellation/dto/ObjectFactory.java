package org.constellation.dto;

import javax.xml.bind.annotation.XmlRegistry;
import java.util.logging.Logger;

/**
 * @author Benjamin Garcia (Geomatys)
 */
@XmlRegistry
public class ObjectFactory {

    private static final Logger LOGGER = Logger.getLogger(ObjectFactory.class.getName());

    public ObjectFactory() {
    }

    public Service createService(){
        return new Service();
    }

    public MetadataLists createMetadataLists() {
        return new MetadataLists();
    }

    public CoverageDataDescription createCoverageDataDescription(){
        return new CoverageDataDescription();
    }

    public CoverageMetadataBean createCoverageMetadataBean(){
        return new CoverageMetadataBean();
    }

    public FeatureDataDescription createFeatureDataDescription(){
        return new FeatureDataDescription();
    }

    public BandDescription createBandDescription(){
        return new BandDescription();
    }

    public PropertyDescription createPropertyDescription(){
        return new PropertyDescription();
    }

    public AccessConstraint createAccessConstraint(){
        return new AccessConstraint();
    }

    public Contact getContact(){
        return new Contact();
    }

    public ParameterValues createParameterValues(){
        return new ParameterValues();
    }
    
    public ObservationFilter createObservationFilter(){
        return new ObservationFilter();
    }
}
