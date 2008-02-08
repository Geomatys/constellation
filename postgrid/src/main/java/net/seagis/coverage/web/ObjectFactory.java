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
package net.seagis.coverage.web;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * Factory methods for each Java content interface and Java element interface generated in the
 * {@code net.opengis.ogc} package. Allows to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content can consist of
 * schema derived interfaces and classes representing the binding of schema type definitions,
 * element declarations and model groups.  Factory methods for each of these are provided in
 * this class.
 *
 * @author Guilhem Legal
 * @author Martin Desruisseaux
 */
@XmlRegistry
public final class ObjectFactory {
    
    private final static QName _Expression_QNAME = new QName("http://www.opengis.net/ogc", "expression");
    
    
    /**
     * Creates a new ObjectFactory that can be used to create new instances of schema derived
     * classes for package {@code net.opengis.ogc}.
     */
    public ObjectFactory() {
    }

    /**
     * Creates an instance of {@link ServiceExceptionType }
     */
    public ServiceExceptionType createServiceExceptionType() {
        return new ServiceExceptionType();
    }

    /**
     * Creates an instance of {@link ServiceExceptionReport }
     */
    public ServiceExceptionReport createServiceExceptionReport() {
        return new ServiceExceptionReport();
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExpressionType }{@code >}}
     * 
     
    @XmlElementDecl(namespace = "http://www.opengis.net/ogc", name = "expression")
    public JAXBElement<ExpressionType> createExpression(ExpressionType value) {
        return new JAXBElement<ExpressionType>(_Expression_QNAME, ExpressionType.class, null, value);
    }
     * 
     */
    
}
