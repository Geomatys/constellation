/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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

import java.io.File;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="WebdavContext")
public class WebdavContext {
    
    private String rootFile;
    
    private Long maxAgeSeconds;
    
    private boolean digestAllowed = true;
    
    private String ssoPrefix;
    
    private boolean hideDotFile = true;
    
    private String contextPath = "webdav";
    
    @XmlTransient
    private String id;
    
    private String defaultPage;

    public WebdavContext() {
        
    }
    
    public WebdavContext(final String rootPath) {
        this.rootFile = rootPath;
    }
    
    public WebdavContext(final File rootFile) {
        this.rootFile = rootFile.getPath();
    }
    
    /**
     * @return the rootFile
     */
    public File getRootFile() {
        return new File(rootFile);
    }

    /**
     * @param rootFile the rootFile to set
     */
    public void setRootFile(String rootFile) {
        this.rootFile = rootFile;
    }

    /**
     * @return the maxAgeSeconds
     */
    public Long getMaxAgeSeconds() {
        if (maxAgeSeconds == null) {
            maxAgeSeconds = -1L;
        }
        return maxAgeSeconds;
    }

    /**
     * @param maxAgeSeconds the maxAgeSeconds to set
     */
    public void setMaxAgeSeconds(Long maxAgeSeconds) {
        this.maxAgeSeconds = maxAgeSeconds;
    }

    /**
     * @return the digestAllowed
     */
    public boolean isDigestAllowed() {
        return digestAllowed;
    }

    /**
     * @param digestAllowed the digestAllowed to set
     */
    public void setDigestAllowed(boolean digestAllowed) {
        this.digestAllowed = digestAllowed;
    }

    /**
     * @return the ssoPrefix
     */
    public String getSsoPrefix() {
        return ssoPrefix;
    }

    /**
     * @param ssoPrefix the ssoPrefix to set
     */
    public void setSsoPrefix(String ssoPrefix) {
        this.ssoPrefix = ssoPrefix;
    }

    /**
     * @return the hideDotFile
     */
    public boolean isHideDotFile() {
        return hideDotFile;
    }

    /**
     * @param hideDotFile the hideDotFile to set
     */
    public void setHideDotFile(boolean hideDotFile) {
        this.hideDotFile = hideDotFile;
    }

    /**
     * @return the contextPath
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * @param contextPath the contextPath to set
     */
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    /**
     * @return the defaultPage
     */
    public String getDefaultPage() {
        return defaultPage;
    }

    /**
     * @param defaultPage the defaultPage to set
     */
    public void setDefaultPage(String defaultPage) {
        this.defaultPage = defaultPage;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
}
