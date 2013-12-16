package org.constellation.engine.register;

public interface ServiceExtraConfig {

    public abstract void setService(Service service);

    public abstract Service getService();

    public abstract void setContent(String content);

    public abstract String getContent();

    public abstract void setFilename(String filename);

    public abstract String getFilename();

    public abstract void setId(int id);

    public abstract int getId();

}