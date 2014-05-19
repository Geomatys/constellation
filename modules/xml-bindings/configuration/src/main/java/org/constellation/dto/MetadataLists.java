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

import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * @author bgarcia
 */
@XmlRootElement
public class MetadataLists {

    private HashMap<String, String> roles;
    private TreeMap<String, String> locales;
    private HashMap<String, String> categories;
    private HashMap<String, String> dateTypes;

    public MetadataLists() {
        roles = new HashMap<>(0);
        locales = new TreeMap<>();
        categories = new HashMap<>(0);
        dateTypes = new HashMap<>(0);
    }

    public HashMap<String, String> getRoles() {
        return roles;
    }

    public void setRoles(final HashMap<String, String> roles) {
        this.roles = roles;
    }

    public TreeMap<String, String> getLocales() {
        return locales;
    }

    public void setLocales(final TreeMap<String, String> locales) {
        this.locales = locales;
    }

    public HashMap<String, String> getCategories() {
        return categories;
    }

    public void setCategories(final HashMap<String, String> categories) {
        this.categories = categories;
    }

    public HashMap<String, String> getDateTypes() {
        return dateTypes;
    }

    public void setDateTypes(final HashMap<String, String> dateTypes) {
        this.dateTypes = dateTypes;
    }

	@Override
	public String toString() {
		return "MetadataLists [roles=" + roles + ", locales=" + locales
				+ ", categories=" + categories + ", dateTypes=" + dateTypes
				+ "]";
	}
}
