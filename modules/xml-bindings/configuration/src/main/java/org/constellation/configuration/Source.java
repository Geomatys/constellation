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
public class Source {

    @XmlAttribute
    private String id;

    @XmlAttribute(name="load_all")
    private Boolean loadAll;

    @XmlElement
    private LayerList include;

    @XmlElement
    private LayerList exclude;

    public Source() {

    }

    public Source(String id, Boolean loadAll, List<Layer> include, List<Layer> exclude) {
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
    public void setLoadAll(Boolean loadAll) {
        this.loadAll = loadAll;
    }

    /**
     * @return the include
     */
    public List<Layer> getInclude() {
        if (include == null) {
            include = new LayerList();
            return include.getLayer();
        } else {
            return include.getLayer();
        }
    }

    /**
     * @param include the include to set
     */
    public void setInclude(List<Layer> include) {
        this.include = new LayerList(include);
    }

    /**
     * @return the exclude
     */
    public List<Layer> getExclude() {
        if (exclude == null) {
            exclude = new LayerList();
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
    public boolean isExcludedLayer(QName name) {
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
    public Layer isIncludedLayer(QName name) {
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
     * @param exclude the exclude to set
     */
    public void setExclude(List<Layer> exclude) {
        this.exclude = new LayerList(exclude);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Source ");
        sb.append(" id=").append(id);
        sb.append(" LoadAll=").append(loadAll);
        return sb.toString();
    }

}
