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


package net.seagis.ogc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LiteralType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LiteralType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/ogc}ExpressionType">
 *       &lt;sequence>
 *         &lt;any/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LiteralType", propOrder = {
    "content"
})
public class LiteralType {

    @XmlMixed
    @XmlAnyElement(lax = true)
    private List<Object> content;

    /**
     * an empty constructor used by JAXB
     */
    LiteralType() {
        
    }
    
    /**
     * build a new Literal with the specified list of object
     */
    public LiteralType(List<Object> content) {
        this.content = content;
    }
    
    /**
     * build a new Literal with the specified String
     */
    public LiteralType(String content) {
        this.content = new ArrayList<Object>(); 
        this.content.add(content);
    }
    
    /**
     * Gets the value of the content property.
     * (unmodifiable)
     */
    public List<Object> getContent() {
        if (content == null) {
            content = new ArrayList<Object>();
        }
        return Collections.unmodifiableList(content);
    }
    
    /**
     * The more often we just want to get a single String value.
     * This method return the first object of the list and cast it in String (if its possible).
     */
    public String getStringValue() {
        if (content != null && content.size() != 0) {
            if (content.get(0) instanceof String) {
                return (String)content.get(0);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("literal:");
        for (Object obj: content) {
            s.append(obj.toString()).append(" - ");
        }
        return s.toString();
    }

}
