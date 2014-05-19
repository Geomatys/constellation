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
