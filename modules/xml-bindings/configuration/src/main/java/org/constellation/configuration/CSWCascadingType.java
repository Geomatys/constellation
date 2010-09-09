/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.configuration;

import java.util.HashMap;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * An operation to refresh the list of cascaded CSW.
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "CSWCascading")
public class CSWCascadingType {

    /**
     * map of id / url CSW service.
     */
    private HashMap<String, String> cascadedServices;

    /**
     * If this flag is set to false, the list of Cascaded service will be overwritten.
     */
    private boolean append;
    
    /**
     * An empty constructor used by JAXB.
     */
    public CSWCascadingType() {
        
    }
    
    /**
     * An empty constructor used by JAXB.
     */
    public CSWCascadingType(HashMap<String, String> cascadedServices, boolean append) {
        this.cascadedServices = cascadedServices;
        this.append           = append;
        
    }

    /**
     * Return a map of id / url of Cascaded CSW service.
     * @return
     */
    public HashMap<String, String> getCascadedServices() {
        if (cascadedServices == null) {
            cascadedServices = new HashMap<String, String>();
        }
        return cascadedServices;
    }

    /**
     * set the map of id / url of Cascaded CSW service.
     * @param cascadedServices
     */
    public void setCascadedServices(HashMap<String, String> cascadedServices) {
        this.cascadedServices = cascadedServices;
    }

    /**
     * return the value of the flag "append".
     * @return
     */
    public boolean isAppend() {
        return append;
    }

    /**
     * Set the value of the flag "append".
     * @param append
     */
    public void setAppend(boolean append) {
        this.append = append;
    }

}
