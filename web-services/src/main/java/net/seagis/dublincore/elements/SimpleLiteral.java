/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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


package net.seagis.dublincore.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * This is the default type for all of the DC elements. It defines a 
 *       complexType SimpleLiteral which permits mixed content but disallows 
 *       child elements by use of minOcccurs/maxOccurs. However, this complexType 
 *       does permit the derivation of other types which would permit child 
 *       elements. The scheme attribute may be used as a qualifier to reference 
 *       an encoding scheme that describes the value domain for a given property.
 * 
 * <p>Java class for SimpleLiteral complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SimpleLiteral">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;any/>
 *       &lt;/sequence>
 *       &lt;attribute name="scheme" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SimpleLiteral", propOrder = {
    "content"
})
public class SimpleLiteral {

    @XmlMixed
    private List<String> content;
    @XmlAttribute
    @XmlSchemaType(name = "anyURI")
    private String scheme;

    /**
     * This is the default type for all of the DC elements. It defines a 
     *       complexType SimpleLiteral which permits mixed content but disallows 
     *       child elements by use of minOcccurs/maxOccurs. However, this complexType 
     *       does permit the derivation of other types which would permit child 
     *       elements. The scheme attribute may be used as a qualifier to reference 
     *       an encoding scheme that describes the value domain for a given property.Gets the value of the content property.
     * 
     * (unmodifiable) 
     */
    public List<String> getContent() {
        if (content == null) {
            content = new ArrayList<String>();
        }
        return Collections.unmodifiableList(content);
    }

    /**
     * Gets the value of the scheme property.
     */
    public String getScheme() {
        return scheme;
    }
}
