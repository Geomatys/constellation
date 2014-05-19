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
package org.constellation.configuration;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.9
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Processes {
    
    @XmlAttribute(name="load_all")
    private Boolean loadAll;
    
    @XmlElement(name="ProcessFactory")
    private List<ProcessFactory> factory;

    public Processes() {
        
    }
    public Processes(final Boolean loadAll) {
        this.loadAll = loadAll;
    }
            
    public Processes(final List<ProcessFactory> factory) {
        this.factory = factory;
    }
    
    /**
     * @return the factory
     */
    public List<ProcessFactory> getFactory() {
        if (factory == null) {
            factory = new ArrayList<>();
        }
        return factory;
    }

    /**
     * @param factory the factory to set
     */
    public void setFactory(final List<ProcessFactory> factory) {
        this.factory = factory;
    }

    /**
     * @return the loadAll
     */
    public Boolean getLoadAll() {
        if (loadAll == null) {
            loadAll = false;
        }
        return loadAll;
    }

    /**
     * @param loadAll the loadAll to set
     */
    public void setLoadAll(Boolean loadAll) {
        this.loadAll = loadAll;
    }
}
