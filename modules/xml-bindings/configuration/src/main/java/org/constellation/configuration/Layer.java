/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.namespace.QName;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.6
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Layer {

    @XmlAttribute
    private QName name;

    @XmlElement(name="Title")
    private String title;

    @XmlElement(name="Abstract")
    private String abstrac;

    @XmlElement(name="Keyword")
    private List<String> keywords;

    @XmlElement(name="MetadataURL")
    private FormatURL metadataURL;

    @XmlElement(name="DataURL")
    private FormatURL dataURL;

    @XmlElement(name="AuthorityURL")
    private FormatURL authorityURL;

    @XmlElement(name="Identifier")
    private Reference identifier;

    @XmlElement(name="Attribution")
    private AttributionType attribution;

    @XmlElement(name="Opaque")
    private Boolean opaque;

    public Layer() {

    }

    public Layer(QName name) {
        this.name = name;
    }

    public Layer(QName name, String title, String abstrac, List<String> keywords, FormatURL metadataURL, FormatURL dataURL, FormatURL authorityURL,
            Reference identifier, AttributionType attribution, Boolean opaque) {
        this.name         = name;
        this.title        = title;
        this.abstrac      = abstrac;
        this.keywords     = keywords;
        this.metadataURL  = metadataURL;
        this.dataURL      = dataURL;
        this.authorityURL = authorityURL;
        this.identifier   = identifier;
        this.attribution  = attribution;
        this.opaque       = opaque;
    }
    
    /**
     * @return the name
     */
    public QName getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(QName name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAbstrac() {
        return abstrac;
    }

    public void setAbstrac(String abstrac) {
        this.abstrac = abstrac;
    }

    public List<String> getKeywords() {
        if (keywords == null) {
            keywords = new ArrayList<String>();
        }
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public FormatURL getMetadataURL() {
        return metadataURL;
    }

    public void setMetadataURL(FormatURL metadataURL) {
        this.metadataURL = metadataURL;
    }

    public FormatURL getDataURL() {
        return dataURL;
    }

    public void setDataURL(FormatURL dataURL) {
        this.dataURL = dataURL;
    }

    public FormatURL getAuthorityURL() {
        return authorityURL;
    }

    public void setAuthorityURL(FormatURL authorityURL) {
        this.authorityURL = authorityURL;
    }

    /**
     * @return the identifier
     */
    public Reference getIdentifier() {
        return identifier;
    }

    /**
     * @return the attribution
     */
    public AttributionType getAttribution() {
        return attribution;
    }

    /**
     * @param attribution the attribution to set
     */
    public void setAttribution(AttributionType attribution) {
        this.attribution = attribution;
    }

    public Boolean getOpaque() {
        return opaque;
    }

    public void setOpaque(Boolean opaque) {
        this.opaque = opaque;
    }
}
