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

package org.constellation.json.metadata.binding;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Pojo class used for Jackson that represents the binding for block
 * in metadata template json.
 *
 * @author Mehdi Sidhoum (Geomatys).
 * @since 0.9
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class Block implements Serializable, ChildEntity, IBlock {

    private String name;
    private int multiplicity;
    private String help;
    private String path;
    private String render;
    private String ignore;
    private String type;
    private boolean strict;
    private List<ComponentObj> children;
    
    public Block(){
        this.children = new ArrayList<>();
    }
    
    public Block(Block block){
        this.name         = block.name;
        this.multiplicity = block.multiplicity;
        this.help         = block.help;
        this.path         = block.path;
        this.type         = block.type;
        this.render       = block.render;
        this.ignore       = block.ignore;
        this.strict       = block.strict;
        this.children     = new ArrayList<>();
        for (ComponentObj child : block.children) {
            if (child instanceof FieldObj) {
                this.children.add(new FieldObj((FieldObj)child));
            } else {
                this.children.add(new BlockObj((BlockObj)child));
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMultiplicity() {
        return multiplicity;
    }

    public void setMultiplicity(int multiplicity) {
        this.multiplicity = multiplicity;
    }

    public List<ComponentObj> getChildren() {
        return children;
    }

    public void setChildren(List<ComponentObj> children) {
        this.children = children;
    }
    
    public Field addField(int index, Field field) {
        children.add(index, new FieldObj(field));
        return field;
    }
    
    public void removeField(FieldObj fieldObj) {
        children.remove(fieldObj);
    }
    
    @Override
    public Block addBlock(int index, Block block) {
        children.add(index, new BlockObj(block));
        return block;
    }
    
    @Override
    public void removeBlock(BlockObj blockObj) {
        children.remove(blockObj);
    }

    @Override
    public boolean childrenEmpty() {
        if (children != null) {
            return children.isEmpty();
        }
        return true;
    }
    
    public ComponentObj getChildrenByPath(final String path) {
        for (ComponentObj b : children) {
            if (path.equals(b.getPath())) {
                return b;
            }
        }
        return null;
    }
    
    public String getHelp() {
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public String getPath() {
        return path;
    }
    
    public void updatePath(int ordinal) {
        final String oldPrefix;
        if (path.endsWith("+")) {
            oldPrefix = path.substring(0, path.length() - 1); // remove the '+'
        } else {
            oldPrefix = path;
        }
        path = path.substring(0, path.lastIndexOf('['));
        path = path + '[' + ordinal + ']';
        for (ComponentObj cp : children) {
            cp.updatePath(oldPrefix, path);
        }
    }
    
    public void updatePath(String oldPrefix, String newPrefix) {
        path = path.replace(oldPrefix, newPrefix);
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRender() {
        return render;
    }

    public void setRender(String render) {
        this.render = render;
    }

    public String getIgnore() {
        return ignore;
    }

    public void setIgnore(String ignore) {
        this.ignore = ignore;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the strict
     */
    public boolean isStrict() {
        return strict;
    }

    /**
     * @param strict the strict to set
     */
    public void setStrict(boolean strict) {
        this.strict = strict;
    }
    
    public static Block diff(Block original, Block modified) {
        final Block result = new Block();
        boolean add = false;
        for (ComponentObj originalChild : original.children) {
            if (originalChild instanceof BlockObj) {
                final BlockObj originalB     = (BlockObj) originalChild;
                final ComponentObj modifiedB = modified.getChildrenByPath(originalB.getBlock().getPath());
                if (modifiedB == null) {
                    result.getChildren().add(new BlockObjDiff(originalB, "REMOVED"));
                    add = true;
                } else if (modifiedB instanceof BlockObj){
                    final BlockObj modif = BlockObj.diff(originalB, (BlockObj) modifiedB) ;
                    if (modif != null) {
                        result.getChildren().add(modif);
                        add = true;
                    }
                } else {
                    throw new IllegalStateException("A block is now a field!");
                }
            } else if (originalChild instanceof FieldObj) {
                final FieldObj originalF     = (FieldObj) originalChild;
                final ComponentObj modifiedF = modified.getChildrenByPath(originalF.getField().getPath());
                if (modifiedF == null) {
                    result.getChildren().add(new FieldObjDiff(originalF, "REMOVED"));
                    add = true;
                } else if (modifiedF instanceof FieldObj){
                    if (FieldObj.diff(originalF, (FieldObj) modifiedF)) {
                        result.getChildren().add(new FieldObjDiff(originalF, "REMOVED"));
                        result.getChildren().add(new FieldObjDiff((FieldObj) modifiedF, "ADDED"));
                        add = true;
                    }
                } else {
                    throw new IllegalStateException("A field is now a block!");
                }
            }
        }
        // look for Added child
        for (ComponentObj modifiedChild : modified.children) {
            if (modifiedChild instanceof BlockObj) {
                BlockObj modifiedB = (BlockObj)modifiedChild;
                if (original.getChildrenByPath(modifiedB.getBlock().getPath()) == null) {
                    result.getChildren().add(new BlockObjDiff(modifiedB, "ADDED"));
                    add = true;
                }
            } else if (modifiedChild instanceof FieldObj) {
                FieldObj modifiedF = (FieldObj)modifiedChild;
                if (original.getChildrenByPath(modifiedF.getField().getPath()) == null) {
                    result.getChildren().add(new FieldObjDiff(modifiedF, "ADDED"));
                    add = true;
                }
            }
        }
        if (add) {
            return result;
        }
        return null;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[Block]\n");
        sb.append("name:").append(name).append('\n');
        sb.append("path:").append(path).append('\n');
        sb.append("type:").append(type).append('\n');
        sb.append("strict:").append(strict).append('\n');
        sb.append("multiplicity:").append(multiplicity).append('\n');
        sb.append("children:").append(children.size());
        return sb.toString();
    }
}
