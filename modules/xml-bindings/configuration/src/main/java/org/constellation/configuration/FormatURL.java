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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Objects;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.6
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class FormatURL {

    @XmlAttribute
    private String name;

    @XmlAttribute
    private String type;

    @XmlAttribute
    private Integer width;

    @XmlAttribute
    private Integer height;

    @XmlElement(name="Format")
    private String format;

    @XmlElement(name="OnlineResource")
    private Reference onlineResource;

    public FormatURL() {

    }

    public FormatURL(final String format, final String href) {
        this.format = format;
        if (href != null) {
            this.onlineResource = new Reference(href);
        }
    }

    public FormatURL(final String format, final Reference href) {
        this.format = format;
        this.onlineResource = href;
    }

    public FormatURL(final String name, final String type, final String format, final String href) {
        this.name   = name;
        this.type   = type;
        this.format = format;
        if (href != null) {
            this.onlineResource = new Reference(href);
        }
    }
    public FormatURL(final String name, final String type, final String format, final Reference href) {
        this.name   = name;
        this.type   = type;
        this.format = format;
        this.onlineResource = href;
    }

    public FormatURL(final Integer width, final Integer height, final String format, final String href) {
        this.width  = width;
        this.height = height;
        this.format = format;
        if (href != null) {
            this.onlineResource = new Reference(href);
        }
    }

    public FormatURL(final Integer width,final Integer height, final String format, final Reference ref) {
        this.width  = width;
        this.height = height;
        this.format = format;
        this.onlineResource = ref;
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
     * @return the format
     */
    public String getFormat() {
        return format;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the width
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(Integer width) {
        this.width = width;
    }

    /**
     * @return the height
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(Integer height) {
        this.height = height;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[FormatURL]");
        if (width != null) {
            sb.append("width=").append(width).append('\n');
        }
        if (type != null) {
            sb.append("type=").append(type).append('\n');
        }
        if (onlineResource != null) {
            sb.append("onlineResource=").append(onlineResource).append('\n');
        }
        if (name != null) {
            sb.append("name=").append(name).append('\n');
        }
        if (format != null) {
            sb.append("format=").append(format).append('\n');
        }
        if (height != null) {
            sb.append("height=").append(height).append('\n');
        }
        return sb.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof FormatURL) {
            final FormatURL that = (FormatURL) obj;
            return Objects.equals(this.width,   that.width) &&
                   Objects.equals(this.type,     that.type) &&
                   Objects.equals(this.onlineResource,  that.onlineResource) &&
                   Objects.equals(this.name,         that.name) &&
                   Objects.equals(this.format,       that.format) &&
                   Objects.equals(this.height,       that.height);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 97 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 97 * hash + (this.width != null ? this.width.hashCode() : 0);
        hash = 97 * hash + (this.height != null ? this.height.hashCode() : 0);
        hash = 97 * hash + (this.format != null ? this.format.hashCode() : 0);
        hash = 97 * hash + (this.onlineResource != null ? this.onlineResource.hashCode() : 0);
        return hash;
    }
}
