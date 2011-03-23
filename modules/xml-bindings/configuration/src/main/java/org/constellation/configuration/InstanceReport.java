/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2011, Geomatys
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

/**
 * Create a report about a service with the instances informations.
 *
 * @author Guilhem Legal (Geomatys)
 */
@XmlRootElement(name ="InstanceReport")
@XmlAccessorType(XmlAccessType.FIELD)
public class InstanceReport {

    private List<Instance> instances;

    public InstanceReport() {

    }

    public InstanceReport(final List<Instance> instances) {
        this.instances = instances;
    }

    /**
     * @return the instances
     */
    public List<Instance> getInstances() {
        return instances;
    }

}
