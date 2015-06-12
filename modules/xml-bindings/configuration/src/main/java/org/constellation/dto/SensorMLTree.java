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

package org.constellation.dto;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Guilhem Legal
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SensorMLTree {

    private String type;

    private String id;

    private String owner;
    
    private List<SensorMLTree> children;
    
    @XmlTransient
    private SensorMLTree parent;
    
    private Date createDate;

    public SensorMLTree() {
        
    }
    
    public SensorMLTree(final String id, final String type, final String owner, final Long time) {
        this.id   = id;
        this.type = type;
        this.owner = owner;
        if (time != null) {
            this.createDate = new Date(time);
        }
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
     * @return the creationDate
     */
    public Date getCreateDate() {
        return createDate;
    }

    /**
     * @param creationDate the creationDate to set
     */
    public void setCreateDate(Date creationDate) {
        this.createDate = creationDate;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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
        final SensorMLTree root = new SensorMLTree("root", "System", null, null);
        
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
        hash = 47 * hash + Objects.hashCode(this.createDate);
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
            return Objects.equals(this.id,           that.id)   &&
                   Objects.equals(this.createDate, that.createDate)   &&
                   Objects.equals(this.type,         that.type);
        }
        return false;
    }
    
    public String toString() {
        final StringBuilder sb = new StringBuilder("[SensorMLTree]\n");
        if (id != null) {
            sb.append("id=").append(id).append("\n");
        }
        if (type != null) {
            sb.append("type=").append(type).append("\n");
        }
        if (createDate != null) {
            sb.append("creationDate=").append(createDate).append("\n");
        }
        if (parent != null) {
            sb.append("parent=").append(parent.id).append("\n");
        }
        if (children != null) {
            sb.append("children:");
            for (SensorMLTree child : children) {
                sb.append(child.id).append("\n");
            }
        }
        return sb.toString();
    }
}
