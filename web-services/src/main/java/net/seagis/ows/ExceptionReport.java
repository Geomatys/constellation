/*
 * Sicade - SystÃ¨mes intÃ©grÃ©s de connaissances pour l'aide Ã  la dÃ©cision en environnement
 * (C) 2005, Institut de Recherche pour le DÃ©veloppement
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


package net.seagis.ows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}Exception" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="version" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;pattern value="\d+\.\d?\d\.\d?\d"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "exception"
})
@XmlRootElement(name = "ExceptionReport")
public class ExceptionReport {

    @XmlElement(name = "Exception", required = true)
    private List<ExceptionType> exception = new ArrayList<ExceptionType>();
    @XmlAttribute(required = true)
    private String version;

    /**
     * Empty constructor used by JAXB.
     */
    ExceptionReport() {}
    
    /**
     * Build a new full exception with the specified text, code and locator.
     * 
     * @param exceptionText 
     * @param exceptionCode
     */
    public ExceptionReport(String exceptionText, String exceptionCode, String locator, String version) {
        exception = new ArrayList<ExceptionType>();
        this.exception.add(new ExceptionType(exceptionText, exceptionCode, locator));
        this.version = version;
    }
    
    /**
     * Unordered list of one or more Exception elements that each describes an error. 
     * These Exception elements shall be interpreted by clients as being independent of one another (not hierarchical). 
     * Gets the value of the exception property.
     * 
     */
    public List<ExceptionType> getException() {
        return Collections.unmodifiableList(exception);
    }

    /**
     * Gets the value of the version property.
     */
    public String getVersion() {
        return version;
    }
}
