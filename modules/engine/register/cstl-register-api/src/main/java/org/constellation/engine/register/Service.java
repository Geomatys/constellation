package org.constellation.engine.register;


public interface Service {

    public abstract int getId();

    public abstract void setId(int id);

    public abstract String getIdentifier();

    public abstract void setIdentifier(String identifier);

    public abstract String getType();

    public abstract void setType(String type);

    public abstract long getDate();

    public abstract void setDate(long date);

    public abstract int getTitle();

    public abstract void setTitle(int title);

    public abstract int getDescription();

    public abstract void setDescription(int description);

    public abstract String getConfig();

    public abstract void setConfig(String config);

    public abstract User getOwner();

    public abstract void setOwner(User owner);

    public abstract ServiceExtraConfig getExtraConfig();

    public abstract void setExtraConfig(ServiceExtraConfig extraConfig);

    public abstract ServiceMetaData getMetaData();

    public abstract void setMetaData(ServiceMetaData metaData);

}