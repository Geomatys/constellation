/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.seagis.ogc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import net.seagis.coverage.wms.WMSExceptionCode;


/**
 * Provides the details for an exception to be included in a {@link ServiceExceptionReport}.
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ServiceExceptionType"&gt;
 *   &lt;simpleContent&gt;
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string"&gt;
 *       &lt;attribute name="code" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;attribute name="locator" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *     &lt;/extension&gt;
 *   &lt;/simpleContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 * @author Guilhem Legal
 * @author Martin Desruisseaux
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServiceExceptionType", propOrder = {"message"})
public final class ServiceExceptionType {
    /**
     * The exception message.
     */
    @XmlValue
    private String message;

    /**
     * The exception code. Must be one of {@link ExceptionCode} constants.
     */
    @XmlAttribute
    private String code;

    /**
     * The method where the error occured.
     */
    @XmlAttribute
    private String locator;

    /**
     * Empty constructor used by JAXB.
     */
    ServiceExceptionType() {
    }

    /**
     * Builds a new exception with the specified message and code.
     *
     * @param message The message of the exception.
     * @param code A standard code for exception (OWS).
     */
    public ServiceExceptionType(final String message, final WMSExceptionCode code) {
        this.message = message;
        this.code    = code.name();
    }

    /**
     * Build a new exception with the specified message, code and locator.
     *
     * @param message The message of the exception.
     * @param code A standard code for exception (OWS).
     * @param locator The method where the error occured.
     */
    public ServiceExceptionType(final String value, final WMSExceptionCode code, final String locator) {
        this.message = value;
        this.code    = code.name();
        this.locator = locator;
    }

    /**
     * Returns the message of the exception, or {@code null} if none.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the exception code, or {@code null} if none.
     */
    public WMSExceptionCode getCode() {
        return WMSExceptionCode.valueOf(code);
    }

    /**
     * Returns the locator, or {@code null} if none.
     * @return
     */
    public String getLocator() {
        return locator;
    }
}
