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

package org.constellation.wps.v100;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.constellation.ows.v110.LanguageStringType;


/**
 * Definition of a format, encoding,  schema, and unit-of-measure for an output to be returned from a process. 
 * 
 * In this use, the DescriptionType shall describe this process input or output. 
 * 
 * <p>Java class for DocumentOutputDefinitionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DocumentOutputDefinitionType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/wps/1.0.0}OutputDefinitionType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}Title" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}Abstract" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="asReference" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DocumentOutputDefinitionType", propOrder = {
    "title",
    "_abstract"
})
public class DocumentOutputDefinitionType
    extends OutputDefinitionType
{

    @XmlElement(name = "Title", namespace = "http://www.opengis.net/ows/1.1")
    protected LanguageStringType title;
    @XmlElement(name = "Abstract", namespace = "http://www.opengis.net/ows/1.1")
    protected LanguageStringType _abstract;
    @XmlAttribute
    protected Boolean asReference;

    /**
     * Title of the process output, normally available for display to a human. This element should be used if the client wishes to customize the Title in the execute response. This element should not be used if the Title provided for this output in the ProcessDescription is adequate. 
     * 
     * @return
     *     possible object is
     *     {@link LanguageStringType }
     *     
     */
    public LanguageStringType getTitle() {
        return title;
    }

    /**
     * Title of the process output, normally available for display to a human. This element should be used if the client wishes to customize the Title in the execute response. This element should not be used if the Title provided for this output in the ProcessDescription is adequate. 
     * 
     * @param value
     *     allowed object is
     *     {@link LanguageStringType }
     *     
     */
    public void setTitle(LanguageStringType value) {
        this.title = value;
    }

    /**
     * Brief narrative description of a process output, normally available for display to a human. This element should be used if the client wishes to customize the Abstract in the execute response. This element should not be used if the Abstract provided for this output in the ProcessDescription is adequate. 
     * 
     * @return
     *     possible object is
     *     {@link LanguageStringType }
     *     
     */
    public LanguageStringType getAbstract() {
        return _abstract;
    }

    /**
     * Brief narrative description of a process output, normally available for display to a human. This element should be used if the client wishes to customize the Abstract in the execute response. This element should not be used if the Abstract provided for this output in the ProcessDescription is adequate. 
     * 
     * @param value
     *     allowed object is
     *     {@link LanguageStringType }
     *     
     */
    public void setAbstract(LanguageStringType value) {
        this._abstract = value;
    }

    /**
     * Gets the value of the asReference property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAsReference() {
        if (asReference == null) {
            return false;
        } else {
            return asReference;
        }
    }

    /**
     * Sets the value of the asReference property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAsReference(Boolean value) {
        this.asReference = value;
    }

}
