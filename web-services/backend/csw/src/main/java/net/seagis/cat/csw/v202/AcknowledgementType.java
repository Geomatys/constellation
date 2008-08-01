/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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


package net.seagis.cat.csw.v202;

import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * This is a general acknowledgement response message for all requests that may be processed in an asynchronous manner.
 * 
 * EchoedRequest - Echoes the submitted request message
 * RequestId     - identifier for polling purposes (if no response handler is available, or the URL scheme is unsupported)
 * 
 * <p>Java class for AcknowledgementType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AcknowledgementType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="EchoedRequest" type="{http://www.opengis.net/cat/csw/2.0.2}EchoedRequestType"/>
 *         &lt;element name="RequestId" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="timeStamp" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AcknowledgementType", propOrder = {
    "echoedRequest",
    "requestId"
})
@XmlRootElement( name ="Acknowledgement" )
public class AcknowledgementType {

    @XmlElement(name = "EchoedRequest", required = true)
    private EchoedRequestType echoedRequest;
    @XmlElement(name = "RequestId")
    @XmlSchemaType(name = "anyURI")
    private String requestId;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "dateTime")
    private XMLGregorianCalendar timeStamp;

    /**
     * An empty constructor used by JAXB 
     */
    public AcknowledgementType() {
        
    }
    
    /**
     * Build a new Anknowledgement message. 
     */
    public AcknowledgementType(String requestId, EchoedRequestType echoedRequest, Long timeStamp) throws DatatypeConfigurationException {
        this.requestId     = requestId;
        this.echoedRequest = echoedRequest;
        if (timeStamp != null) {
            Date d = new Date(timeStamp);
            GregorianCalendar cal = new  GregorianCalendar();
            cal.setTime(d);
            DatatypeFactory factory = DatatypeFactory.newInstance();
            this.timeStamp = factory.newXMLGregorianCalendar(cal);
        }
    }
            
    /**
     * Gets the value of the echoedRequest property.
     */
    public EchoedRequestType getEchoedRequest() {
        return echoedRequest;
    }

    /**
     * Gets the value of the requestId property.
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Gets the value of the timeStamp property.
     */
    public XMLGregorianCalendar getTimeStamp() {
        return timeStamp;
    }
}
