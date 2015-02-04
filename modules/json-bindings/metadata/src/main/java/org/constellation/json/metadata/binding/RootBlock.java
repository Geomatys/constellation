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
 * Pojo class used for Jackson that represents the binding for root block
 * in metadata template json.
 *
 * @author Mehdi Sidhoum (Geomatys).
 * @since 0.9
 */
 @JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class RootBlock implements Serializable {

    private String name;
    private int multiplicity;
    private List<SuperBlockObj> children;

    public RootBlock(){

    }
    
    public RootBlock(RootBlock block){
        this.name         = block.name;
        this.multiplicity = block.multiplicity;
        this.children     = new ArrayList<>();
        for (SuperBlockObj sbj : block.children) {
            this.children.add(new SuperBlockObj(sbj));
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

    public List<SuperBlockObj> getChildren() {
        return children;
    }
    
    public void remove(SuperBlock sb) {
        for (SuperBlockObj sbo : children) {
            if (sbo.getSuperblock().getName().equals(sb.getName())) {
                children.remove(sbo);
                return;
            }
        }
    }
    
    public List<SuperBlock> getSuperBlocks() {
        final List<SuperBlock> results = new ArrayList<>();
        for (SuperBlockObj sb : children) {
            results.add(sb.getSuperblock());
        }
        return results;
    }

    public void setChildren(List<SuperBlockObj> children) {
        this.children = children;
    }
    
    public void moveFollowingNumeratedPath(Block block, int ordinal) {
        for (SuperBlockObj sb : children) {
            sb.getSuperblock().moveFollowingNumeratedPath(block, ordinal);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[RootBlock]\n");
        sb.append("name:").append(name).append('\n');
        sb.append("multiplicity:").append(multiplicity).append('\n');
        sb.append("children:").append(children.size());
        return sb.toString();
    }
}
