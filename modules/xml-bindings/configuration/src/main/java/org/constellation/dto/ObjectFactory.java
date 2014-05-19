/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
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
