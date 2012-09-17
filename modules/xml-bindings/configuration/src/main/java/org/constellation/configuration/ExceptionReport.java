/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2011, Geomatys
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
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@XmlRootElement(name ="ExceptionReport")
@XmlAccessorType(XmlAccessType.FIELD)
public class ExceptionReport {

    private String message;

    @XmlAttribute
    private String code;

    public ExceptionReport() {

    }

    public ExceptionReport(final String message, final String code) {
        this.code    = code;
        this.message = message;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ExceptionReport) {
            final ExceptionReport that = (ExceptionReport) obj;
            return Objects.equals(this.code, that.code) &&
                   Objects.equals(this.message, that.message);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + (this.message != null ? this.message.hashCode() : 0);
        hash = 47 * hash + (this.code != null ? this.code.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[ExceptionReport]\n");
        if (message != null) {
            sb.append("message:").append(message).append('\n');
        }
        if (code != null) {
            sb.append("code:").append(code).append('\n');
        }
        return sb.toString();
    }
}
