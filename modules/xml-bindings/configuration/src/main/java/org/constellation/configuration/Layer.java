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
import org.geotoolkit.util.Utilities;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.6
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Layer {

    @XmlAttribute
    private QName name;

    @XmlAttribute
    private String style;

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

    @XmlElement(name="CRS")
    private List<String> crs;

    public Layer() {

    }

    public Layer(final QName name) {
        this.name = name;
    }

    public Layer(final QName name, final String title, final String abstrac, final List<String> keywords, final FormatURL metadataURL,
            final FormatURL dataURL, final FormatURL authorityURL, final Reference identifier, final AttributionType attribution, final Boolean opaque,
            final List<String> crs)
    {
        this(name, null, title, abstrac, keywords, metadataURL, dataURL, authorityURL, identifier, attribution, opaque, crs);
    }

    public Layer(final QName name, final String style, final String title, final String abstrac, final List<String> keywords, final FormatURL metadataURL,
            final FormatURL dataURL, final FormatURL authorityURL, final Reference identifier, final AttributionType attribution, final Boolean opaque,
            final List<String> crs) {
        this.name         = name;
        this.style        = style;
        this.title        = title;
        this.abstrac      = abstrac;
        this.keywords     = keywords;
        this.metadataURL  = metadataURL;
        this.dataURL      = dataURL;
        this.authorityURL = authorityURL;
        this.identifier   = identifier;
        this.attribution  = attribution;
        this.opaque       = opaque;
        this.crs          = crs;
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
    public void setName(final QName name) {
        this.name = name;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(final String style) {
        this.style = style;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getAbstrac() {
        return abstrac;
    }

    public void setAbstrac(final String abstrac) {
        this.abstrac = abstrac;
    }

    public List<String> getKeywords() {
        if (keywords == null) {
            keywords = new ArrayList<String>();
        }
        return keywords;
    }

    public void setKeywords(final List<String> keywords) {
        this.keywords = keywords;
    }

    public FormatURL getMetadataURL() {
        return metadataURL;
    }

    public void setMetadataURL(final FormatURL metadataURL) {
        this.metadataURL = metadataURL;
    }

    public FormatURL getDataURL() {
        return dataURL;
    }

    public void setDataURL(final FormatURL dataURL) {
        this.dataURL = dataURL;
    }

    public FormatURL getAuthorityURL() {
        return authorityURL;
    }

    public void setAuthorityURL(final FormatURL authorityURL) {
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
    public void setAttribution(final AttributionType attribution) {
        this.attribution = attribution;
    }

    public Boolean getOpaque() {
        return opaque;
    }

    public void setOpaque(final Boolean opaque) {
        this.opaque = opaque;
    }

    /**
     * @return the crs
     */
    public List<String> getCrs() {
        if (crs == null) {
            crs = new ArrayList<String>();
        }
        return crs;
    }

    /**
     * @param crs the crs to set
     */
    public void setCrs(final List<String> crs) {
        this.crs = crs;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[Layer]");
        if (name != null) {
            sb.append("name:\n").append(name).append('\n');
        }
        if (style != null) {
            sb.append("style:\n").append(style).append('\n');
        }
        if (abstrac != null) {
            sb.append("abstract=").append(abstrac).append('\n');
        }
        if (attribution != null) {
            sb.append("attribution:\n").append(attribution).append('\n');
        }
        if (authorityURL != null) {
            sb.append("authorityURL:\n").append(authorityURL).append('\n');
        }
        if (crs != null) {
            sb.append("crs:\n").append(crs).append('\n');
        }
        if (dataURL != null) {
            sb.append("dataURL:\n").append(dataURL).append('\n');
        }
        if (identifier != null) {
            sb.append("identifier:\n").append(identifier).append('\n');
        }
        if (keywords != null) {
            sb.append("keywords:\n").append(keywords).append('\n');
        }
        if (metadataURL != null) {
            sb.append("metadataURL:\n").append(metadataURL).append('\n');
        }
        if (opaque != null) {
            sb.append("opaque:\n").append(opaque).append('\n');
        }
        if (title != null) {
            sb.append("title:\n").append(title).append('\n');
        }
        return sb.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Layer) {
            final Layer that = (Layer) obj;
            return Utilities.equals(this.abstrac,      that.abstrac) &&
                   Utilities.equals(this.attribution,  that.attribution) &&
                   Utilities.equals(this.authorityURL, that.authorityURL) &&
                   Utilities.equals(this.crs,          that.crs) &&
                   Utilities.equals(this.dataURL,      that.dataURL) &&
                   Utilities.equals(this.identifier,   that.identifier) &&
                   Utilities.equals(this.keywords,     that.keywords) &&
                   Utilities.equals(this.metadataURL,  that.metadataURL) &&
                   Utilities.equals(this.name,         that.name) &&
                   Utilities.equals(this.style,        that.style) &&
                   Utilities.equals(this.opaque,       that.opaque) &&
                   Utilities.equals(this.title,        that.title);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 79 * hash + (this.style != null ? this.style.hashCode() : 0);
        hash = 79 * hash + (this.title != null ? this.title.hashCode() : 0);
        hash = 79 * hash + (this.abstrac != null ? this.abstrac.hashCode() : 0);
        hash = 79 * hash + (this.keywords != null ? this.keywords.hashCode() : 0);
        hash = 79 * hash + (this.metadataURL != null ? this.metadataURL.hashCode() : 0);
        hash = 79 * hash + (this.dataURL != null ? this.dataURL.hashCode() : 0);
        hash = 79 * hash + (this.authorityURL != null ? this.authorityURL.hashCode() : 0);
        hash = 79 * hash + (this.identifier != null ? this.identifier.hashCode() : 0);
        hash = 79 * hash + (this.attribution != null ? this.attribution.hashCode() : 0);
        hash = 79 * hash + (this.opaque != null ? this.opaque.hashCode() : 0);
        hash = 79 * hash + (this.crs != null ? this.crs.hashCode() : 0);
        return hash;
    }
}
