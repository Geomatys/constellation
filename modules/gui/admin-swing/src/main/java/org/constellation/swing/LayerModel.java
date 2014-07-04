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
package org.constellation.swing;

import org.constellation.configuration.Layer;

import java.io.Serializable;
import java.util.Objects;

/**
 * Class LayerModel extend Layer to add provider identifier from where layer is come from.
 *
 * @author Quentin Boileau (Geomatys)
 */
public class LayerModel implements Serializable {
    
    private String providerId;
    private final Layer layer;

    public LayerModel(final Layer layer, final String providerId) {
        this.providerId = providerId;
        this.layer = layer;
    }

    public String getProviderId() {
        return providerId;
    }
    
    public void setProviderId(final String providerId) {
        this.providerId = providerId;
    }

    public Layer getLayer() {
        return layer;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + (this.providerId != null ? this.providerId.hashCode() : 0);
        hash = 11 * hash + (this.layer != null ? this.layer.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof LayerModel) {
            final LayerModel other = (LayerModel) obj;
            return Objects.equals(this.providerId, other.providerId) &&
                   Objects.equals(this.layer, other.layer);
        }
        return false;
    }

    @Override
    public String toString() {
        return "LayerModel{" + "providerId=" + providerId + ", layer=" + layer + '}';
    }

}
