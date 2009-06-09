/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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

package org.constellation.configuration;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each Java content interface and Java element interface generated 
 * in the org.constellation.configuration package. 
 * An ObjectFactory allows you to programatically construct new instances of the Java representation for XML content. 
 * The Java representation of XML content can consist of schema derived interfaces and classes representing 
 * the binding of schema type definitions, element declarations and model groups.
 * Factory methods for each of these are provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.constellation.configuration
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CSWCascadingType }
     * 
     */
    public CSWCascadingType createCSWCascadingType() {
        return new CSWCascadingType();
    }

    /**
     * Create an instance of {@link UpdateCapabilitiesType }
     * 
     */
    public UpdateCapabilitiesType createUpdateCapabilitiesType() {
        return new UpdateCapabilitiesType();
    }
    
    /**
     * Create an instance of {@link AcknowlegementType }
     * 
     */
    public AcknowlegementType createAcknowlegementType() {
        return new AcknowlegementType();
    }
    
    /**
     * Create an instance of {@link UpdatePropertiesFileType }
     * 
     */
    public UpdatePropertiesFileType createUpdatePropertiesFileType() {
        return new UpdatePropertiesFileType();
    }

    /**
     * Create an instance of {@link SOSConfiguration }
     *
     */
    public SOSConfiguration createSOSConfiguration() {
        return new SOSConfiguration();
    }

    /**
     * Create an instance of {@link HarvestTasks }
     *
     */
    public HarvestTasks createHarvestTasks() {
        return new HarvestTasks();
    }

    /**
     * Create an instance of {@link HarvestTask }
     *
     */
    public HarvestTask createHarvestTask() {
        return new HarvestTask();
    }

}
