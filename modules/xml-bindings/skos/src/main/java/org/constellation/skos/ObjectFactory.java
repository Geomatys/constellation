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
package org.constellation.skos;

import javax.xml.bind.annotation.XmlRegistry;


/**
 */
@XmlRegistry
public class ObjectFactory {

   /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: net.opengis.sld
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link UserDefinedSymbolization }
     * 
     */
    public RDF createRDF() {
        return new RDF();
    }
    
    /**
     * Create an instance of {@link DescribeLayerResponseType }
     * 
     */
    public Concept  createConcept() {
        return new Concept();
    }
}
