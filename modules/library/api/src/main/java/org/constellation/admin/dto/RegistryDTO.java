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

import java.util.List;
import java.util.Objects;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class RegistryDTO {

    private String name;

    private List<ProcessDTO> processes;

    public RegistryDTO() {
        
    }
    
    public RegistryDTO(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ProcessDTO> getProcesses() {
        return processes;
    }

    public void setProcesses(List<ProcessDTO> processes) {
        this.processes = processes;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[RegistryDTO] name:");
        sb.append(name).append("Processes:\n");
        if (processes != null) {
            for (ProcessDTO p : processes){
                sb.append(p).append('\n');
            }
        }
        return  sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof RegistryDTO) {
            final RegistryDTO that = (RegistryDTO) obj;
            return Objects.equals(this.name, that.name) &&
                   Objects.equals(this.processes, that.processes); 
        }
        return false;
    }
}
