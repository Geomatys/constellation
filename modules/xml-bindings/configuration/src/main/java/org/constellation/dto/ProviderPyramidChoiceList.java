/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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

package org.constellation.dto;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
