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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import net.seagis.wcs.InterpolationMethodBaseType;
import org.geotools.resources.Utilities;


/**
 * Type copied from basicTypes.xsd of GML 3 with documentation edited, for possible use outside the ServiceIdentification section of a service metadata document. 
 * 
 * <p>Java class for CodeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CodeType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *       &lt;attribute name="codeSpace" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CodeType", propOrder = {
    "value"
})
@XmlSeeAlso({
    InterpolationMethodBaseType.class
})
public class CodeType {

    @XmlValue
    private String value;
    @XmlAttribute
    @XmlSchemaType(name = "anyURI")
    private String codeSpace;

    /**
     * Empty constructor used by JAXB.
     */
    protected CodeType() {
    }
    
    /**
     * Build a new code.
     */
    public CodeType(String value, String codespace) {
        this.codeSpace = codespace;
        this.value     = value;
    }
    
    /**
     * Gets the value of the value property.
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the value of the codeSpace property.
     */
    public String getCodeSpace() {
        return codeSpace;
    }

    /**
     * Verify that this entry is identical to the specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        final CodeType that = (CodeType) object;

        return Utilities.equals(this.value,     that.value) &&
               Utilities.equals(this.codeSpace, that.codeSpace);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.value != null ? this.value.hashCode() : 0);
        hash = 71 * hash + (this.codeSpace != null ? this.codeSpace.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "class:CodeType value=" + value + " codeSpace=" + codeSpace;
    }

}
