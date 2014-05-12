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
import javax.xml.bind.annotation.XmlTransient;

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
    
    @XmlTransient
    private SensorMLTree parent;

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
     * @return the parent
     */
    public SensorMLTree getParent() {
        return parent;
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
    
    public void addChildren(final SensorMLTree child) {
        child.parent = this;
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
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
                newChild.parent = this;
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
    
    public SensorMLTree find(final String id) {
        if (this.id.equals(id)) {
            return this;
        }
        for (SensorMLTree child : getChildren()) {
            final SensorMLTree found = child.find(id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
    
    public List<String> getAllChildrenIds() {
        final List<String> results = new ArrayList<>();
        results.add(id);
        for (SensorMLTree child : getChildren()) {
            if (child != null) {
                results.addAll(child.getAllChildrenIds());
            }
        }
        return results;
    }

    public static SensorMLTree buildTree(final List<SensorMLTree> nodeList) {
        final SensorMLTree root = new SensorMLTree("root", "System");
        
        for (SensorMLTree node : nodeList) {
            final SensorMLTree parent = getParent(node, nodeList);
            if (parent == null) {
                root.addChildren(node);
            } else {
                parent.replaceChildren(node);
            }
        }
        return root;
    }
    
    private static SensorMLTree getParent(final SensorMLTree current, final List<SensorMLTree> nodeList) {
        for (SensorMLTree node : nodeList) {
            if (node.hasChild(current.getId())) {
                return node;
            }
        }
        return null;
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
