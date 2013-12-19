package org.constellation.gui.service.bean;


import juzu.Mapped;

import java.util.Date;

/**
 * @author Benjamin Garcia (Geomatys)
 */
@Mapped
public class LayerData {

    private String idProvider;

    private String type;

    private String name;

    private String namespace;

    private Date date;

    private String owner;

    public LayerData() {
    }

    public LayerData(final String idProvider, final String type, final String name, final Date date, final String owner, final String namespace) {
        this.idProvider = idProvider;
        this.type = type;
        this.name = name;
        this.date = date;
        this.owner = owner;
        this.namespace = namespace;
    }

    public String getIdProvider() {
        return idProvider;
    }

    public void setIdProvider(final String idProvider) {
        this.idProvider = idProvider;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(final String owner) {
        this.owner = owner;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }
}
