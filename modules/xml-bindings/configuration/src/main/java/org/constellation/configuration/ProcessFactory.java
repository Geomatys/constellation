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
public class ProcessFactory {
    @XmlAttribute
    private String autorityCode;

    @XmlAttribute(name="load_all")
    private Boolean loadAll;

    @XmlElement
    private ProcessList include;

    public ProcessFactory() {

    }

    public ProcessFactory(final String autorityCode, final Boolean loadAll) {
        this.autorityCode = autorityCode;
        this.loadAll = loadAll;
    }

    /**
     * @return the autorityCode
     */
    public String getAutorityCode() {
        return autorityCode;
    }

    /**
     * @param autorityCode the autorityCode to set
     */
    public void setAutorityCode(final String autorityCode) {
        this.autorityCode = autorityCode;
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
    public void setLoadAll(final Boolean loadAll) {
        this.loadAll = loadAll;
    }

    /**
     * @return the include
     */
    public ProcessList getInclude() {
        if (include == null) {
            include = new ProcessList();
        }
        return include;
    }

    /**
     * @param include the include to set
     */
    public void setInclude(final ProcessList include) {
        this.include = include;
    }

}
