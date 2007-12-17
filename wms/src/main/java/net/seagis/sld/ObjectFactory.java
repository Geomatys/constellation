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

package net.seagis.sld;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import net.seagis.wms.OperationType;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the net.opengis.sld package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

   private final static QName _GetLegendGraphic_QNAME = new QName("http://www.opengis.net/sld", "GetLegendGraphic");
    private final static QName _DescribeLayer_QNAME = new QName("http://www.opengis.net/sld", "DescribeLayer");
    private final static QName _UserDefinedSymbolization_QNAME = new QName("http://www.opengis.net/sld", "UserDefinedSymbolization");

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
    public UserDefinedSymbolization createUserDefinedSymbolization() {
        return new UserDefinedSymbolization();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OperationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/sld", name = "GetLegendGraphic", substitutionHeadNamespace = "http://www.opengis.net/wms", substitutionHeadName = "_ExtendedOperation")
    public JAXBElement<OperationType> createGetLegendGraphic(OperationType value) {
        return new JAXBElement<OperationType>(_GetLegendGraphic_QNAME, OperationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OperationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/sld", name = "DescribeLayer", substitutionHeadNamespace = "http://www.opengis.net/wms", substitutionHeadName = "_ExtendedOperation")
    public JAXBElement<OperationType> createDescribeLayer(OperationType value) {
        return new JAXBElement<OperationType>(_DescribeLayer_QNAME, OperationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UserDefinedSymbolization }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/sld", name = "UserDefinedSymbolization", substitutionHeadNamespace = "http://www.opengis.net/wms", substitutionHeadName = "_ExtendedCapabilities")
    public JAXBElement<UserDefinedSymbolization> createUserDefinedSymbolization(UserDefinedSymbolization value) {
        return new JAXBElement<UserDefinedSymbolization>(_UserDefinedSymbolization_QNAME, UserDefinedSymbolization.class, null, value);
    }




}
