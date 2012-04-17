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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import org.geotoolkit.util.Utilities;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.6
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class AttributionType {

    @XmlElement(name="Title")
    private String title;

    @XmlElement(name="OnlineResource")
    private Reference onlineResource;

    @XmlElement(name="LogoURL")
    private FormatURL logoURL;

    public AttributionType() {

    }

    public AttributionType(String title, Reference onlineResource, FormatURL logoURL) {
        this.title          = title;
        this.onlineResource = onlineResource;
        this.logoURL        = logoURL;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the onlineResource
     */
    public Reference getOnlineResource() {
        return onlineResource;
    }

    /**
     * @param onlineResource the onlineResource to set
     */
    public void setOnlineResource(Reference onlineResource) {
        this.onlineResource = onlineResource;
    }

    /**
     * @return the logoURL
     */
    public FormatURL getLogoURL() {
        return logoURL;
    }

    /**
     * @param logoURL the logoURL to set
     */
    public void setLogoURL(FormatURL logoURL) {
        this.logoURL = logoURL;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[AttributionType]");
        if (logoURL != null) {
            sb.append("logoURL=").append(logoURL).append('\n');
        }
        if (onlineResource != null) {
            sb.append("onlineResource=").append(onlineResource).append('\n');
        }
        if (title != null) {
            sb.append("title=").append(title).append('\n');
        }
        return sb.toString();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof AttributionType) {
            final AttributionType that = (AttributionType) obj;
            return Utilities.equals(this.logoURL,        that.logoURL) &&
                   Utilities.equals(this.onlineResource, that.onlineResource) &&
                   Utilities.equals(this.title,          that.title);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + (this.title != null ? this.title.hashCode() : 0);
        hash = 23 * hash + (this.onlineResource != null ? this.onlineResource.hashCode() : 0);
        hash = 23 * hash + (this.logoURL != null ? this.logoURL.hashCode() : 0);
        return hash;
    }
}
