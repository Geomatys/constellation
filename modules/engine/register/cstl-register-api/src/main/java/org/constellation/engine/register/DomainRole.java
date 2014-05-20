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

import java.util.HashSet;
import java.util.Set;

public class DomainRole {

    private String name;

    private String description;
    
    private Set<String> permissions = new HashSet<String>();

    public DomainRole() {
    }

    public DomainRole(String name, String description, Set<String> roles) {
        super();
        this.name = name;
        this.description = description;
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

    @Override
    public String toString() {
        return "DomainRole [name=" + name + ", description=" + description + "]";
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> roles) {
        this.permissions = roles;
    }
    
    

}
