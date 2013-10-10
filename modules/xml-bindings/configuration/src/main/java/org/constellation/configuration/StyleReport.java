/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bernard Fabien (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public final class StyleReport implements Serializable {

    @XmlElement(name="Brief")
    private StyleBrief brief;

    @XmlElement(name="Description")
    private String description;

    @XmlElement(name="SymbolizerTypes")
    private List<String> symbolizerTypes = new ArrayList<>();

    @XmlElement(name="TargetData")
    private List<DataBrief> targetData = new ArrayList<>();


    public StyleBrief getBrief() {
        return brief;
    }

    public void setBrief(final StyleBrief brief) {
        this.brief = brief;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public List<String> getSymbolizerTypes() {
        return symbolizerTypes;
    }

    public void setSymbolizerTypes(final List<String> symbolizerTypes) {
        this.symbolizerTypes = symbolizerTypes;
    }

    public List<DataBrief> getTargetData() {
        return targetData;
    }

    public void setTargetData(final List<DataBrief> targetData) {
        this.targetData = targetData;
    }
}
