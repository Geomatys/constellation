/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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

package org.constellation.bean;

import java.util.ArrayList;
import java.util.List;
import javax.faces.model.SelectItem;

/**
 *
 * @author Guilhem Legal
 */
public class CSWConfigurationBean extends ConfigurationBean {

    private List<SelectItem> synchroneMode;

    private String currentSynchroneMode;

    private String serviceIdentifier;

    private String recordIdentifiers;

    public CSWConfigurationBean() {
        
        synchroneMode = new ArrayList<SelectItem>();
        synchroneMode.add(new SelectItem("synchrone", "synchrone"));
        synchroneMode.add(new SelectItem("asynchrone", "asynchrone"));
        currentSynchroneMode = "synchrone";
    }

     public void generateIndex() {
        logger.info("GUI refresh index");
        refreshServletRequest();
        String URL = getConfigurationURL() + "/WS/configuration?request=refreshIndex&asynchrone=";
        if (currentSynchroneMode.equals("synchrone"))
            URL = URL + "false";
        else
            URL = URL + "true";

        logger.info(URL);
        String response = performRequest(URL);
    }

    public void addToIndex() {
        logger.info("GUI add to index");
        refreshServletRequest();
        String URL = getConfigurationURL() + "/WS/configuration?request=addToIndex&identifiers=" + getRecordIdentifiers();
        logger.info(URL);
        String response = performRequest(URL);
    }

    public void resfreshContact() {
        logger.info("GUI refresh contact index");
        refreshServletRequest();
        String URL = getConfigurationURL() + "/WS/configuration?request=resfreshContact";
        logger.info(URL);
        String response = performRequest(URL);
    }

    public void resfreshVocabulary() {
        logger.info("GUI refresh vocabulary index");
        refreshServletRequest();
        String URL = getConfigurationURL() + "/WS/configuration?request=resfreshVocabulary";
        logger.info(URL);
        String response = performRequest(URL);
    }
    
    /**
     * @return the synchroneMode
     */
    public List<SelectItem> getSynchroneMode() {
        return synchroneMode;
    }

    /**
     * @param synchroneMode the synchroneMode to set
     */
    public void setSynchroneMode(List<SelectItem> synchroneMode) {
        this.synchroneMode = synchroneMode;
    }

    /**
     * @return the currentSynchroneMode
     */
    public String getCurrentSynchroneMode() {
        return currentSynchroneMode;
    }

    /**
     * @param currentSynchroneMode the currentSynchroneMode to set
     */
    public void setCurrentSynchroneMode(String currentSynchroneMode) {
        this.currentSynchroneMode = currentSynchroneMode;
    }

    /**
     * @return the serviceIdentifier
     */
    public String getServiceIdentifier() {
        return serviceIdentifier;
    }

    /**
     * @param serviceIdentifier the serviceIdentifier to set
     */
    public void setServiceIdentifier(String serviceIdentifier) {
        this.serviceIdentifier = serviceIdentifier;
    }

    /**
     * @return the recordIdentifiers
     */
    public String getRecordIdentifiers() {
        return recordIdentifiers;
    }

    /**
     * @param recordIdentifiers the recordIdentifiers to set
     */
    public void setRecordIdentifiers(String recordIdentifiers) {
        this.recordIdentifiers = recordIdentifiers;
    }

}
