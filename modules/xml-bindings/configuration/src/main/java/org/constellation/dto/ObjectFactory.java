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

    public Details createService(){
        return new Details();
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
    
    public SimpleValue createSimpleValue() {
        return new SimpleValue();
    }
}
