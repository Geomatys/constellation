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
