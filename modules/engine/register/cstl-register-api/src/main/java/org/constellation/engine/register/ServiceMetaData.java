package org.constellation.engine.register;

public interface ServiceMetaData {

    public abstract void setService(Service service);

    public abstract Service getService();

    public abstract void setContent(String content);

    public abstract String getContent();

    public abstract void setLang(String lang);

    public abstract String getLang();

    public abstract void setId(int id);

    public abstract int getId();

}