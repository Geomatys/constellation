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
