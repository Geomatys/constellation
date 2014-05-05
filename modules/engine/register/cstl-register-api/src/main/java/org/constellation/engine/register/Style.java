package org.constellation.engine.register;

public class Style {

    private User owner;
    private String body;
    private int description;
    private int title;
    private long date;
    private String type;
    private Provider provider;
    private String name;
    private int id;

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public User getOwner() {
        return owner;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }


    public void setDescription(int description) {
        this.description = description;
    }


    public int getDescription() {
        return description;
    }


    public void setTitle(int title) {
        this.title = title;
    }


    public int getTitle() {
        return title;
    }


    public void setDate(long date) {
        this.date = date;
    }


    public long getDate() {
        return date;
    }


    public void setType(String type) {
        this.type = type;
    }


    public String getType() {
        return type;
    }


    public void setProvider(Provider provider) {
        this.provider = provider;
    }


    public Provider getProvider() {
        return provider;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getName() {
        return this.name;
    }


    public void setId(int id) {
        this.id = id;
    }


    public int getId() {
        return this.id;
    }

}
