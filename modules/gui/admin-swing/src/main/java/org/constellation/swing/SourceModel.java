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

package org.constellation.swing;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class SourceModel implements Serializable {

    private String providerId;

    private boolean loadAll;

    public SourceModel() {

    }
    
    public SourceModel(final String providerId, final boolean loadAll) {
        this.providerId = providerId;
        this.loadAll    = loadAll;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(final String providerId) {
        this.providerId = providerId;
    }

    /**
     * @return the loadAll
     */
    public boolean isLoadAll() {
        return loadAll;
    }

    /**
     * @param loadAll the loadAll to set
     */
    public void setLoadAll(boolean loadAll) {
        this.loadAll = loadAll;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof SourceModel) {
            final SourceModel other = (SourceModel) obj;
            return Objects.equals(this.providerId, other.providerId) &&
                   Objects.equals(this.loadAll,    other.loadAll);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Objects.hashCode(this.providerId);
        hash = 11 * hash + (this.loadAll ? 1 : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "SourceModel {providerId=" + providerId + " loadAll="+  loadAll + '}';
    }
}
