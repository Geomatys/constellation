/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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

package org.constellation.cat.wrs;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlType;


/**
 * Extends rim:ExtrinsicObjectType to add the following:
 *  1. MTOM/XOP based attachment support.
 *  2. XLink based reference to a part in a multipart/related message structure.
 * 
 * NOTE: This content model is planned for RegRep 4.0.
 *       
 * 
 * <p>Java class for ExtrinsicObjectType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ExtrinsicObjectType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0}ExtrinsicObjectType">
 *       &lt;choice minOccurs="0">
 *         &lt;element name="repositoryItemRef" type="{http://www.opengis.net/cat/wrs/1.0}SimpleLinkType"/>
 *         &lt;element name="repositoryItem" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *       &lt;/choice>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExtrinsicObjectType", propOrder = {
    "repositoryItemRef",
    "repositoryItem"
})
public class ExtrinsicObjectType extends org.constellation.ebrim.v300.ExtrinsicObjectType {

    private SimpleLinkType repositoryItemRef;
    @XmlMimeType("*/*")
    private DataHandler repositoryItem;

    /**
     * Gets the value of the repositoryItemRef property.
     */
    public SimpleLinkType getRepositoryItemRef() {
        return repositoryItemRef;
    }

    /**
     * Sets the value of the repositoryItemRef property.
     */
    public void setRepositoryItemRef(SimpleLinkType value) {
        this.repositoryItemRef = value;
    }

    /**
     * Gets the value of the repositoryItem property.
     */
    public DataHandler getRepositoryItem() {
        return repositoryItem;
    }

    /**
     * Sets the value of the repositoryItem property.
     */
    public void setRepositoryItem(DataHandler value) {
        this.repositoryItem = value;
    }

}
