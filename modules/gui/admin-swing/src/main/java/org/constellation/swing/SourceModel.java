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
