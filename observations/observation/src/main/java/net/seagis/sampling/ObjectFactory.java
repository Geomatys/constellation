/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package net.seagis.sampling;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import net.seagis.gml.v311.PointType;

/**
 *
 * @version $Id:
 * @author Guilhem Legal
 */
@XmlRegistry
public class ObjectFactory {
    
    private final static QName _SamplingPoint_QNAME = new QName("http://www.opengis.net/sa/1.0", "SamplingPoint");
    /**
     *
     */
    public ObjectFactory() {
    }
    
    /**
     * Create an instance of {@link SamplingPointEntry }
     * 
     */
    public SamplingPointEntry createSamplingPointEntry() {
        return new SamplingPointEntry();
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ObservationEntry }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/sa/1.0", name = "SamplingPoint", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "AbstractFeature")
    public JAXBElement<SamplingPointEntry> createSamplingPoint(SamplingPointEntry value) {
        return new JAXBElement<SamplingPointEntry>(_SamplingPoint_QNAME, SamplingPointEntry.class, null, value);
    }
}
