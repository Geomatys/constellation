/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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
