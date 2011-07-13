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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.constellation.generic.database.Automatic;

/**
 * A XML binding object for SOS configuration.
 * 
 * @author Guilhem Legal (Geomatys)
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "SOSConfiguration")
public class SOSConfiguration {

    /**
     * Informations about SensorML Datasource.
     */
    @XmlElement(name="SMLConfiguration")
    private Automatic smlConfiguration;

    /**
     * Implementation type for observation filter.
     */
    private DataSourceType observationFilterType;

    /**
     * Implementation type for observation reader.
     */
    private DataSourceType observationReaderType;

    /**
     * Implementation type for observation writer.
     */
    private DataSourceType observationWriterType;

    /**
     * type of the datasource for SensorML Datasource.
     */
    @XmlElement(name="SMLType")
    private DataSourceType smlType;

    /**
     * Informations about O&M Datasource.
     */
    @XmlElement(name="OMConfiguration")
    private Automatic omConfiguration;

    /**
     * Other datasource informations (used by sub-implmentations).
     */
    private List<Automatic> extensions;

    /**
     * prefix for observations id (example: urn:ogc:object:observation:orgName:)
     */
    private String observationIdBase;

    /**
     * prefix for phenomenons id (example: urn:ogc:def:phenomenon:orgName:)
     */
    private String phenomenonIdBase;

    /**
     * prefix for observation templates id (example: urn:ogc:object:observationTemplate:orgName:)
     */
    private String observationTemplateIdBase;

    /**
     * prefix for sensorML id (example: urn:ogc:object:sensor:orgName:)
     */
    private String sensorIdBase;

    /**
     * maximal number of observations permit in the result of  a getObservation request.
     */
    private int maxObservationByRequest;

    /**
     * time of validity of a template obtain with a getObservation with resultTemplate.
     * after this time the template will be destroy.
     */
    private String templateValidTime;

    /**
     * profile of the SOS (discovery / transactional)
     */
    private String profile;

    /**
     * A directory where to redirect the logs.
     */
    private String logFolder;

    /**
     * a debug flag activating some extra logs.
     */
    private boolean debugMode;

    /**
     * A debug flag use to verify the synchronization in nearly real time insertion
     * its not advised to set this flag to true.
     */
    private boolean verifySynchronization;

    /**
     * if this flag is set to true, the response of the operation getCapabilities wil not be updated
     * every request.
     */
    private boolean keepCapabilities = false;

    /**
     * Empty constructor used by JAXB.
     */
    public SOSConfiguration() {
        
    }

    /**
     * Build a new SOS configuration with the specified SML dataSource and O&M dataSource.
     * @param smlConfiguration
     * @param omConfiguration
     */
    public SOSConfiguration(final Automatic smlConfiguration, final Automatic omConfiguration) {
        this.omConfiguration  = omConfiguration;
        this.smlConfiguration = smlConfiguration;
    }

    /**
     * @return the SMLConfiguration
     */
    public Automatic getSMLConfiguration() {
        return smlConfiguration;
    }

    /**
     * @param SMLConfiguration the SMLConfiguration to set
     */
    public void setSMLConfiguration(final Automatic smlConfiguration) {
        this.smlConfiguration = smlConfiguration;
    }

    /**
     * @return the OMConfiguration
     */
    public Automatic getOMConfiguration() {
        return omConfiguration;
    }

    /**
     * @param OMConfiguration the OMConfiguration to set
     */
    public void setOMConfiguration(final Automatic omConfiguration) {
        this.omConfiguration = omConfiguration;
    }

    /**
     * @return the observationFilterType
     */
    public DataSourceType getObservationFilterType() {
        if (observationFilterType == null)
            observationFilterType = DataSourceType.POSTGRID;
        return observationFilterType;
    }

    /**
     * @param observationFilterType the observationFilterType to set
     */
    public void setObservationFilterType(final DataSourceType observationFilterType) {
        this.observationFilterType = observationFilterType;
    }

    /**
     * @return the observationReaderType
     */
    public DataSourceType getObservationReaderType() {
        if (observationReaderType == null)
            observationReaderType = DataSourceType.POSTGRID;
        return observationReaderType;
    }

    /**
     * @param observationReaderType the observationReaderType to set
     */
    public void setObservationReaderType(final DataSourceType observationReaderType) {
        this.observationReaderType = observationReaderType;
    }

    /**
     * @return the SMLType
     */
    public DataSourceType getSMLType() {
        if (smlType == null)
            smlType = DataSourceType.MDWEB;
        return smlType;
    }

    /**
     * @param SMLType the SMLType to set
     */
    public void setSMLType(final DataSourceType smlType) {
        this.smlType = smlType;
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
    public void setObservationIdBase(final String observationIdBase) {
        this.observationIdBase = observationIdBase;
    }

    /**
     * return the phenomenon id prefix.
     * @return
     */
    public String getPhenomenonIdBase() {
        return phenomenonIdBase;
    }

    /**
     * set the the phenomenon id prefix.
     * @param phenomenonIdBase
     */
    public void setPhenomenonIdBase(final String phenomenonIdBase) {
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
    public void setObservationTemplateIdBase(final String observationTemplateIdBase) {
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
    public void setSensorIdBase(final String sensorIdBase) {
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
    public void setMaxObservationByRequest(final int maxObservationByRequest) {
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
    public void setTemplateValidTime(final String templateValidTime) {
        this.templateValidTime = templateValidTime;
    }

    /**
     * @return the observationWriterType
     */
    public DataSourceType getObservationWriterType() {
        return observationWriterType;
    }

    /**
     * @param observationWriterType the observationWriterType to set
     */
    public void setObservationWriterType(final DataSourceType observationWriterType) {
        this.observationWriterType = observationWriterType;
    }

    /**
     * Return a flag for the SOS profile (discovery/transactional)
     * @return
     */
    public int getProfile() {
        if ("transactional".equalsIgnoreCase(profile))
            return 1;
        return 0;
    }
    
    public String getProfileValue() {
        return profile;
    }

    /**
     * set the flag for the SOS profile (discovery/transactional)
     * @param profile
     */
    public void setProfile(final String profile) {
        this.profile = profile;
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
    public void setLogFolder(final String logFolder) {
        this.logFolder = logFolder;
    }

    /**
     * @return the debugMode
     */
    public boolean isDebugMode() {
        return debugMode;
    }

    /**
     * @param debugMode the debugMode to set
     */
    public void setDebugMode(final boolean debugMode) {
        this.debugMode = debugMode;
    }

    /**
     * @return the verifySynchronization
     */
    public boolean isVerifySynchronization() {
        return verifySynchronization;
    }

    /**
     * @param verifySynchronization the verifySynchronization to set
     */
    public void setVerifySynchronization(final boolean verifySynchronization) {
        this.verifySynchronization = verifySynchronization;
    }

    /**
     * @return the extensions
     */
    public List<Automatic> getExtensions() {
        if (extensions == null) {
            extensions = new ArrayList<Automatic>();
        }
        return extensions;
    }

    /**
     * @param extensions the extensions to set
     */
    public void setExtensions(final List<Automatic> extensions) {
        this.extensions = extensions;
    }

    /**
     * @return the keepCapabilities
     */
    public boolean isKeepCapabilities() {
        return keepCapabilities;
    }

    /**
     * @param keepCapabilities the keepCapabilities to set
     */
    public void setKeepCapabilities(final boolean keepCapabilities) {
        this.keepCapabilities = keepCapabilities;
    }

    /**
     * Replace all the password in this object by '****'
     */
    public void hideSensibleField() {
        for (Automatic aut: getExtensions()) {
            aut.hideSensibleField();
        }
        if (omConfiguration != null) {
            omConfiguration.hideSensibleField();
        }
        if (omConfiguration != null) {
            smlConfiguration.hideSensibleField();
        }
    }
}
