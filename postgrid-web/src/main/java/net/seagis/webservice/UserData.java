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

package net.seagis.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * This object record the webService metadatas for a specific user.
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserData")
public class UserData {
    
    private Object[] WMSCapabilities;
    
    private Object[] WCSCapabilities;
    
    private Object[] CSWCapabilities;
    
    private Object[] SOSCapabilities;
    
    public UserData() {
        
    }

    public Object[] getWMSCapabilities() {
        return WMSCapabilities;
    }

    public void setWMSCapabilities(Object[] WMSCapabilities) {
        this.WMSCapabilities = WMSCapabilities;
    }

    public Object[] getWCSCapabilities() {
        return WCSCapabilities;
    }

    public void setWCSCapabilities(Object[] WCSCapabilities) {
        this.WCSCapabilities = WCSCapabilities;
    }

    public Object[] getCSWCapabilities() {
        return CSWCapabilities;
    }

    public void setCSWCapabilities(Object[] CSWCapabilities) {
        this.CSWCapabilities = CSWCapabilities;
    }

    public Object[] getSOSCapabilities() {
        return SOSCapabilities;
    }

    public void setSOSCapabilities(Object[] SOSCapabilities) {
        this.SOSCapabilities = SOSCapabilities;
    }
    
    
    
    

}
