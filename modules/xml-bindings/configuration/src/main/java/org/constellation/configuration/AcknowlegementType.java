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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Objects;

/**
 * A single response object indicating the success or failure of an operation.
 *
 * @author Guilhem Legal (Geomatys)
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "Acknowlegement")
public class AcknowlegementType {

    public static AcknowlegementType success(final String message) {
        return new AcknowlegementType("Success", message);
    }

    public static AcknowlegementType failure(final String message) {
        return new AcknowlegementType("Failure", message);
    }

    /**
     * An explanation message.
     */
    private String message;

    /**
     * the status of the request operation
     */
    private String status;

    /**
     * Empty constructor, by default status is set to "success".
     */
    public AcknowlegementType() {
        status  = "Success";
        message = null;
    }

    /**
     * Build a new Acknowlegement with the specified status and message.
     * @param status
     * @param message
     */
    public AcknowlegementType(final String status, final String message) {
        this.status  = status;
        this.message = message;
    }
    
    public AcknowlegementType(final boolean status, final String message) {
        if (status) {
            this.status  = "Success";
        } else {
            this.status  = "Failure";
        }
        this.message = message;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof AcknowlegementType) {
            final AcknowlegementType that = (AcknowlegementType) obj;
            return Objects.equals(this.message, that.message) &&
                   Objects.equals(this.status, that.status);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this.message != null ? this.message.hashCode() : 0);
        hash = 53 * hash + (this.status != null ? this.status.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "[Ack status=" + this.status + " message=" + this.message + ']';
    }
}
