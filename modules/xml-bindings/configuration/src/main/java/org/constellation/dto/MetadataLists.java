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
    private List<String> geometricObjectTypeCodes;
    private List<String> classificationCodes;
    private List<String> characterSetCodes;
    private List<String> keywordTypeCodes;
    private List<String> restrictionCodes;
    private List<String> scopeCodes;
    private List<String> pixelOrientationCodes;
    private List<String> cellGeometryCodes;
    private List<String> dimensionNameTypeCodes;

    public MetadataLists() {
    }
    
    public MetadataLists(MetadataLists m) {
        if (m != null) {
            this.roleCodes = m.roleCodes;
            this.localeCodes= m.localeCodes;
            this.topicCategoryCodes= m.topicCategoryCodes;
            this.dateTypeCodes= m.dateTypeCodes;
            this.maintenanceFrequencyCodes= m.maintenanceFrequencyCodes;
            this.geometricObjectTypeCodes= m.geometricObjectTypeCodes;
            this.classificationCodes= m.classificationCodes;
            this.characterSetCodes= m.characterSetCodes;
            this.keywordTypeCodes= m.keywordTypeCodes;
            this.restrictionCodes = m.restrictionCodes;
            this.scopeCodes = m.scopeCodes;
            this.pixelOrientationCodes = m.pixelOrientationCodes;
            this.cellGeometryCodes = m.cellGeometryCodes;
            this.dimensionNameTypeCodes = m.dimensionNameTypeCodes;
        }
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

    public List<String> getGeometricObjectTypeCodes() {
        return geometricObjectTypeCodes;
    }

    public void setGeometricObjectTypeCodes(List<String> geometricObjectTypeCodes) {
        this.geometricObjectTypeCodes = geometricObjectTypeCodes;
    }

    public List<String> getClassificationCodes() {
        return classificationCodes;
    }

    public void setClassificationCodes(List<String> classificationCodes) {
        this.classificationCodes = classificationCodes;
    }

    public List<String> getCharacterSetCodes() {
        return characterSetCodes;
    }

    public void setCharacterSetCodes(List<String> characterSetCodes) {
        this.characterSetCodes = characterSetCodes;
    }

    public List<String> getKeywordTypeCodes() {
        return keywordTypeCodes;
    }

    public void setKeywordTypeCodes(List<String> keywordTypeCodes) {
        this.keywordTypeCodes = keywordTypeCodes;
    }
    
    public List<String> getRestrictionCodes() {
        return restrictionCodes;
    }

    public void setRestrictionCodes(List<String> restrictionCodes) {
        this.restrictionCodes = restrictionCodes;
    }
    
    public List<String> getScopeCodes() {
        return scopeCodes;
    }

    public void setScopeCodes(List<String> scopeCodes) {
        this.scopeCodes = scopeCodes;
    }
    
    public List<String> getPixelOrientationCodes() {
        return pixelOrientationCodes;
    }

    public void setPixelOrientationCodes(List<String> pixelOrientationCodes) {
        this.pixelOrientationCodes = pixelOrientationCodes;
    }
    
    public List<String> getCellGeometryCodes() {
        return cellGeometryCodes;
    }

    public void setCellGeometryCodes(List<String> cellGeometryCodes) {
        this.cellGeometryCodes = cellGeometryCodes;
    }
    
    public List<String> getDimensionNameTypeCodes() {
        return dimensionNameTypeCodes;
    }

    public void setDimensionNameTypeCodes(List<String> dimensionNameTypeCodes) {
        this.dimensionNameTypeCodes = dimensionNameTypeCodes;
    }
}
