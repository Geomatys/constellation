package org.constellation.engine.register;

public interface Provider {

    public abstract void setMetadata(String metadata);

    public abstract String getMetadata();

    public abstract void setOwner(User owner);

    public abstract User getOwner();

    public abstract void setConfig(String config);

    public abstract String getConfig();

    public abstract void setImpl(String impl);

    public abstract String getImpl();

    public abstract void setType(String type);

    public abstract String getType();

    public abstract void setIdentifier(String identifier);

    public abstract String getIdentifier();

    public abstract void setId(int id);

    public abstract int getId();

}