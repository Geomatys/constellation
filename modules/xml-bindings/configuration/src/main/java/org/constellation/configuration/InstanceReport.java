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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

/**
 * Create a report about a service with the instances informations.
 *
 * @author Guilhem Legal (Geomatys)
 */
@XmlRootElement(name ="InstanceReport")
@XmlAccessorType(XmlAccessType.FIELD)
public class InstanceReport {

    @XmlElement(name="instance")
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
        if (instances == null) {
            instances = new ArrayList<Instance>();
        }
        return instances;
    }

    public Instance getInstance(final String name) {
        if (instances != null) {
            for (Instance instance : instances) {
                if (instance.getName().equals(name)) {
                    return instance;
                }
            }
        }
        return null;
    }

    public List<Instance> getInstances(final String type) {
        final List<Instance> results = new ArrayList<Instance>();
        if (instances != null) {
            for (Instance instance : instances) {
                if (instance.getType().equals(type)) {
                    results.add(instance);
                }
            }
        }
        return results;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof InstanceReport) {
            final InstanceReport that = (InstanceReport) obj;
            return Objects.equals(this.instances, that.instances);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.instances != null ? this.instances.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[InstanceReport]\n");
        sb.append("Instances:\n");
        for (Instance instance : instances) {
            sb.append(instance);
        }
        return sb.toString();
    }
}
