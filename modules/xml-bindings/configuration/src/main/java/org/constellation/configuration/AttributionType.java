/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.Objects;

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
            return Objects.equals(this.logoURL,        that.logoURL) &&
                   Objects.equals(this.onlineResource, that.onlineResource) &&
                   Objects.equals(this.title,          that.title);
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
