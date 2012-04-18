/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010-2012, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
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

    @XmlElement
    private ProcessList exclude;

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
        return include;
    }

    /**
     * @param include the include to set
     */
    public void setInclude(final ProcessList include) {
        this.include = include;
    }

    /**
     * @return the exclude
     */
    public ProcessList getExclude() {
        return exclude;
    }

    /**
     * @param exclude the exclude to set
     */
    public void setExclude(final ProcessList exclude) {
        this.exclude = exclude;
    }
}
