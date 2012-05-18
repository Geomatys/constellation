/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010-2012, Geomatys
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
import javax.xml.bind.annotation.XmlRootElement;
import org.geotoolkit.gui.swing.tree.Trees;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.9
 */
@XmlRootElement(name="ProcessContext")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessContext {

    private Processes processes;

    private String security;

    private Languages supportedLanguages;
    
    private String tmpDirectory;

    public ProcessContext() {

    }

    public ProcessContext(Processes processes) {
        this.processes = processes;
    }

    public ProcessContext(Processes processes, String security) {
        this.processes = processes;
        this.security = security;
    }

    public Processes getProcesses() {
        return processes;
    }
    
    /**
     * @return the layers
     */
    public List<ProcessFactory> getProcessFactories() {
        if (processes == null) {
            processes = new Processes();
            return processes.getFactory();
        } else {
            return processes.getFactory();
        }
    }


    /**
     * @param layers the layers to set
     */
    public void setProcesses(List<ProcessFactory> processes) {
        this.processes = new Processes(processes);
    }

    /**
     * @return the security constraint, or {@code null} if none.
     */
    public String getSecurity() {
        return security;
    }

    /**
     * Sets the security value.
     *
     * @param security the security value.
     */
    public void setSecurity(String security) {
        this.security = security;
    }

    /**
     * @return the supportedLanguages
     */
    public Languages getSupportedLanguages() {
        return supportedLanguages;
    }

    /**
     * @param supportedLanguages the supportedLanguages to set
     */
    public void setSupportedLanguages(Languages supportedLanguages) {
        this.supportedLanguages = supportedLanguages;
    }
    
    /**
     * @return the tmpDirectory
     */
    public String getTmpDirectory() {
        return tmpDirectory;
    }

    /**
     * @param tmpDirectory the tmpDirectory to set
     */
    public void setTmpDirectory(String tmpDirectory) {
        this.tmpDirectory = tmpDirectory;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(Trees.toString("ProcessContext", getProcessFactories()));
        if (security != null && !security.isEmpty()) {
            sb.append("Security=").append(security);
        }
        if (supportedLanguages != null) {
            sb.append("Supported languages:\n").append(supportedLanguages);
        }
        return sb.toString();
    }

}
