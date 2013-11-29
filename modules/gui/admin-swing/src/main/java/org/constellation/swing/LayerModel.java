/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.swing;

import java.io.Serializable;
import org.constellation.configuration.Layer;

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
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LayerModel other = (LayerModel) obj;
        if ((this.providerId == null) ? (other.providerId != null) : !this.providerId.equals(other.providerId)) {
            return false;
        }
        if (this.layer != other.layer && (this.layer == null || !this.layer.equals(other.layer))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "LayerModel{" + "providerId=" + providerId + ", layer=" + layer + '}';
    }

}
