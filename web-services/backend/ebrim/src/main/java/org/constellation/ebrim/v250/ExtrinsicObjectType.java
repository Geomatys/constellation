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

package org.constellation.ebrim.v250;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import org.constellation.cat.wrs.v090.WRSExtrinsicObjectType;


/**
 * 
 * ExtrinsicObject are attributes from the ExtrinsicObject interface in ebRIM.
 * It inherits RegistryEntryAttributes
 * 			
 * 
 * <p>Java class for ExtrinsicObjectType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ExtrinsicObjectType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5}RegistryEntryType">
 *       &lt;attribute name="mimeType" type="{urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5}LongName" default="application/octet-stream" />
 *       &lt;attribute name="isOpaque" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExtrinsicObjectType")
@XmlSeeAlso({
    WRSExtrinsicObjectType.class
})
public class ExtrinsicObjectType extends RegistryEntryType {

    @XmlAttribute
    private String mimeType;
    @XmlAttribute
    private Boolean isOpaque;

    /**
     * Gets the value of the mimeType property.
     */
    public String getMimeType() {
        if (mimeType == null) {
            return "application/octet-stream";
        } else {
            return mimeType;
        }
    }

    /**
     * Sets the value of the mimeType property.
     */
    public void setMimeType(String value) {
        this.mimeType = value;
    }

    /**
     * Gets the value of the isOpaque property.
     */
    public boolean isIsOpaque() {
        if (isOpaque == null) {
            return false;
        } else {
            return isOpaque;
        }
    }

    /**
     * Sets the value of the isOpaque property.
     */
    public void setIsOpaque(Boolean value) {
        this.isOpaque = value;
    }

}
