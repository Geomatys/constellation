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
 * An XML binding for a request used to update server property File.
 * 
 * @author Guilhem Legal (Geomatys)
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "UpdatePropertiesFile")
public class UpdatePropertiesFileType {
    
    /**
     * The name of the properties file.
     */
    private String fileName;
    
    /**
     * The name of the service using this configuration file (used for verification)
     */
    private String service;
    
    /**
     * A list of key-value properties.
     */
    private HashMap<String, String> properties;
    
    /**
     * Build an empty UpdatePropertiesFile request
     */
    public UpdatePropertiesFileType() {
        properties = new HashMap<String, String>();
    }
    
    /**
     * Build an UpdatePropertiesFile request with the specified file name and service.
     * 
     * @param fileName The name of the properties file. example: config.properties
     * @param service  The name of the Service using this file.
     */
    public UpdatePropertiesFileType(String fileName, String service) {
        this.fileName = fileName;
        this.service  = service;
        properties    = new HashMap<String, String>();
    }
    
    /**
     * Build an UpdatePropertiesFile request with the specified file name and service.
     * 
     * @param fileName The name of the properties file. example: config.properties
     * @param service  The name of the Service using this file.
     * @param properties A map of key-value properties.
     */
    public UpdatePropertiesFileType(String path, String service, HashMap<String, String> properties) {
        this.fileName       = path;
        this.service    = service;
        this.properties = properties;
    }

    /**
     * return name of the properties file.
     * @return
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Set the name of the properties file.
     * @param fileName
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * return the name of the service using this configuration file (used for verification)
     * @return
     */
    public String getService() {
        return service;
    }

    /**
     * Set the name of the service using this configuration file (used for verification)
     * @param service
     */
    public void setService(String service) {
        this.service = service;
    }

    /**
     * return a list of key-value properties.
     * @return
     */
    public HashMap<String, String> getProperties() {
        if (properties == null) {
            properties = new HashMap<String, String>();
        }
        return properties;
    }

    /**
     * Set a list of key-value properties.
     * @param properties
     */
    public void setProperties(HashMap<String, String> properties) {
        this.properties = properties;
    }

}
