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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.9
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessList {

    @XmlElement(name="Process")
    private List<Process> process;

    public ProcessList() {

    }

    public ProcessList(final List<Process> process) {
        this.process = process;
    }

    /**
     * @return the process
     */
    public List<Process> getProcess() {
        if (process == null) {
            process = new ArrayList<>();
        }
        return process;
    }

    /**
     * @param process the process to set
     */
    public void setProcess(final List<Process> process) {
        this.process = process;
    }

    public boolean contains(final String id) {
        for (Process p : getProcess()) {
            if (p.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }
}
