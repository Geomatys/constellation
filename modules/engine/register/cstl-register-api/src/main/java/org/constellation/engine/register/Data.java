package org.constellation.engine.register;

import java.util.List;

public class Data {

    private int id;
    private String name;
    private String namespace;
    private int providerId;
    private String type;
    private long date;
    private int title;
    private int description;
    private String owner;
    private List<Integer> styleIds;
    private String metadata;

    
    public int getId() {
        return id;
    }

    
    public void setId(int id) {
        this.id = id;        
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    public int getProviderId() {
        return providerId;
    }


    public String getType() {
        return type;
    }

    public long getDate() {
        return date;
    }

    public int getTitle() {
        return title;
    }

    public int getDescription() {
        return description;
    }

    public String getOwner() {
        return owner;
    }


    public List<Integer> getStyles() {
        return styleIds;
    }


    public String getMetadata() {
        return metadata;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setProviderId(int provider) {
        this.providerId = provider;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public void setDescription(int description) {
        this.description = description;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setStyleIds(List<Integer> styleIds) {
        this.styleIds = styleIds;
    }

    public void setMetadata(String metaData) {
        this.metadata = metaData;
    }

}
