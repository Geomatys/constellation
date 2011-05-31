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

import java.util.ArrayList;
import java.util.List;
import javax.faces.model.SelectItem;
import org.constellation.ServiceDef.Specification;
import org.constellation.generic.database.Automatic;

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
            Automatic config = (Automatic) configurationObject;
            this.configType = config.getFormat();
        }
        return configType;
    }

    /**
     * @param configType the configType to set
     */
    public void setConfigType(String configType) {
        if (configurationObject instanceof Automatic) {
            Automatic config = (Automatic) configurationObject;
            config.setFormat(configType);
        }
        this.configType = configType;
    }

    /**
     * @return the type
     */
    public String getProfile() {
        if (configurationObject instanceof Automatic) {
            Automatic config = (Automatic) configurationObject;
            this.profile = config.getProfileValue();
        }
        return profile;
    }

    /**
     * @param type the type to set
     */
    public void setProfile(String profile) {
        if (configurationObject instanceof Automatic) {
            Automatic config = (Automatic) configurationObject;
            config.setProfile(profile);
        }
        this.profile = profile;
    }

    /**
     * @return the dataDirectory
     */
    public String getDataDirectory() {
        if (configurationObject instanceof Automatic) {
            Automatic config = (Automatic) configurationObject;
            this.dataDirectory = config.getDataDirectoryValue();
        }
        return dataDirectory;
    }

    /**
     * @param dataDirectory the dataDirectory to set
     */
    public void setDataDirectory(String dataDirectory) {
        if (configurationObject instanceof Automatic) {
            Automatic config = (Automatic) configurationObject;
            config.setDataDirectory(dataDirectory);
        }
        this.dataDirectory = dataDirectory;
    }

    /**
     * @return the driverClass
     */
    public String getDriverClass() {
        if (configurationObject instanceof Automatic) {
            Automatic config = (Automatic) configurationObject;
            this.driverClass = config.getBdd().getClassName();
        }
        return driverClass;
    }

    /**
     * @param driverClass the driverClass to set
     */
    public void setDriverClass(String driverClass) {
        if (configurationObject instanceof Automatic) {
            Automatic config = (Automatic) configurationObject;
            config.getBdd().setClassName(driverClass);
        }
        this.driverClass = driverClass;
    }

    /**
     * @return the connectURL
     */
    public String getConnectURL() {
        if (configurationObject instanceof Automatic) {
            Automatic config = (Automatic) configurationObject;
            this.connectURL = config.getBdd().getConnectURL();
        }
        return connectURL;
    }

    /**
     * @param connectURL the connectURL to set
     */
    public void setConnectURL(String connectURL) {
        if (configurationObject instanceof Automatic) {
            Automatic config = (Automatic) configurationObject;
            config.getBdd().setConnectURL(connectURL);
        }
        this.connectURL = connectURL;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        if (configurationObject instanceof Automatic) {
            Automatic config = (Automatic) configurationObject;
            this.userName = config.getBdd().getUser();
        }
        return userName;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        if (configurationObject instanceof Automatic) {
            Automatic config = (Automatic) configurationObject;
            config.getBdd().setUser(userName);
        }
        this.userName = userName;
    }

    /**
     * @return the userPass
     */
    public String getUserPass() {
        if (configurationObject instanceof Automatic) {
            Automatic config = (Automatic) configurationObject;
            this.userPass = config.getBdd().getPassword();
        }
        return userPass;
    }

    /**
     * @param userPass the userPass to set
     */
    public void setUserPass(String userPass) {
        if (configurationObject instanceof Automatic) {
            Automatic config = (Automatic) configurationObject;
            config.getBdd().setPassword(userPass);
        }
        this.userPass = userPass;
    }
}
