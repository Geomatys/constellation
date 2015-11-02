/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2015 Geomatys.
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
package org.constellation.admin.dto;

import java.util.Objects;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ProcessDTO {

    private String id;

    private String description;

    public ProcessDTO() {
        
    }
    
    public ProcessDTO(String id) {
        this.id = id;
    }
    
    public ProcessDTO(String id, String description) {
        this.id = id;
        this.description = description;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String name) {
        this.id = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "[ProcessDTO] id:" + id + ", description:" + description;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ProcessDTO) {
            final ProcessDTO that = (ProcessDTO) obj;
            return Objects.equals(this.id, that.id) &&
                   Objects.equals(this.description, that.description); 
        }
        return false;
    }
}
