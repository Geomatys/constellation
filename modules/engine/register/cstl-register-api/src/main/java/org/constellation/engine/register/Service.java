package org.constellation.engine.register;


public class Service {

    private int id;
    private String identifier;
    private String type;
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

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getDate() {
        return this.date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getTitle() {
        return this.title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public int getDescription() {
        return this.description;
    }

    public void setDescription(int description) {
        this.description = description;
    }

    public String getConfig() {
        return this.config;
    }

    public void setConfig(String config) {
        this.config = config;

    }

    public String getOwner() {
        return this.owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "ServiceDTO [id=" + id + ", identifier=" + identifier + ", type=" + type + ", date=" + date + ", title="
                + title + ", description=" + description + ", config=" + config + ", owner=" + owner + "]";
    }

}
