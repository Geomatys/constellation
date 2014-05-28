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

import java.util.ArrayList;
import java.util.List;

public class DomainRole {

    private Integer id;
    
    private String name;

    private String description;
    
    private boolean system;
    
    private List<Permission> permissions = new ArrayList<>();

    public DomainRole() {
    }

    public DomainRole(Integer id, String name, String description, boolean system, List<Permission> roles) {
        super();
        this.id = id;
        this.name = name;
        this.description = description;
        this.system = system;
        this.permissions = roles;
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


    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    
    @Override
    public String toString() {
        return "DomainRole [id=" + id + ", name=" + name + ", system: " + system + ", description=" + description + ", permissions="
                + permissions + "]";
    }

    public boolean isSystem() {
        return system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }
    

}
