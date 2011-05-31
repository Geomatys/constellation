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

package org.constellation.menu.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.faces.model.SelectItem;
import javax.sql.DataSource;
import org.constellation.ServiceDef.Specification;
import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.ObservationFilterType;
import org.constellation.configuration.ObservationReaderType;
import org.constellation.configuration.ObservationWriterType;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.observation.sql.ObservationDatabaseCreator;
import org.mdweb.sql.DatabaseCreator;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class SOSBean extends AbstractServiceBean{

    private String omConfigType;
    
    private String omDataDirectory;
    
    private String omDriverClass = "org.postgresql.Driver";
    
    private String omConnectURL;
    
    private String omUserName;
    
    private String omUserPass;
    
    private String smlConfigType;
    
    private String smlDataDirectory;
    
    private String smlDriverClass = "org.postgresql.Driver";
    
    private String smlConnectURL;
    
    private String smlUserName;
    
    private String smlUserPass;
    
    private String profile;
    
    public SOSBean() {
        super(Specification.SOS,
                "/service/sos.xhtml",
                "/service/sosConfig.xhtml");
        addBundle("service.sos");
    }
    
    public List<SelectItem> getOmConfigTypes() {
        final List<SelectItem> selectItems = new ArrayList<SelectItem>();
        selectItems.add(new SelectItem("default"));
        selectItems.add(new SelectItem("filesystem"));
        return selectItems;
    }
    
    public List<SelectItem> getSmlConfigTypes() {
        final List<SelectItem> selectItems = new ArrayList<SelectItem>();
        selectItems.add(new SelectItem("mdweb"));
        selectItems.add(new SelectItem("filesystem"));
        return selectItems;
    }
    
    public List<SelectItem> getProfiles() {
        final List<SelectItem> selectItems = new ArrayList<SelectItem>();
        selectItems.add(new SelectItem("discovery"));
        selectItems.add(new SelectItem("transactional"));
        return selectItems;
    }

    /**
     * @return the omConfigType
     */
    public String getOmConfigType() {
        if (configurationObject instanceof SOSConfiguration) {
            final SOSConfiguration config = (SOSConfiguration) configurationObject;
            this.omConfigType = config.getObservationReaderType().getName();
        }
        return omConfigType;
    }

    /**
     * @param omConfigType the omConfigType to set
     */
    public void setOmConfigType(String omConfigType) {
        if (configurationObject instanceof SOSConfiguration) {
            final SOSConfiguration config = (SOSConfiguration) configurationObject;
            final ObservationReaderType rType = ObservationReaderType.fromName(omConfigType);
            config.setObservationReaderType(rType);
            if (rType == ObservationReaderType.DEFAULT) {
                config.setObservationFilterType(ObservationFilterType.DEFAULT);
                config.setObservationWriterType(ObservationWriterType.DEFAULT);
            } else if (rType == ObservationReaderType.FILESYSTEM) {
                config.setObservationFilterType(ObservationFilterType.LUCENE);
                config.setObservationWriterType(ObservationWriterType.FILESYSTEM);
            }
        }
        this.omConfigType = omConfigType;
    }

    /**
     * @return the omDataDirectory
     */
    public String getOmDataDirectory() {
        if (configurationObject instanceof SOSConfiguration) {
            final SOSConfiguration config = (SOSConfiguration) configurationObject;
            this.omDataDirectory = config.getOMConfiguration().getDataDirectoryValue();
        }
        return omDataDirectory;
    }

    /**
     * @param omDataDirectory the omDataDirectory to set
     */
    public void setOmDataDirectory(String omDataDirectory) {
        if (configurationObject instanceof SOSConfiguration) {
            final SOSConfiguration config = (SOSConfiguration) configurationObject;
            config.getOMConfiguration().setDataDirectory(omDataDirectory);
        }
        this.omDataDirectory = omDataDirectory;
    }

    /**
     * @return the omDriverClass
     */
    public String getOmDriverClass() {
        if (configurationObject instanceof SOSConfiguration) {
            final SOSConfiguration config = (SOSConfiguration) configurationObject;
            this.omDriverClass = config.getOMConfiguration().getBdd().getClassName();
        }
        return omDriverClass;
    }

    /**
     * @param omDriverClass the omDriverClass to set
     */
    public void setOmDriverClass(String omDriverClass) {
        if (configurationObject instanceof SOSConfiguration) {
            final SOSConfiguration config = (SOSConfiguration) configurationObject;
            config.getOMConfiguration().getBdd().setClassName(omDriverClass);
        }
        this.omDriverClass = omDriverClass;
    }

    /**
     * @return the omConnectURL
     */
    public String getOmConnectURL() {
        if (configurationObject instanceof SOSConfiguration) {
            final SOSConfiguration config = (SOSConfiguration) configurationObject;
            this.omConnectURL = config.getOMConfiguration().getBdd().getConnectURL();
        }
        return omConnectURL;
    }

    /**
     * @param omConnectURL the omConnectURL to set
     */
    public void setOmConnectURL(String omConnectURL) {
        if (configurationObject instanceof SOSConfiguration) {
            final SOSConfiguration config = (SOSConfiguration) configurationObject;
            config.getOMConfiguration().getBdd().setClassName(omDriverClass);
        }
        this.omConnectURL = omConnectURL;
    }

    /**
     * @return the omUserName
     */
    public String getOmUserName() {
        if (configurationObject instanceof SOSConfiguration) {
            final SOSConfiguration config = (SOSConfiguration) configurationObject;
            this.omUserName = config.getOMConfiguration().getBdd().getUser();
        }
        return omUserName;
    }

    /**
     * @param omUserName the omUserName to set
     */
    public void setOmUserName(String omUserName) {
        if (configurationObject instanceof SOSConfiguration) {
            final SOSConfiguration config = (SOSConfiguration) configurationObject;
            config.getOMConfiguration().getBdd().setUser(omUserName);
        }
        this.omUserName = omUserName;
    }

    /**
     * @return the omUserPass
     */
    public String getOmUserPass() {
        if (configurationObject instanceof SOSConfiguration) {
            final SOSConfiguration config = (SOSConfiguration) configurationObject;
            this.omUserPass = config.getOMConfiguration().getBdd().getPassword();
        }
        return omUserPass;
    }

    /**
     * @param omUserPass the omUserPass to set
     */
    public void setOmUserPass(String omUserPass) {
        if (configurationObject instanceof SOSConfiguration) {
            final SOSConfiguration config = (SOSConfiguration) configurationObject;
            config.getOMConfiguration().getBdd().setPassword(omUserPass);
        }
        this.omUserPass = omUserPass;
    }

    /**
     * @return the smlConfigType
     */
    public String getSmlConfigType() {
        if (configurationObject instanceof SOSConfiguration) {
            final SOSConfiguration config = (SOSConfiguration) configurationObject;
            this.smlConfigType = config.getSMLType().getName();
        }
        return smlConfigType;
    }

    /**
     * @param smlConfigType the smlConfigType to set
     */
    public void setSmlConfigType(String smlConfigType) {
        if (configurationObject instanceof SOSConfiguration) {
            final SOSConfiguration config = (SOSConfiguration) configurationObject;
            config.setSMLType(DataSourceType.fromName(smlConfigType));
        }
        this.smlConfigType = smlConfigType;
    }

    /**
     * @return the smlDataDirectory
     */
    public String getSmlDataDirectory() {
        if (configurationObject instanceof SOSConfiguration) {
            final SOSConfiguration config = (SOSConfiguration) configurationObject;
            this.smlDataDirectory = config.getSMLConfiguration().getDataDirectoryValue();
        }
        return smlDataDirectory;
    }

    /**
     * @param smlDataDirectory the smlDataDirectory to set
     */
    public void setSmlDataDirectory(String smlDataDirectory) {
        if (configurationObject instanceof SOSConfiguration) {
            final SOSConfiguration config = (SOSConfiguration) configurationObject;
            config.getSMLConfiguration().setDataDirectory(smlDataDirectory);
        }
        this.smlDataDirectory = smlDataDirectory;
    }

    /**
     * @return the smlDriverClass
     */
    public String getSmlDriverClass() {
        if (configurationObject instanceof SOSConfiguration) {
            final SOSConfiguration config = (SOSConfiguration) configurationObject;
            this.smlDriverClass = config.getSMLConfiguration().getBdd().getClassName();
        }
        return smlDriverClass;
    }

    /**
     * @param smlDriverClass the smlDriverClass to set
     */
    public void setSmlDriverClass(String smlDriverClass) {
        if (configurationObject instanceof SOSConfiguration) {
            final SOSConfiguration config = (SOSConfiguration) configurationObject;
            config.getSMLConfiguration().getBdd().setClassName(smlDriverClass);
        }
        this.smlDriverClass = smlDriverClass;
    }

    /**
     * @return the smlConnectURL
     */
    public String getSmlConnectURL() {
        if (configurationObject instanceof SOSConfiguration) {
            final SOSConfiguration config = (SOSConfiguration) configurationObject;
            this.smlConnectURL= config.getSMLConfiguration().getBdd().getConnectURL();
        }
        return smlConnectURL;
    }

    /**
     * @param smlConnectURL the smlConnectURL to set
     */
    public void setSmlConnectURL(String smlConnectURL) {
        if (configurationObject instanceof SOSConfiguration) {
            final SOSConfiguration config = (SOSConfiguration) configurationObject;
            config.getSMLConfiguration().getBdd().setConnectURL(smlConnectURL);
        }
        this.smlConnectURL = smlConnectURL;
    }

    /**
     * @return the smlUserName
     */
    public String getSmlUserName() {
        if (configurationObject instanceof SOSConfiguration) {
            final SOSConfiguration config = (SOSConfiguration) configurationObject;
            this.smlUserName = config.getSMLConfiguration().getBdd().getUser();
        }
        return smlUserName;
    }

    /**
     * @param smlUserName the smlUserName to set
     */
    public void setSmlUserName(String smlUserName) {
        if (configurationObject instanceof SOSConfiguration) {
            final SOSConfiguration config = (SOSConfiguration) configurationObject;
            config.getSMLConfiguration().getBdd().setUser(smlUserName);
        }
        this.smlUserName = smlUserName;
    }

    /**
     * @return the smlUserPass
     */
    public String getSmlUserPass() {
        if (configurationObject instanceof SOSConfiguration) {
            final SOSConfiguration config = (SOSConfiguration) configurationObject;
            this.smlUserPass = config.getOMConfiguration().getBdd().getPassword();
        }
        return smlUserPass;
    }

    /**
     * @param smlUserPass the smlUserPass to set
     */
    public void setSmlUserPass(String smlUserPass) {
        if (configurationObject instanceof SOSConfiguration) {
            final SOSConfiguration config = (SOSConfiguration) configurationObject;
            config.getSMLConfiguration().getBdd().setPassword(smlUserPass);
        }
        this.smlUserPass = smlUserPass;
    }

    /**
     * @return the profile
     */
    public String getProfile() {
        if (configurationObject instanceof SOSConfiguration) {
            final SOSConfiguration config = (SOSConfiguration) configurationObject;
            this.profile = config.getProfileValue();
        }
        return profile;
    }

    /**
     * @param profile the profile to set
     */
    public void setProfile(String profile) {
        if (configurationObject instanceof SOSConfiguration) {
            final SOSConfiguration config = (SOSConfiguration) configurationObject;
            config.setProfile(profile);
        }
        this.profile = profile;
    }

    public void buildMDWDatabase() {
        if (configurationObject instanceof SOSConfiguration) {
            final SOSConfiguration config = (SOSConfiguration) configurationObject;
            try {
                final DataSource ds = config.getSMLConfiguration().getBdd().getDataSource();
                DatabaseCreator.createPGMetadataDatabase(ds);
            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING, "Error while creating the database", ex);
            }
        }
    }
    
    public void buildOMDatabase() {
        if (configurationObject instanceof SOSConfiguration) {
            final SOSConfiguration config = (SOSConfiguration) configurationObject;
            try {
                final DataSource ds = config.getOMConfiguration().getBdd().getDataSource();
                ObservationDatabaseCreator.createObservationDatabase(ds);
            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING, "Error while creating the database", ex);
            }
        }
    }
}
