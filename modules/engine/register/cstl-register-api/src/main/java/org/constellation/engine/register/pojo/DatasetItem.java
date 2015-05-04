package org.constellation.engine.register.pojo;

import java.io.Serializable;

/**
 * @author Fabien Bernard (Geomatys).
 */
public class DatasetItem implements Serializable {

    private static final long serialVersionUID = 2780458219281698748L;


    protected Integer id;

    protected String identifier;

    protected Long creationDate;

    protected Integer ownerId;

    protected String ownerLogin;

    protected Long dataCount;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerLogin() {
        return ownerLogin;
    }

    public void setOwnerLogin(String ownerLogin) {
        this.ownerLogin = ownerLogin;
    }

    public Long getDataCount() {
        return dataCount;
    }

    public void setDataCount(Long dataCount) {
        this.dataCount = dataCount;
    }
}
