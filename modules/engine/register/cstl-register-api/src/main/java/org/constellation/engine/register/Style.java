package org.constellation.engine.register;

public interface Style {

    public abstract void setOwner(User owner);

    public abstract User getOwner();

    public abstract void setBody(String body);

    public abstract String getBody();

    public abstract void setDescription(int description);

    public abstract int getDescription();

    public abstract void setTitle(int title);

    public abstract int getTitle();

    public abstract void setDate(long date);

    public abstract long getDate();

    public abstract void setType(String type);

    public abstract String getType();

    public abstract void setProvider(Provider provider);

    public abstract Provider getProvider();

    public abstract void setName(String name);

    public abstract String getName();

    public abstract void setId(int id);

    public abstract int getId();

    public abstract String toString();

}