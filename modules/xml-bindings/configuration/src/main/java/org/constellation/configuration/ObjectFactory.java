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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


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

    public static final QName SOURCE_QNAME = new QName("http://www.geotoolkit.org/parameter", "source");
    public static final QName LAYER_QNAME  = new QName("http://www.geotoolkit.org/parameter", "Layer");
    
    
    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.constellation.configuration
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link AcknowlegementType }
     * 
     */
    public AcknowlegementType createAcknowlegementType() {
        return new AcknowlegementType();
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

    public LayerContext createLayerContext() {
        return new LayerContext();
    }

    public LayerList createLayerList() {
        return new LayerList();
    }

    public Layers createLayers() {
        return new Layers();
    }

    public Layer createLayer() {
        return new Layer();
    }

    public Source createSource() {
        return new Source();
    }

    public InstanceReport createInstanceReport() {
        return new InstanceReport();
    }
    
    public ServiceReport createServiceReport() {
        return new ServiceReport();
    }

    public ProviderReport createProviderReport() {
        return new ProviderReport();
    }

    
    public Instance createInstance() {
        return new Instance();
    }

    public ExceptionReport createExceptionReport() {
        return new ExceptionReport();
    }
    
    @XmlElementDecl(namespace = "http://www.geotoolkit.org/parameter", name = "source")
    public JAXBElement<Object> createSource(Object value) {
        return new JAXBElement<Object>(SOURCE_QNAME, Object.class, null, value);
    }
            
    @XmlElementDecl(namespace = "http://www.geotoolkit.org/parameter", name = "Layer")
    public JAXBElement<Object> createLayer(Object value) {
        return new JAXBElement<Object>(LAYER_QNAME, Object.class, null, value);
    }
}
