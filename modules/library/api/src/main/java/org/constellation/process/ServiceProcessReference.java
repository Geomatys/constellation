/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2015 Geomatys.
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
package org.constellation.process;

import java.io.Serializable;

/**
 * Lite view of a Constellation Service object.
 * Used to refer to an existing Service.
 * Used to define Constellation compatible process input/output.
 *
 * @author Quentin Boileau (Geomatys)
 */
public class ServiceProcessReference implements Serializable {

    private int id;
    private String type;
    private String name;

    public ServiceProcessReference() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ServiceProcessReference serviceProcessReference = (ServiceProcessReference) o;

        if (id != serviceProcessReference.id) return false;
        if (!name.equals(serviceProcessReference.name)) return false;
        if (!type.equals(serviceProcessReference.type)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + type.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ServiceDTO{");
        sb.append("id=").append(id);
        sb.append(", type='").append(type).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
