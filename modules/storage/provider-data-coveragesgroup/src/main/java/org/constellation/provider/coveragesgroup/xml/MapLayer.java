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
package org.constellation.provider.coveragesgroup.xml;

import org.constellation.configuration.GetFeatureInfoCfg;
import org.geotoolkit.sld.xml.v110.StyledLayerDescriptor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a layer, with its provider, its name and its style.
 *
 * @author Cédric Briançon (Geomatys)
 * @author Quentin Boileau (Geomatys)
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "dataReference",
    "styleReference",
    "style",
    "opacity",
    "getFeatureInfoCfgs"
})
@XmlRootElement(name = "MapLayer")
public class MapLayer extends MapItem {
    @XmlElement(name = "dataReference", required = true)
    private DataReference dataReference;

    @XmlElement(name = "styleReference")
    private StyleReference styleReference;

    @XmlElement(name = "style")
    private StyledLayerDescriptor style;
    
    @XmlElement(name = "opacity")
    private Double opacity;

    @XmlElementWrapper(name="featureInfos")
    @XmlElement(name="FeatureInfo")
    private List<GetFeatureInfoCfg> getFeatureInfoCfgs;

    public MapLayer(){
    }

    public MapLayer(final DataReference dataReference, final StyleReference styleReference) {
        this.dataReference = dataReference;
        this.styleReference = styleReference;
    }

    public MapLayer(final DataReference dataReference, final StyledLayerDescriptor style) {
        this.dataReference = dataReference;
        this.style = style;
    }

    public DataReference getDataReference() {
        return dataReference;
    }

    public void setDataReference(final DataReference dataReference) {
        this.dataReference = dataReference;
    }

    public StyleReference getStyleReference() {
        return styleReference;
    }

    public void setReferenceStyle(final StyleReference styleReference) {
        this.styleReference = styleReference;
    }

    public StyledLayerDescriptor getStyle() {
        return style;
    }

    public void setStyle(StyledLayerDescriptor style) {
        this.style = style;
    }

    public Double getOpacity() {
        return opacity;
    }

    public void setOpacity(Double opacity) {
        this.opacity = opacity;
    }

    public List<GetFeatureInfoCfg> getGetFeatureInfoCfgs() {
        if (getFeatureInfoCfgs == null) {
            getFeatureInfoCfgs = new ArrayList<GetFeatureInfoCfg>();
        }
        return getFeatureInfoCfgs;
    }

    public void setGetFeatureInfoCfgs(final List<GetFeatureInfoCfg> getFeatureInfoCfgs) {
        this.getFeatureInfoCfgs = getFeatureInfoCfgs;
    }
    
}
