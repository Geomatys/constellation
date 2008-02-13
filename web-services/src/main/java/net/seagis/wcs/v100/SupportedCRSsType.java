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

package net.seagis.wcs.v100;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import net.seagis.gml.CodeListType;


/**
 * Unordered list(s) of identifiers of Coordinate Reference Systems (CRSs) supported in server operation requests and responses. 
 * 
 * <p>Java class for SupportedCRSsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SupportedCRSsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="requestResponseCRSs" type="{http://www.opengis.net/gml}CodeListType" maxOccurs="unbounded"/>
 *           &lt;sequence>
 *             &lt;element name="requestCRSs" type="{http://www.opengis.net/gml}CodeListType" maxOccurs="unbounded"/>
 *             &lt;element name="responseCRSs" type="{http://www.opengis.net/gml}CodeListType" maxOccurs="unbounded"/>
 *           &lt;/sequence>
 *         &lt;/choice>
 *         &lt;element name="nativeCRSs" type="{http://www.opengis.net/gml}CodeListType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SupportedCRSsType", propOrder = {
    "requestResponseCRSs",
    "requestCRSs",
    "responseCRSs",
    "nativeCRSs"
})
public class SupportedCRSsType {

    private List<CodeListType> requestResponseCRSs;
    private List<CodeListType> requestCRSs;
    private List<CodeListType> responseCRSs;
    private List<CodeListType> nativeCRSs;
    
    
    /**
     * An empty constructor used by JAXB
     */
    SupportedCRSsType(){
    }
    
    /**
     * Build a new light Supported Crs element with only the request/reponse CRS accepted.
     */
    public SupportedCRSsType(List<CodeListType> requestResponseCRSs){
        this.requestResponseCRSs = requestResponseCRSs;
    }
    
    /**
     * Build a new light Supported Crs element with only the request/reponse CRS accepted.
     * all the element of the list of codeList are in the parameters.
     */
    public SupportedCRSsType(CodeListType... requestResponseCRS){
        this.requestResponseCRSs = new ArrayList<CodeListType>();
        for (CodeListType element:requestResponseCRS) {
            requestResponseCRSs.add(element);
        }
    }
    
    /**
     * Build a new full Supported Crs element.
     */
    public SupportedCRSsType(List<CodeListType> requestResponseCRSs, List<CodeListType> requestCRSs,
            List<CodeListType> responseCRSs, List<CodeListType> nativeCRSs){
        this.nativeCRSs          = nativeCRSs;
        this.requestCRSs         = requestCRSs;
        this.requestResponseCRSs = requestResponseCRSs;
        this.responseCRSs        = responseCRSs;
    }
    
    /**
     * Gets the value of the requestResponseCRSs property (unmodifiable).
     */
    public List<CodeListType> getRequestResponseCRSs() {
        return Collections.unmodifiableList(requestResponseCRSs);
    }

    /**
     * Gets the value of the requestCRSs property (unmodifiable).
     */
    public List<CodeListType> getRequestCRSs() {
        return Collections.unmodifiableList(requestCRSs);
    }

    /**
     * Gets the value of the responseCRSs property (unmodifiable).
     * 
     */
    public List<CodeListType> getResponseCRSs() {
        return Collections.unmodifiableList(responseCRSs);
    }

    /**
     * Gets the value of the nativeCRSs property (unmodifiable).
     * 
     */
    public List<CodeListType> getNativeCRSs() {
        return Collections.unmodifiableList(nativeCRSs);
    }

}
