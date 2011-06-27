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

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.faces.model.SelectItem;
import javax.sql.DataSource;
import org.constellation.ServiceDef.Specification;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.geotoolkit.util.FileUtilities;
import org.mdweb.sql.DatabaseCreator;
import org.mapfaces.model.UploadedFile;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @author Guilhem Legal (Geomatys)
 */
public class CSWBean extends AbstractServiceBean {

    private String configType;
    
    private String profile;
    
    private String dataDirectory;
    
    private String driverClass = "org.postgresql.Driver";
    
    private String connectURL;
    
    private String userName;
    
    private String userPass;
    
    private UploadedFile uploadedFile;
    
    public CSWBean() {
        super(Specification.CSW,
                "/service/csw.xhtml",
                "/service/cswConfig.xhtml");
        addBundle("service.csw");
    }

    public List<SelectItem> getConfigTypes() {
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
     * @return the configType
     */
    public String getConfigType() {
        if (configurationObject instanceof Automatic) {
            final Automatic config = (Automatic) configurationObject;
            this.configType = config.getFormat();
        }
        return configType;
    }

    /**
     * @param configType the configType to set
     */
    public void setConfigType(String configType) {
        if (configurationObject instanceof Automatic) {
            final Automatic config = (Automatic) configurationObject;
            config.setFormat(configType);
        }
        this.configType = configType;
    }

    /**
     * @return the type
     */
    public String getProfile() {
        if (configurationObject instanceof Automatic) {
            final Automatic config = (Automatic) configurationObject;
            this.profile = config.getProfileValue();
        }
        return profile;
    }

    /**
     * @param type the type to set
     */
    public void setProfile(String profile) {
        if (configurationObject instanceof Automatic) {
            final Automatic config = (Automatic) configurationObject;
            config.setProfile(profile);
        }
        this.profile = profile;
    }

    /**
     * @return the dataDirectory
     */
    public String getDataDirectory() {
        if (configurationObject instanceof Automatic) {
            final Automatic config = (Automatic) configurationObject;
            this.dataDirectory = config.getDataDirectoryValue();
        }
        return dataDirectory;
    }

    /**
     * @param dataDirectory the dataDirectory to set
     */
    public void setDataDirectory(String dataDirectory) {
        if (configurationObject instanceof Automatic) {
            final Automatic config = (Automatic) configurationObject;
            config.setDataDirectory(dataDirectory);
        }
        this.dataDirectory = dataDirectory;
    }

    /**
     * @return the driverClass
     */
    public String getDriverClass() {
        if (configurationObject instanceof Automatic) {
            final Automatic config = (Automatic) configurationObject;
            if (config.getBdd() != null) {
                this.driverClass = config.getBdd().getClassName();
            }
        }
        return driverClass;
    }

    /**
     * @param driverClass the driverClass to set
     */
    public void setDriverClass(String driverClass) {
        if (configurationObject instanceof Automatic) {
            final Automatic config = (Automatic) configurationObject;
            if (config.getBdd() == null) {
                config.setBdd(new BDD());
            }
            config.getBdd().setClassName(driverClass);
        }
        this.driverClass = driverClass;
    }

    /**
     * @return the connectURL
     */
    public String getConnectURL() {
        if (configurationObject instanceof Automatic) {
            final Automatic config = (Automatic) configurationObject;
            if (config.getBdd() != null) {
                this.connectURL = config.getBdd().getConnectURL();
            }
        }
        return connectURL;
    }

    /**
     * @param connectURL the connectURL to set
     */
    public void setConnectURL(String connectURL) {
        if (configurationObject instanceof Automatic) {
            final Automatic config = (Automatic) configurationObject;
            if (config.getBdd() == null) {
                config.setBdd(new BDD());
            }
            config.getBdd().setConnectURL(connectURL);
        }
        this.connectURL = connectURL;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        if (configurationObject instanceof Automatic) {
            final Automatic config = (Automatic) configurationObject;
            if (config.getBdd() != null) {
                this.userName = config.getBdd().getUser();
            }
        }
        return userName;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        if (configurationObject instanceof Automatic) {
            final Automatic config = (Automatic) configurationObject;
            if (config.getBdd() == null) {
                config.setBdd(new BDD());
            }
            config.getBdd().setUser(userName);
        }
        this.userName = userName;
    }

    /**
     * @return the userPass
     */
    public String getUserPass() {
        if (configurationObject instanceof Automatic) {
            final Automatic config = (Automatic) configurationObject;
            if (config.getBdd() != null) {
                this.userPass = config.getBdd().getPassword();
            }
        }
        return userPass;
    }

    /**
     * @param userPass the userPass to set
     */
    public void setUserPass(String userPass) {
        if (configurationObject instanceof Automatic) {
            final Automatic config = (Automatic) configurationObject;
            if (config.getBdd() == null) {
                config.setBdd(new BDD());
            }
            config.getBdd().setPassword(userPass);
        }
        this.userPass = userPass;
    }
    
    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }
    
    /**
     * Build an MDWeb Database
     */
    public void buildDatabase() {
        
        if (configurationObject instanceof Automatic) {
            final Automatic config = (Automatic) configurationObject;
            try {
                if (config.getBdd() != null) {
                    final DataSource ds    = config.getBdd().getDataSource();
                    DatabaseCreator.createPGMetadataDatabase(ds);
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING, "Error while creating the database", ex);
            }
        }
        
    }
    
    /**
     * Refresh the cSW instance lucene index.
     */
     public void refreshIndex() {
         final String instanceId = getConfiguredInstance().getName();
         getServer().csws.refreshIndex(instanceId, true);
         getServer().services.restartInstance("CSW", instanceId);
     }
     
     /**
     * Build an MDWeb Database
     */
    public void importRecord() {
        if (uploadedFile != null) {
            final String contentType = uploadedFile.getContentType();
            if ("application/zip".equals(contentType)
             || "application/octet-stream".equals(contentType)
             || "application/x-download".equals(contentType)
             || "application/download".equals(contentType)
             || "text/xml".equals(contentType)
             || "application/x-httpd-php".equals(contentType)
             || "application/x-zip-compressed".equals(contentType)) {
               
                final String instanceId = getConfiguredInstance().getName();
                try {
                    final File importedfile = FileUtilities.buildFileFromStream(uploadedFile.getInputStream());
                    getServer().csws.importFile(instanceId, importedfile);
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "IO exception while reading imported file", ex);
                }
                
            } else {
                LOGGER.log(Level.WARNING, "This content type can not be read : {0}", contentType);
            }
        } else {
            LOGGER.log(Level.WARNING, "imported file is null");
        }
    }
    
    
    
}
