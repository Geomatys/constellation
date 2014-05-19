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
import javax.xml.bind.annotation.XmlValue;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.7
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Language {

    @XmlAttribute(name="default")
    private Boolean defaultt;

    @XmlValue
    private String languageCode;

    public Language() {

    }

    public Language(final String languageCode) {
        this.languageCode = languageCode;
        this.defaultt = false;
    }

    public Language(final String languageCode, final Boolean defaultt) {
        this.languageCode = languageCode;
        this.defaultt = defaultt;
    }
    
    /**
     * @return the default
     */
    public Boolean getDefault() {
        if (defaultt == null) {
            return false;
        }
        return defaultt;
    }

    /**
     * @param defaultt the default to set
     */
    public void setDefault(Boolean defaultt) {
        this.defaultt = defaultt;
    }

    /**
     * @return the languageCode
     */
    public String getLanguageCode() {
        return languageCode;
    }

    /**
     * @param languageCode the languageCode to set
     */
    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }


    @Override
    public String toString() {
        String s =  "[Language]=" + languageCode;
        if (defaultt != null && defaultt) {
            s = s + "(default)";
        }
        return s;
    }
}
