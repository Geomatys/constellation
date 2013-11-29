/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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

import com.google.common.base.Objects;
import java.io.Serializable;
import org.constellation.configuration.ProcessFactory;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ProcessFactoryModel implements Serializable {

    private ProcessFactory factory;


    public ProcessFactoryModel(final ProcessFactory factory) {
        this.factory = factory;
    }
    
    /**
     * @return the factory
     */
    public ProcessFactory getFactory() {
        return factory;
    }

    /**
     * @param factory the factory to set
     */
    public void setFactory(ProcessFactory factory) {
        this.factory = factory;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + (this.factory != null ? this.factory.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ProcessFactoryModel) {
            final ProcessFactoryModel other = (ProcessFactoryModel) obj;
            return Objects.equal(this.factory, other.factory);
        }
        return false;
    }

    @Override
    public String toString() {
        return "ProcessFactoryModel{" + "factory=" + factory + '}';
    }

}
