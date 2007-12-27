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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import net.seagis.ows.OnlineResourceType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/wcs/1.1.1}CoverageSummary" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="SupportedCRS" type="{http://www.w3.org/2001/XMLSchema}anyURI" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="SupportedFormat" type="{http://www.opengis.net/ows/1.1}MimeType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="OtherSource" type="{http://www.opengis.net/ows/1.1}OnlineResourceType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "coverageSummary",
    "supportedCRS",
    "supportedFormat",
    "otherSource"
})
@XmlRootElement(name = "Contents")
public class Contents {

    @XmlElement(name = "CoverageSummary")
    protected List<CoverageSummaryType> coverageSummary;
    @XmlElement(name = "SupportedCRS")
    @XmlSchemaType(name = "anyURI")
    protected List<String> supportedCRS;
    @XmlElement(name = "SupportedFormat")
    protected List<String> supportedFormat;
    @XmlElement(name = "OtherSource")
    protected List<OnlineResourceType> otherSource;

    /**
     * Unordered list of brief metadata describing top-level coverages available from this WCS server. This list shall be included unless one or more OtherSources are referenced and all this metadata is available from those sources. Gets the value of the coverageSummary property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the coverageSummary property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCoverageSummary().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CoverageSummaryType }
     * 
     * 
     */
    public List<CoverageSummaryType> getCoverageSummary() {
        if (coverageSummary == null) {
            coverageSummary = new ArrayList<CoverageSummaryType>();
        }
        return this.coverageSummary;
    }

    /**
     * Gets the value of the supportedCRS property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the supportedCRS property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSupportedCRS().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getSupportedCRS() {
        if (supportedCRS == null) {
            supportedCRS = new ArrayList<String>();
        }
        return this.supportedCRS;
    }

    /**
     * Gets the value of the supportedFormat property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the supportedFormat property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSupportedFormat().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getSupportedFormat() {
        if (supportedFormat == null) {
            supportedFormat = new ArrayList<String>();
        }
        return this.supportedFormat;
    }

    /**
     * Gets the value of the otherSource property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the otherSource property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOtherSource().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OnlineResourceType }
     * 
     * 
     */
    public List<OnlineResourceType> getOtherSource() {
        if (otherSource == null) {
            otherSource = new ArrayList<OnlineResourceType>();
        }
        return this.otherSource;
    }

}
