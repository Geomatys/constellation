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

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.6
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Source {

    @XmlAttribute
    private String id;

    @XmlAttribute(name="load-all")
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
     * @param exclude the exclude to set
     */
    public void setExclude(List<Layer> exclude) {
        this.exclude = new LayerList(exclude);
    }
}
