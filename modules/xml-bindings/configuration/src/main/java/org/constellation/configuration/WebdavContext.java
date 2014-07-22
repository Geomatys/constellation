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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.File;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="WebdavContext")
public class WebdavContext extends AbstractConfigurationObject {

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
        if (rootFile != null) {
            return new File(rootFile);
        }
        return null;
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
