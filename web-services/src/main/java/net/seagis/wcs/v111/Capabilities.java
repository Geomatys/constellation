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


package net.seagis.wcs.v111;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import net.seagis.ows.CapabilitiesBaseType;
import net.seagis.ows.OperationsMetadata;
import net.seagis.ows.ServiceIdentification;
import net.seagis.ows.ServiceProvider;


/**
 * <p>Root Document for a response to a getCapabilities request (WCS version 1.1.1).
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/ows/1.1}CapabilitiesBaseType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/wcs/1.1.1}Contents" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "contents"
})
@XmlRootElement(name = "Capabilities")
public class Capabilities extends CapabilitiesBaseType {

    @XmlElement(name = "Contents")
    private Contents contents;

    /**
     * An empty constructor used by JAXB
     */
    Capabilities(){}
    
    /**
     * Build a new Capabilities document.
     */
    public Capabilities(ServiceIdentification serviceIdentification, ServiceProvider serviceProvider,
            OperationsMetadata operationsMetadata, String version, String updateSequence, Contents contents) {
        super(serviceIdentification, serviceProvider, operationsMetadata, version, updateSequence);
        this.contents = contents;
    }
    
    /**
     * Gets the value of the contents property.
     * 
     */
    public Contents getContents() {
        return contents;
    }
    
    public void setContents(Contents contents) {
        this.contents = contents;
    }
}
