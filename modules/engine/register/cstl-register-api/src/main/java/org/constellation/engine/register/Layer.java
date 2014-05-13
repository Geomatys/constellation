package org.constellation.engine.register;



public class Layer {
    
    private int id;

    private String name;

    private String namespace;

    private String alias;

    private int serviceId;

    private int dataId;

    private long date;

    private int title;

    private int description;

    private String config;

    private String owner;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public int getService() {
        return serviceId;
    }

    public void setService(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getData() {
        return dataId;
    }

    public void setData(int dataId) {
        this.dataId = dataId;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public int getDescription() {
        return description;
    }

    public void setDescription(int description) {
        this.description = description;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "Layer [id=" + id + ", name=" + name + ", namespace=" + namespace + ", alias=" + alias + ", serviceId="
                + serviceId + ", dataId=" + dataId + ", date=" + date + ", title=" + title + ", description="
                + description + ", config=" + config + ", owner=" + owner + "]";
    }
    
    
}
