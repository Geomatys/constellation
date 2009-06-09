/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class HarvestTask {

    private String sourceURL;
    private String resourceType;
    private int mode;
    private List<String> emails;
    private long period;

    public HarvestTask() {

    }

    public HarvestTask(String sourceURL, String resourceType, int mode, List<String> emails, long period) {
        this.emails       = emails;
        this.mode         = mode;
        this.resourceType = resourceType;
        this.sourceURL    = sourceURL;
        this.period       = period;
    }
    
    /**
     * @return the sourceURL
     */
    public String getSourceURL() {
        return sourceURL;
    }

    /**
     * @param sourceURL the sourceURL to set
     */
    public void setSourceURL(String sourceURL) {
        this.sourceURL = sourceURL;
    }

    /**
     * @return the resourceType
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * @param resourceType the resourceType to set
     */
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    /**
     * @return the mode
     */
    public int getMode() {
        return mode;
    }

    /**
     * @param mode the mode to set
     */
    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * @return the emails
     */
    public List<String> getEmails() {
        return emails;
    }

    /**
     * @param emails the emails to set
     */
    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    /**
     * @return the period
     */
    public long getPeriod() {
        return period;
    }

    /**
     * @param period the period to set
     */
    public void setPeriod(long period) {
        this.period = period;
    }
}
