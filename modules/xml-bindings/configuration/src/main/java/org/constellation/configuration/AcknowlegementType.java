/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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
        status  = "succeed";
        message = null;
    }

    /**
     * Build a new Acknowlegement with the specified status and message.
     * @param status
     * @param message
     */
    public AcknowlegementType(String status, String message) {
        this.status  = status;
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
