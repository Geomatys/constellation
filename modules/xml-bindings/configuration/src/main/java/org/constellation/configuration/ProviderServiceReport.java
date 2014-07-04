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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ProviderServiceReport {

    @XmlAttribute
    private String type;
    @XmlAttribute
    private boolean styleService;
    @XmlElement(name = "provider")
    private List<ProviderReport> providers;

    public ProviderServiceReport() {
    }

    public ProviderServiceReport(final String type, final boolean styleService, final List<ProviderReport> providers) {
        this.type = type;
        this.styleService = styleService;
        this.providers = providers;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return true if this service provider give style objects
     */
    public boolean isStyleService() {
        return styleService;
    }

    /**
     * @param stylingService
     */
    public void setStyleService(boolean stylingService) {
        this.styleService = stylingService;
    }

    /**
     * @return the providers
     */
    public List<ProviderReport> getProviders() {
        if (providers == null) {
            providers = new ArrayList<ProviderReport>();
        }
        return providers;
    }

    /**
     * @param providers the sources to set
     */
    public void setProviders(List<ProviderReport> providers) {
        this.providers = providers;
    }

    /**
     * Return report for the given Id. null if it doesn't exist.
     */
    public ProviderReport getProvider(final String id) {
        for (ProviderReport report : getProviders()) {
            if (report.getId().equals(id)) {
                return report;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof ProviderServiceReport) {
            ProviderServiceReport compar = (ProviderServiceReport) other;
            if (compar.getType().equals(getType()) && compar.isStyleService() == isStyleService()) {
                if (compar.getProviders().equals(getProviders())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 23 * hash + (this.providers != null ? this.providers.hashCode() : 0);
        return hash;
    }
}
