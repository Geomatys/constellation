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

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

/**
 * This class represents a pojo that store all necessary codelist for UI part.
 * this pojo is sent by REST api.
 *
 * @author Mehdi Sidhoum (Geomatys).
 * @since 0.9
 */
@XmlRootElement
public class MetadataLists implements Serializable {

    private List<String> roleCodes;
    private List<String> localeCodes;
    private List<String> topicCategoryCodes;
    private List<String> dateTypeCodes;
    private List<String> maintenanceFrequencyCodes;

    public MetadataLists() {
    }

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public void setRoleCodes(List<String> roleCodes) {
        this.roleCodes = roleCodes;
    }

    public List<String> getLocaleCodes() {
        return localeCodes;
    }

    public void setLocaleCodes(List<String> localeCodes) {
        this.localeCodes = localeCodes;
    }

    public List<String> getTopicCategoryCodes() {
        return topicCategoryCodes;
    }

    public void setTopicCategoryCodes(List<String> topicCategoryCodes) {
        this.topicCategoryCodes = topicCategoryCodes;
    }

    public List<String> getDateTypeCodes() {
        return dateTypeCodes;
    }

    public void setDateTypeCodes(List<String> dateTypeCodes) {
        this.dateTypeCodes = dateTypeCodes;
    }

    public List<String> getMaintenanceFrequencyCodes() {
        return maintenanceFrequencyCodes;
    }

    public void setMaintenanceFrequencyCodes(List<String> maintenanceFrequencyCodes) {
        this.maintenanceFrequencyCodes = maintenanceFrequencyCodes;
    }
}
