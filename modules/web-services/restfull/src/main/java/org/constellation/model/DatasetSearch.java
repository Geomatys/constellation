package org.constellation.model;

import org.constellation.json.PagedSearch;

/**
 * @author Fabien Bernard (Geomatys).
 */
public class DatasetSearch extends PagedSearch {

    private static final long serialVersionUID = -5561221853197386905L;


    private boolean excludeEmpty;

    private Boolean hasVectorData;

    private Boolean hasCoverageData;

    private Boolean hasLayerData;

    private Boolean hasSensorData;


    public boolean isExcludeEmpty() {
        return excludeEmpty;
    }

    public void setExcludeEmpty(boolean excludeEmpty) {
        this.excludeEmpty = excludeEmpty;
    }

    public Boolean getHasVectorData() {
        return hasVectorData;
    }

    public void setHasVectorData(Boolean hasVectorData) {
        this.hasVectorData = hasVectorData;
    }

    public Boolean getHasCoverageData() {
        return hasCoverageData;
    }

    public void setHasCoverageData(Boolean hasCoverageData) {
        this.hasCoverageData = hasCoverageData;
    }

    public Boolean getHasLayerData() {
        return hasLayerData;
    }

    public void setHasLayerData(Boolean hasLayerData) {
        this.hasLayerData = hasLayerData;
    }

    public Boolean getHasSensorData() {
        return hasSensorData;
    }

    public void setHasSensorData(Boolean hasSensorData) {
        this.hasSensorData = hasSensorData;
    }
}
