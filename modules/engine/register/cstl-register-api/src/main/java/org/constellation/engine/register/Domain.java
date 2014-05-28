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
package org.constellation.engine.register;

import java.io.Serializable;
import java.util.Set;

public class Domain implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    
    private Integer id;
    private String name;
    private String description;

    
    public Domain() {
    }
    
    public Domain(String name, String description) {
        this.name = name;
        this.description = description;
    }
    public Domain(int id, String name, String description) {
        this(name, description);
        this.id = id;
    }

    public void setId(Integer id) {
        this.id = id;        
    }
    
    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;

    }

    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }


   

    @Override
    public String toString() {
        return "DomainDTO [id=" + id + ", name=" + name + ", description=" + description + "]";
    }
    
    


}
