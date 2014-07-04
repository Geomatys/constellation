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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
@XmlRootElement(name ="ProvidersReport")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProvidersReport {

    @XmlElement(name = "service")
    private List<ProviderServiceReport> providerServices;

    public ProvidersReport() {

    }

    public ProvidersReport(final List<ProviderServiceReport> services) {
        this.providerServices = services;
    }

    /**
     * @return the provider services
     */
    public List<ProviderServiceReport> getProviderServices() {
        if(providerServices == null){
            providerServices = new ArrayList<ProviderServiceReport>();
        }
        return providerServices;
    }

    /**
     * @param providerServices the provider services to set
     */
    public void setProviderServices(List<ProviderServiceReport> providerServices) {
        this.providerServices = providerServices;
    }

    /**
     * @param type The type (Postgis, shapefile, ...) of the wanted provider service.
     * @return ProviderServiceReport or null
     */
    public ProviderServiceReport getProviderService(final String type){
        for(ProviderServiceReport report : getProviderServices()){
            if(type.equals(report.getType())){
                return report;
            }
        }
        return null;
    }

    /**
     * @param id of the wanted provider.
     * @return ProviderReport or null
     */
    public ProviderReport getProvider(final String id){
        for(ProviderServiceReport report : getProviderServices()){
            final ProviderReport p = report.getProvider(id);
            if (p !=  null) {
                return p;
            }
        }
        return null;
    }

}
