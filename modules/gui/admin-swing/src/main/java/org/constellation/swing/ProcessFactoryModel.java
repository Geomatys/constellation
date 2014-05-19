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
