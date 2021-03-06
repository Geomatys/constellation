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
import org.constellation.json.JsonMetadataConstants;

/**
 * Pojo class used for Jackson that represents the binding for superblock
 * in metadata template json.
 *
 * @author Mehdi Sidhoum (Geomatys).
 * @since 0.9
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class SuperBlock implements Serializable, IBlock {

    private String name;
    private int multiplicity;
    private String help;
    private String path;
    private String render;
    private List<BlockObj> children;

    public SuperBlock(){
        this.children = new ArrayList<>();
    }
    
    public SuperBlock(SuperBlock sb){
        this.name         = sb.name;
        this.help         = sb.help;
        this.multiplicity = sb.multiplicity;
        this.path         = sb.path;
        this.render       = sb.render;
        this.children     = new ArrayList<>();
        for (BlockObj bobj : sb.children) {
            this.children.add(new BlockObj(bobj));
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

    public List<BlockObj> getChildren() {
        return children;
    }
    
    public BlockObj getChildrenByPath(final String path) {
        for (BlockObj b : children) {
            if (b.getBlock().getPath().equals(path)) {
                return b;
            }
        }
        return null;
    }

    public void setChildren(List<BlockObj> children) {
        this.children = children;
    }
    
    public List<Block> getBlocks() {
        final List<Block> results = new ArrayList<>();
        for (BlockObj b : children) {
            results.add(b.getBlock());
        }
        return results;
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
        return children.isEmpty();
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

    public void setPath(String path) {
        this.path = path;
    }

    public String getRender() {
        return render;
    }

    public void setRender(String render) {
        this.render = render;
    }

    public void moveFollowingNumeratedPath(Block block, int ordinal) {
        // super block is not supposed to use path
        final String prefix = JsonMetadataConstants.removeLastNumeratedPathPart(block.getPath());
        for (BlockObj bo : children) {
            int bOrd = JsonMetadataConstants.getLastOrdinal(bo.getBlock().getPath());
            if (bo.getBlock().getPath().startsWith(prefix) && bOrd >= ordinal && bo.getBlock() != block) {
                bo.getBlock().updatePath(bOrd + 1);
            }
        }
        
    }
    
    public static SuperBlock diff(SuperBlock original, SuperBlock modified) {
        final SuperBlock result = new SuperBlock();
        boolean add = false;
        for (BlockObj originalB : original.children) {
            final BlockObj modifiedB = modified.getChildrenByPath(originalB.getBlock().getPath());
            if (modifiedB == null) {
                result.getChildren().add(new BlockObjDiff(originalB, "REMOVED"));
                add = true;
            } else {
                final BlockObj modif = BlockObj.diff(originalB, modifiedB) ;
                if (modif != null) {
                    result.getChildren().add(modif);
                    add = true;
                }
            }
            
        }
        // look for Added bo
        for (BlockObj modifiedB : modified.children) {
            if (original.getChildrenByPath(modifiedB.getBlock().getPath()) == null) {
                result.getChildren().add(new BlockObjDiff(modifiedB, "ADDED"));
                add = true;
            }
        }
        if (add) {
            return result;
        }
        return null;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[SuperBlock]\n");
        sb.append("name:").append(name).append('\n');
        sb.append("path:").append(path).append('\n');
        sb.append("multiplicity:").append(multiplicity).append('\n');
        sb.append("children:").append(children.size());
        return sb.toString();
    }
}
