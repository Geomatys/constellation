/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2012, Geomatys
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

import juzu.Mapped;

/**
 * Access constraint part on getCapabilities service section.
 * It's a DTO used from Juzu to constellation server side. it's {@link org.constellation.dto.Service} part.
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 *
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
