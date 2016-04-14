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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class RegistryListDTO {
    
    private List<RegistryDTO> registries;

    /**
     * @return the registries
     */
    public List<RegistryDTO> getRegistries() {
        if (registries == null) {
            registries = new ArrayList<>();
        }
        return registries;
    }

    /**
     * @param registries the registries to set
     */
    public void setRegistries(List<RegistryDTO> registries) {
        this.registries = registries;
    }
}