/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.List;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class HarvestTask {

    /**
     * The URl of the resource to harvest
     */
    private String sourceURL;

    /**
     * The type of the resource. example: http://www.isotc211.org/2005/gmd
     */
    private String resourceType;

    /**
     * The type of the resource: 0 for a single record, 1 for a CSW service
     */
    private int mode;

    /**
     * A list of mails addresses to contact for harvest report.
     */
    private List<String> emails;

    /**
     * The frequency of harvest task.
     */
    private long period;

    /**
     * The last date where the task was launch.
     */
    private long lastHarvest;

    /**
     * Empty constructor used by JAXB
     */
    public HarvestTask() {

    }

    public HarvestTask(String sourceURL, String resourceType, int mode, List<String> emails, long period, long lastHarvest) {
        this.emails       = emails;
        this.mode         = mode;
        this.resourceType = resourceType;
        this.sourceURL    = sourceURL;
        this.period       = period;
        this.lastHarvest  = lastHarvest;
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

    /**
     * @return the lastHarvest
     */
    public long getLastHarvest() {
        return lastHarvest;
    }

    /**
     * @param lastHarvest the lastHarvest to set
     */
    public void setLastHarvest(long lastHarvest) {
        this.lastHarvest = lastHarvest;
    }
}
