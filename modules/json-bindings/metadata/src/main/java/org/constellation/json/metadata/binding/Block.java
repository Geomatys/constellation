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
public class Block implements Serializable, ChildEntity {

    private String name;
    private int multiplicity;
    private String help;
    private String path;
    private String render;
    private String ignore;
    private String type;
    private List<FieldObj> children;

    public Block(){

    }
    
    public Block(Block block){
        this.name         = block.name;
        this.multiplicity = block.multiplicity;
        this.help         = block.help;
        this.path         = block.path;
        this.type         = block.type;
        this.render       = block.render;
        this.ignore       = block.ignore;
        this.children     = new ArrayList<>();
        for (FieldObj f : block.children) {
            this.children.add(new FieldObj(f));
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

    public List<FieldObj> getChildren() {
        return children;
    }

    public void setChildren(List<FieldObj> children) {
        this.children = children;
    }
    
    public List<Field> getFields() {
        final List<Field> results = new ArrayList<>();
        for (FieldObj f : children) {
            results.add(f.getField());
        }
        return results;
    }
    
    public void addField(int index, Field field) {
        children.add(index, new FieldObj(field));
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
}
