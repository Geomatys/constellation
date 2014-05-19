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
