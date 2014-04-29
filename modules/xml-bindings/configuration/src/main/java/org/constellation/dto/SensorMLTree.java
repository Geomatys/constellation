/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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

package org.constellation.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Guilhem Legal
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SensorMLTree {
    
    private String type;
    
    private String id;
    
    private List<SensorMLTree> children;

    public SensorMLTree() {
        
    }
    
    public SensorMLTree(final String id, final String type) {
        this.id   = id;
        this.type = type;
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
     * @return the children
     */
    public List<SensorMLTree> getChildren() {
        if (children == null) {
            children = new ArrayList<>();
        }
        return children;
    }

    /**
     * @param children the children to set
     */
    public void setChildren(List<SensorMLTree> children) {
        this.children = children;
    }
    
    public void replaceChildren(final SensorMLTree newChild) {
        if (children == null) {
            children = new ArrayList<>();
        }
        for (SensorMLTree child : children) {
            if (newChild.getId().equals(child.getId())) {
                children.remove(child);
                children.add(newChild);
                return;
            }
        }
        throw new IllegalArgumentException("No child to replace:" + newChild.getId());
    }

    public boolean hasChild(final String id) {
        if (children == null) {
            children = new ArrayList<>();
        }
        for (SensorMLTree child : children) {
            if (id.equals(child.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Objects.hashCode(this.type);
        hash = 47 * hash + Objects.hashCode(this.id);
        return hash;
    }
    
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (object instanceof SensorMLTree) {
            final SensorMLTree that = (SensorMLTree) object;
            return Objects.equals(this.id,    that.id)   &&
                   Objects.equals(this.type, that.type);
        }
        return false;
    }
}
