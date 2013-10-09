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
}
