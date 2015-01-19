/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.constellation.json.metadata.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.constellation.json.metadata.binding.Block;
import org.constellation.json.metadata.binding.Field;

/**
 *
 * @author guilhem
 */
public class ValueNode {
    
    List<ValueNode> children = new ArrayList<>();
    
    String name;
    
    String type;
    
    String path;
    
    String blockName;
    
    ValueNode parent;
    
    String defaultValue;
    
    String value;
    
    String render;
    
    int ordinal = 0;
    
    boolean strict = false;
    
    boolean multiple = false;
    
    private String numeratedPath; // must be computed
    
    public ValueNode(String path, String type, int ordinal, ValueNode parent, String blockName) {
        this.path      = path;
        this.type      = type;
        this.blockName = blockName;
        if (path.indexOf('.') != -1) {
            this.name = path.substring(path.lastIndexOf('.') + 1, path.length());
        } else {
            this.name = path;
        }
        this.ordinal = ordinal;
        if (parent != null) {
            parent.addChild(this);
        }
    }
    
    public ValueNode(String path, String type, String defaultValue, String render, int ordinal, String value, ValueNode parent) {
        this.path = path;
        this.type = type;
        if (path.indexOf('.') != -1) {
            this.name = path.substring(path.lastIndexOf('.') + 1, path.length());
        } else {
            this.name = path;
        }
        this.defaultValue = defaultValue;
        this.render       = render;
        this.ordinal      = ordinal;
        this.value        = value;
        if (parent != null) {
            parent.addChild(this);
        }
    }
    
    
    
    public ValueNode(ValueNode node, ValueNode parent, int ordinal) {
        this.path         = node.path;
        this.name         = node.name;
        this.type         = node.type;
        this.render       = node.render;
        this.value        = node.value;
        this.defaultValue = node.defaultValue;
        this.strict       = node.strict;
        this.blockName    = node.blockName;
        this.multiple     = node.multiple;
        this.ordinal      = ordinal;
        this.parent       = parent;
        this.parent.addChild(this);
    }
    
    public ValueNode(ValueNode node) {
        this.path         = node.path;
        this.name         = node.name;
        this.type         = node.type;
        this.render       = node.render;
        this.value        = node.value;
        this.defaultValue = node.defaultValue;
        this.ordinal      = node.ordinal;
        this.parent       = node.parent;
        this.strict       = node.strict;
        this.blockName    = node.blockName;
        this.multiple     = node.multiple;
        for (ValueNode child : node.children) {
            this.children.add(new ValueNode(child));
        }
    }
    
    public ValueNode(Block block, int ordinal) {
        this.path = block.getPath();
        this.type         = block.getType();
        this.render       = block.getRender();
        this.strict       = block.isStrict();
        this.blockName    = block.getName();
        this.multiple     = block.getMultiplicity() > 1;
        this.ordinal      = ordinal;
        if (path.indexOf('.') != -1) {
            this.name = path.substring(path.lastIndexOf('.') + 1, path.length());
        } else {
            this.name = path;
        }
    }
    
    public ValueNode(Field field, int ordinal) {
        this.path = field.getPath();
        this.type         = field.getType();
        this.render       = field.getRender();
        this.defaultValue = field.getDefaultValue();
        this.value        = field.getValue();
        this.multiple     = field.getMultiplicity() > 1;
        this.ordinal      = ordinal;
        if (path.indexOf('.') != -1) {
            this.name = path.substring(path.lastIndexOf('.') + 1, path.length());
        } else {
            this.name = path;
        }
    }

    public void addChild(ValueNode child) {
        child.parent = this;
        children.add(child);
    }
    
    public boolean isField() {
        return children.isEmpty();
    }
    
    public boolean hashParent(final ValueNode n) {
        if (parent != null) {
            if (parent.simpleEquals(n)) {
                return true;
            } else {
                return parent.hashParent(n);
            }
        }
        return false;
    }
    
    public boolean simpleEquals(ValueNode n) {
        return this.getNumeratedPath().equals(n.getNumeratedPath());
    }
    
    public String getNumeratedPath() {
        if (numeratedPath == null) {
            if (parent != null) {
                numeratedPath = parent.getNumeratedPath() + '.' + name + "[" + ordinal + "]";
            } else {
                numeratedPath = name + "[" + ordinal + "]";
            }
        }
        return numeratedPath;
    }
    
    public List<ValueNode> getChildrenByName(String name) {
        final List<ValueNode> results = new ArrayList<>();
        for (ValueNode node : children) {
            if (node.name.equals(name)) {
                results.add(node);
            }
        }
        return results;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[ValueNode]\n");
        sb.append("name:").append(name).append('\n');
        sb.append("ordinal:").append(ordinal).append('\n');
        if (type != null) {
            sb.append("type:").append(type).append('\n');
        }
        sb.append("path:").append(path).append('\n');
        if (parent != null) {
            sb.append("parent:").append(parent.path).append('[').append(parent.ordinal).append("]\n");
        }
        sb.append("children:").append(children.size()).append('\n');
        sb.append("render:").append(render).append('\n');
        sb.append("value:").append(value).append('\n');
        sb.append("default value:").append(defaultValue).append('\n');
        sb.append("strict:").append(strict).append('\n');
        if (blockName != null){
            sb.append("block name:").append(blockName).append('\n');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.children);
        hash = 97 * hash + Objects.hashCode(this.name);
        hash = 97 * hash + Objects.hashCode(this.type);
        hash = 97 * hash + Objects.hashCode(this.path);
        hash = 97 * hash + Objects.hashCode(this.defaultValue);
        hash = 97 * hash + Objects.hashCode(this.value);
        hash = 97 * hash + Objects.hashCode(this.render);
        hash = 97 * hash + Objects.hashCode(this.strict);
        hash = 97 * hash + Objects.hashCode(this.blockName);
        hash = 97 * hash + this.ordinal;
        return hash;
    }
    
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ValueNode) {
            final ValueNode that = (ValueNode) obj;
            return Objects.equals(this.defaultValue,  that.defaultValue) &&
                   Objects.equals(this.name,          that.name) &&
                   Objects.equals(this.ordinal,       that.ordinal) && 
                   Objects.equals(this.path,          that.path) && 
                   Objects.equals(this.render,        that.render) && 
                   Objects.equals(this.type,          that.type) && 
                   Objects.equals(this.value,         that.value) && 
                   Objects.equals(this.children,      that.children) && 
                   Objects.equals(this.strict,        that.strict) &&
                   Objects.equals(this.blockName,     that.blockName) &&
                   Objects.equals(this.getNumeratedPath(), that.getNumeratedPath());
        }
        return false;
    }
}
