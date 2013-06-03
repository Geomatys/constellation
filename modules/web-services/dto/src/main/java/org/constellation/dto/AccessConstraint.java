package org.constellation.dto;

import juzu.Mapped;

/**
 * @author bgarcia
 * @since 28/05/13
 */
@Mapped
public class AccessConstraint {

    private String fees;

    private String accessConstraint;

    private int layerLimit;

    private int maxWidth;

    private int maxHeight;

    public AccessConstraint() {
    }

    public String getFees() {
        return fees;
    }

    public void setFees(String fees) {
        this.fees = fees;
    }

    public String getAccessConstraint() {
        return accessConstraint;
    }

    public void setAccessConstraint(String accessConstraint) {
        this.accessConstraint = accessConstraint;
    }

    public int getLayerLimit() {
        return layerLimit;
    }

    public void setLayerLimit(int layerLimit) {
        this.layerLimit = layerLimit;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }
}
