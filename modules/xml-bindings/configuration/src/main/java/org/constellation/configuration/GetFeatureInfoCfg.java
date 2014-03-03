/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010 - 2014, Geomatys
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

import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.Objects;

/**
 * Configuration for custom GetFeatureInfo formatter.
 *
 * @author Quentin Boileau (Geomatys)
 * @since 0.9
 */

@XmlAccessorType(XmlAccessType.FIELD)
public class GetFeatureInfoCfg {

    @XmlAttribute
    private String mimeType;

    @XmlAttribute
    private String binding;

    @XmlElementWrapper(name="parameters")
    @XmlElement(name="GFIParam")
    private List<GFIParam> gfiParameter;

    public GetFeatureInfoCfg() {
    }

    public GetFeatureInfoCfg(String mimeType, String binding) {
        this.mimeType = mimeType;
        this.binding = binding;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getBinding() {
        return binding;
    }

    public void setBinding(String binding) {
        this.binding = binding;
    }

    public List<GFIParam> getGfiParameter() {
        return gfiParameter;
    }

    public void setGfiParameter(List<GFIParam> gfiParameter) {
        this.gfiParameter = gfiParameter;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[GetFeatureInfoCfg]");
        if (mimeType != null) {
            sb.append("mimeType:").append(mimeType);
        }
        if (binding != null) {
            sb.append(" binding:").append(binding);
        }
        if (gfiParameter != null) {
            sb.append(" parameters:\n");
            for (GFIParam param : gfiParameter) {
                sb.append('\t').append(param).append('\n');
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof GetFeatureInfoCfg) {
            final GetFeatureInfoCfg that = (GetFeatureInfoCfg) obj;
            return Objects.equals(this.mimeType, that.mimeType) &&
                   Objects.equals(this.binding,  that.binding) &&
                   Objects.equals(this.gfiParameter, that.gfiParameter);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.mimeType != null ? this.mimeType.hashCode() : 0);
        hash = 79 * hash + (this.binding != null ? this.binding.hashCode() : 0);
        hash = 79 * hash + (this.gfiParameter != null ? this.gfiParameter.hashCode() : 0);
        return hash;
    }
}
