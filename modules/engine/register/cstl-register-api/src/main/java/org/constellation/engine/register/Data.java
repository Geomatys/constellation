package org.constellation.engine.register;

import java.util.List;


public interface Data {


    public int getId();

    public void setId(int id);

    public String getName();

    public void setName(String name);

    public String getNamespace();

    public void setNamespace(String namespace);

    public Provider getProvider();

    public void setProvider(Provider provider);

    public String getType();

    public void setType(String type);

    public long getDate();

    public void setDate(long date);

    public int getTitle();

    public void setTitle(int title);

    public int getDescription();

    public void setDescription(int description);

    public User getOwner();

    public void setOwner(User owner);

    public List<Style> getStyles();

    public void setStyles(List<Style> styles);

    public String getMetadata();

    public void setMetadata(String metadata);
    
    
}
