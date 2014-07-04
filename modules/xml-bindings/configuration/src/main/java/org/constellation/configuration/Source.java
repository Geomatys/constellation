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
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.6
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Source {

    @XmlAttribute
    private String id;

    @XmlAttribute(name="load_all")
    private Boolean loadAll;

    @XmlAttribute
    private String date;

    @XmlAttribute
    private String providerType;

    @XmlElement
    private LayerList include;

    @XmlElement
    private LayerList exclude;

    public Source() {

    }

    public Source(final String id, final Boolean loadAll, final List<Layer> include, final List<Layer> exclude) {
        this.id      = id;
        this.loadAll = loadAll;
        if (exclude != null) {
            this.exclude = new LayerList(exclude);
        }
        if (include != null) {
            this.include = new LayerList(include);
        }
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the loadAll
     */
    public Boolean getLoadAll() {
        if (loadAll == null) {
            return false;
        }
        return loadAll;
    }

    /**
     * @param loadAll the loadAll to set
     */
    public void setLoadAll(final Boolean loadAll) {
        if (loadAll != null && !loadAll) {
            this.include = new LayerList();
        }
        this.loadAll = loadAll;
    }

    public String getDate() {
        return date;
    }

    public void setDate(final String date) {
        this.date = date;
    }

    public String getProviderType() {
        return providerType;
    }

    public void setProviderType(final String providerType) {
        this.providerType = providerType;
    }

    /**
     * @return the include
     */
    public List<Layer> getInclude() {
        if (include == null) {
            include = new LayerList(new ArrayList<Layer>());
            return include.getLayer();
        } else {
            return include.getLayer();
        }
    }

    /**
     * @param include the include to set
     */
    public void setInclude(final List<Layer> include) {
        this.include = new LayerList(include);
    }

    /**
     * @return the exclude
     */
    public List<Layer> getExclude() {
        if (exclude == null) {
            exclude = new LayerList(new ArrayList<Layer>());
            return exclude.getLayer();
        } else {
            return exclude.getLayer();
        }
    }

    /**
     * Return true if the specified layer is excluded from the source.
     * @param name
     * @return
     */
    public boolean isExcludedLayer(final QName name) {
        for (Layer layer : getExclude()) {
            final QName layerName = layer.getName();
            /*
             * fix an xml bug with QName
             * when xmlns is set to "http://www.constellation.org/config" in the layer context,
             * the layer take this as a namespace
             */
            if (layerName != null) {
                if (layerName.getNamespaceURI() != null && layerName.getNamespaceURI().equals("http://www.constellation.org/config")) {
                    if (layerName.getLocalPart().equals(name.getLocalPart())) {
                        return true;
                    }
                } else if (layerName.equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Return the layer object if the specified layer is included from the source.
     * or {@code null} else;
     *
     * @param name
     * @return
     */
    public Layer isIncludedLayer(final QName name) {
        for (Layer layer : getInclude()) {
            final QName layerName = layer.getName();
            /*
             * fix an xml bug with QName
             * when xmlns is set to "http://www.constellation.org/config" in the layer context,
             * the layer take this as a namespace
             */
            if (layerName != null) {
                if (layerName.getNamespaceURI() != null && layerName.getNamespaceURI().equals("http://www.constellation.org/config")) {
                    if (layerName.getLocalPart().equals(name.getLocalPart())) {
                        return layer;
                    }
                } else if (layerName.equals(name)) {
                    return layer;
                }
            }
        }
        return null;
    }
    
    /**
     * Return all layer objects if the specified layer name is included from the source.
     * or {@code null} else;
     *
     * @param name
     * @return
     */
    public List<Layer> allIncludedLayer(final QName name) {
        final List<Layer> layers = new ArrayList<>();
        for (Layer layer : getInclude()) {
            final QName layerName = layer.getName();
            /*
             * fix an xml bug with QName
             * when xmlns is set to "http://www.constellation.org/config" in the layer context,
             * the layer take this as a namespace
             */
            if (layerName != null) {
                if (layerName.getNamespaceURI() != null && layerName.getNamespaceURI().equals("http://www.constellation.org/config")) {
                    if (layerName.getLocalPart().equals(name.getLocalPart())) {
                        layers.add(layer);
                    }
                } else if (layerName.equals(name)) {
                    layers.add(layer);
                }
            }
        }
        return layers;
    }

    /**
     * @param exclude the exclude to set
     */
    public void setExclude(final List<Layer> exclude) {
        this.exclude = new LayerList(exclude);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Source ");
        sb.append(" id=").append(id);
        sb.append(" LoadAll=").append(loadAll).append('\n');
        if (include != null) {
            sb.append("include:\n").append(include);
        }
        if (exclude != null) {
            sb.append("exclude:\n").append(exclude);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Source) {
            final Source that = (Source) obj;
            return Objects.equals(this.exclude, that.exclude) &&
                   Objects.equals(this.id,      that.id)      &&
                   Objects.equals(this.include, that.include) &&
                   Objects.equals(this.loadAll, that.loadAll);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 29 * hash + (this.loadAll != null ? this.loadAll.hashCode() : 0);
        hash = 29 * hash + (this.include != null ? this.include.hashCode() : 0);
        hash = 29 * hash + (this.exclude != null ? this.exclude.hashCode() : 0);
        return hash;
    }

}
