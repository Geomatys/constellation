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

package org.constellation.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains a list of available cache pyramids for a provider data.
 * 
 * @author Johann Sorel (Geomatys)
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ProviderPyramidChoiceList {
   
    @XmlElement
    private List<CachePyramid> pyramids;

    public ProviderPyramidChoiceList() {
    }

    public List<CachePyramid> getPyramids() {
        if(pyramids==null){
            pyramids = new ArrayList<>();
        }
        return pyramids;
    }

    public void setPyramids(List<CachePyramid> pyramids) {
        this.pyramids = pyramids;
    }
    
    public static final class CachePyramid{
        
        @XmlElement
        private String providerId;
        @XmlElement
        private String dataId;
        @XmlElement
        private boolean conform;
        @XmlElement
        private String crs;
        @XmlElement
        private double[] scales;

        public String getProviderId() {
            return providerId;
        }

        public void setProviderId(String providerId) {
            this.providerId = providerId;
        }

        public String getDataId() {
            return dataId;
        }

        public void setDataId(String dataId) {
            this.dataId = dataId;
        }

        public boolean isConform() {
            return conform;
        }

        public void setConform(boolean conform) {
            this.conform = conform;
        }

        public String getCrs() {
            return crs;
        }

        public void setCrs(String crs) {
            this.crs = crs;
        }

        public double[] getScales() {
            return scales;
        }

        public void setScales(double[] scales) {
            this.scales = scales;
        }
                
    }
    
}
