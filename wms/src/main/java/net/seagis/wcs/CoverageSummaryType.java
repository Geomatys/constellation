/*
 * Sicade - SystÃ¨mes intÃ©grÃ©s de connaissances pour l'aide Ã  la dÃ©cision en environnement
 * (C) 2005, Institut de Recherche pour le DÃ©veloppement
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


package net.seagis.wcs;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlType;
import net.seagis.ows.DescriptionType;
import net.seagis.ows.MetadataType;
import net.seagis.ows.WGS84BoundingBoxType;


/**
 * Brief metadata describing one or more coverages available from this WCS server. 
 * 
 * <p>Java class for CoverageSummaryType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CoverageSummaryType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/ows/1.1}DescriptionType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}Metadata" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}WGS84BoundingBox" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="SupportedCRS" type="{http://www.w3.org/2001/XMLSchema}anyURI" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="SupportedFormat" type="{http://www.opengis.net/ows/1.1}MimeType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;sequence>
 *             &lt;element ref="{http://www.opengis.net/wcs/1.1.1}CoverageSummary" maxOccurs="unbounded"/>
 *             &lt;element ref="{http://www.opengis.net/wcs/1.1.1}Identifier" minOccurs="0"/>
 *           &lt;/sequence>
 *           &lt;element ref="{http://www.opengis.net/wcs/1.1.1}Identifier"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CoverageSummaryType", propOrder = {
    "rest"
})
public class CoverageSummaryType extends DescriptionType {

    @XmlElementRefs({
        @XmlElementRef(name = "CoverageSummary", namespace = "http://www.opengis.net/wcs/1.1.1", type = JAXBElement.class),
        @XmlElementRef(name = "SupportedFormat", namespace = "http://www.opengis.net/wcs/1.1.1", type = JAXBElement.class),
        @XmlElementRef(name = "Identifier", namespace = "http://www.opengis.net/wcs/1.1.1", type = JAXBElement.class),
        @XmlElementRef(name = "SupportedCRS", namespace = "http://www.opengis.net/wcs/1.1.1", type = JAXBElement.class),
        @XmlElementRef(name = "WGS84BoundingBox", namespace = "http://www.opengis.net/ows/1.1", type = JAXBElement.class),
        @XmlElementRef(name = "Metadata", namespace = "http://www.opengis.net/ows/1.1", type = JAXBElement.class)
    })
    protected List<JAXBElement<?>> rest  = new ArrayList<JAXBElement<?>>();

    /**
     * Gets the rest of the content model. 
     * 
     * <p>
     * You are getting this "catch-all" property because of the following reason: 
     * The field name "Identifier" is used by two different parts of a schema. See: 
     * line 95 of file:/C:/Documents%20and%20Settings/jcd/Bureau/07-067r1%20WCS%201.1.1%20draft/wcs/1.1.1/wcsContents.xsd
     * line 89 of file:/C:/Documents%20and%20Settings/jcd/Bureau/07-067r1%20WCS%201.1.1%20draft/wcs/1.1.1/wcsContents.xsd
     * <p>
     * To get rid of this property, apply a property customization to one 
     * of both of the following declarations to change their names: 
     * Gets the value of the rest property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rest property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRest().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link CoverageSummaryType }{@code >}
     * {@link JAXBElement }{@code <}{@link WGS84BoundingBoxType }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link MetadataType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<?>> getRest() {
        return this.rest;
    }

}
