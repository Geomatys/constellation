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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
//import juzu.Mapped;

/**
 * Access constraint part on getCapabilities service section.
 * It's a DTO used from Juzu to constellation server side. it's {@link org.constellation.dto.Service} part.
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 *
 */
//@Mapped
public class AccessConstraint {

    private String fees;

    private String accessConstraint;

    private int layerLimit;

    private int maxWidth;

    private int maxHeight;

    public AccessConstraint() {
    }

    public AccessConstraint(final String fees, final String accessConstraint,
            final int layerLimit, final int maxWidth, final int maxHeight) {
        this.accessConstraint = accessConstraint;
        this.fees = fees;
        this.layerLimit = layerLimit;
        this.maxHeight = maxHeight;
        this.maxWidth = maxWidth;
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
    
//    @JsonIgnore
//    public void setLayerLimit(String layerLimit) {
//        try {
//            this.layerLimit = Integer.parseInt(layerLimit);
//        } catch (NumberFormatException ex) {
//            this.layerLimit = 0;
//        }
//    }

    public int getMaxWidth() {
        return maxWidth;
    }
    
   
    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }
    
//    @JsonIgnore
//    public void setMaxWidth(String maxWidth) {
//        try {
//            this.maxWidth = Integer.parseInt(maxWidth);
//        } catch (NumberFormatException ex) {
//            this.maxWidth = 0;
//        }
//    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }
    
//    @JsonIgnore
//    public void setMaxHeight(String maxHeight) {
//        try {
//            this.maxHeight = Integer.parseInt(maxHeight);
//        } catch (NumberFormatException ex) {
//            this.maxHeight = 0;
//        }
//    }
}
