package org.constellation.engine.register;

public interface Layer {

    public abstract void setOwner(User owner);

    public abstract User getOwner();

    public abstract void setConfig(String config);

    public abstract String getConfig();

    public abstract void setDescription(int description);

    public abstract int getDescription();

    public abstract void setTitle(int title);

    public abstract int getTitle();

    public abstract void setDate(long date);

    public abstract long getDate();

    public abstract void setData(Data data);

    public abstract Data getData();

    public abstract void setService(Service service);

    public abstract Service getService();

    public abstract void setAlias(String alias);

    public abstract String getAlias();

    public abstract void setNamespace(String namespace);

    public abstract String getNamespace();

    public abstract void setName(String name);

    public abstract String getName();

    public abstract void setId(int id);

    public abstract int getId();

}