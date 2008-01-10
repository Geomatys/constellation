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

import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.geotools.resources.Utilities;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="Get" type="{http://www.opengis.net/ows/1.1}RequestMethodType"/>
 *         &lt;element name="Post" type="{http://www.opengis.net/ows/1.1}RequestMethodType"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "getOrPost"
})
@XmlRootElement(name = "HTTP")
public class HTTP {

    @XmlElementRefs({
        @XmlElementRef(name = "Get", namespace = "http://www.opengis.net/ows/1.1", type = JAXBElement.class),
        @XmlElementRef(name = "Post", namespace = "http://www.opengis.net/ows/1.1", type = JAXBElement.class)
    })
    private List<JAXBElement<RequestMethodType>> getOrPost;

    /**
     * Empty constructor used by JAXB.
     */
    HTTP(){
    }
    
    /**
     * build a new HTTP object.
     */
    public HTTP(List<JAXBElement<RequestMethodType>> getOrPost){
        this.getOrPost = getOrPost;
    }
    
    /**
     * Gets the value of the getOrPost property.
     */
    public List<JAXBElement<RequestMethodType>> getGetOrPost() {
        return getOrPost;
    }

     /**
     * Verify that this entry is identical to the specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        final HTTP that = (HTTP) object;
        int i=0;
        for (JAXBElement<RequestMethodType> j:getOrPost) {
            if (!Utilities.equals(j.getValue(), that.getOrPost.get(i).getValue()))
                return false;
                    
            i++;        
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.getOrPost != null ? this.getOrPost.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("class HTTP: getorPos=").append('\n');
        for (JAXBElement<RequestMethodType> j:getOrPost){
            s.append(j.getValue().toString()).append('\n');
        }
        return s.toString();
    }
}
