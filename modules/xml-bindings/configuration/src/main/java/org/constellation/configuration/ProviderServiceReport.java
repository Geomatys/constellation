/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ProviderServiceReport {
    
    @XmlAttribute
    private String type;
    
    @XmlAttribute
    private boolean styleService;
    
    @XmlElement(name = "provider")
    private List<ProviderReport> providers;
    
    public ProviderServiceReport() {
        
    }

    public ProviderServiceReport(final String type, final boolean styleService, final List<ProviderReport> providers) {
        this.type = type;
        this.styleService = styleService;
        this.providers = providers;
    }
    
    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return true if this service provider give style objects
     */
    public boolean isStyleService() {
        return styleService;
    }

    /**
     * @param stylingService 
     */
    public void setStyleService(boolean stylingService) {
        this.styleService = stylingService;
    }

    /**
     * @return the providers
     */
    public List<ProviderReport> getProviders() {
        if(providers == null){
            providers = new ArrayList<ProviderReport>();
        }
        return providers;
    }

    /**
     * @param providers the sources to set
     */
    public void setProviders(List<ProviderReport> providers) {
        this.providers = providers;
    }
}
