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
package org.constellation.observation;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 *
 * @version $Id:
 * @author Guilhem Legal
 */
@XmlRegistry
public class ObjectFactory {
    
    private final static QName _Observation_QNAME   = new QName("http://www.opengis.net/om/1.0", "Observation");
     
    /**
     *
     */
    public ObjectFactory() {
    }
    
     /**
     * Create an instance of {@link ObservationEntry }
     * 
     */
    public ObservationEntry createObservationEntry() {
        return new ObservationEntry();
    }
    
    /**
     * Create an instance of {@link ObservationCollectionEntry }
     * 
     */
    public ObservationCollectionEntry createObservationCollectionEntry() {
        return new ObservationCollectionEntry();
    }
    
      /**
     * Create an instance of {@link MeasurementEntry }
     * 
     */
    public MeasurementEntry createMeasurementEntry() {
        return new MeasurementEntry();
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ObservationEntry }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/om/1.0", name = "Observation", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "AbstractFeature")
    public JAXBElement<ObservationEntry> createObservation(ObservationEntry value) {
        return new JAXBElement<ObservationEntry>(_Observation_QNAME, ObservationEntry.class, null, value);
    }
}
