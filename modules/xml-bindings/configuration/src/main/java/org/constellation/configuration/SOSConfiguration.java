/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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

package org.constellation.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.constellation.generic.database.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "SOSConfiguration")
public class SOSConfiguration {

    private Automatic SMLConfiguration;

    private ObservationFilterType observationFilterType;

    private ObservationReaderType observationReaderType;

    private ObservationWriterType observationWriterType;

    private DataSourceType SMLType;

    private Automatic OMConfiguration;

    private String observationIdBase;

    private String phenomenonIdBase;

    private String observationTemplateIdBase;

    private String sensorIdBase;

    private int maxObservationByRequest;

    private String templateValidTime;

    private String profile;

    private String logFolder;

    public SOSConfiguration() {
        
    }

    public SOSConfiguration(Automatic SMLConfiguration, Automatic OMConfiguration) {
        this.OMConfiguration  = OMConfiguration;
        this.SMLConfiguration = SMLConfiguration;
    }

    /**
     * @return the SMLConfiguration
     */
    public Automatic getSMLConfiguration() {
        return SMLConfiguration;
    }

    /**
     * @param SMLConfiguration the SMLConfiguration to set
     */
    public void setSMLConfiguration(Automatic SMLConfiguration) {
        this.SMLConfiguration = SMLConfiguration;
    }

    /**
     * @return the OMConfiguration
     */
    public Automatic getOMConfiguration() {
        return OMConfiguration;
    }

    /**
     * @param OMConfiguration the OMConfiguration to set
     */
    public void setOMConfiguration(Automatic OMConfiguration) {
        this.OMConfiguration = OMConfiguration;
    }

    /**
     * @return the observationFilterType
     */
    public ObservationFilterType getObservationFilterType() {
        if (observationFilterType == null)
            observationFilterType = ObservationFilterType.DEFAULT;
        return observationFilterType;
    }

    /**
     * @param observationFilterType the observationFilterType to set
     */
    public void setObservationFilterType(ObservationFilterType observationFilterType) {
        this.observationFilterType = observationFilterType;
    }

    /**
     * @return the observationReaderType
     */
    public ObservationReaderType getObservationReaderType() {
        if (observationReaderType == null)
            observationReaderType = observationReaderType.DEFAULT;
        return observationReaderType;
    }

    /**
     * @param observationReaderType the observationReaderType to set
     */
    public void setObservationReaderType(ObservationReaderType observationReaderType) {
        this.observationReaderType = observationReaderType;
    }

    /**
     * @return the SMLType
     */
    public DataSourceType getSMLType() {
        if (SMLType == null)
            SMLType = DataSourceType.MDWEB;
        return SMLType;
    }

    /**
     * @param SMLType the SMLType to set
     */
    public void setSMLType(DataSourceType SMLType) {
        this.SMLType = SMLType;
    }

    /**
     * @return the observationIdBase
     */
    public String getObservationIdBase() {
        return observationIdBase;
    }

    /**
     * @param observationIdBase the observationIdBase to set
     */
    public void setObservationIdBase(String observationIdBase) {
        this.observationIdBase = observationIdBase;
    }

    public String getPhenomenonIdBase() {
        return phenomenonIdBase;
    }

    public void setPhenomenonIdBase(String phenomenonIdBase) {
        this.phenomenonIdBase = phenomenonIdBase;
    }

    /**
     * @return the observationTemplateIdBase
     */
    public String getObservationTemplateIdBase() {
        return observationTemplateIdBase;
    }

    /**
     * @param observationTemplateIdBase the observationTemplateIdBase to set
     */
    public void setObservationTemplateIdBase(String observationTemplateIdBase) {
        this.observationTemplateIdBase = observationTemplateIdBase;
    }

    /**
     * @return the sensorIdBase
     */
    public String getSensorIdBase() {
        return sensorIdBase;
    }

    /**
     * @param sensorIdBase the sensorIdBase to set
     */
    public void setSensorIdBase(String sensorIdBase) {
        this.sensorIdBase = sensorIdBase;
    }

    /**
     * @return the maxObservationByRequest
     */
    public int getMaxObservationByRequest() {
        return maxObservationByRequest;
    }

    /**
     * @param maxObservationByRequest the maxObservationByRequest to set
     */
    public void setMaxObservationByRequest(int maxObservationByRequest) {
        this.maxObservationByRequest = maxObservationByRequest;
    }

    /**
     * @return the templateValidTime
     */
    public String getTemplateValidTime() {
        return templateValidTime;
    }

    /**
     * @param templateValidTime the templateValidTime to set
     */
    public void setTemplateValidTime(String templateValidTime) {
        this.templateValidTime = templateValidTime;
    }

    /**
     * @return the observationWriterType
     */
    public ObservationWriterType getObservationWriterType() {
        return observationWriterType;
    }

    /**
     * @param observationWriterType the observationWriterType to set
     */
    public void setObservationWriterType(ObservationWriterType observationWriterType) {
        this.observationWriterType = observationWriterType;
    }

    public int getProfile() {
        if ("transactional".equalsIgnoreCase(profile))
            return 1;
        return 0;
    }

    /**
     * @return the logFolder
     */
    public String getLogFolder() {
        return logFolder;
    }

    /**
     * @param logFolder the logFolder to set
     */
    public void setLogFolder(String logFolder) {
        this.logFolder = logFolder;
    }

}
