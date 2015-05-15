package org.constellation.engine.register.pojo;

import java.io.Serializable;

/**
 * @author Fabien Bernard (Geomatys).
 */
public class DatasetItem implements Serializable {

    private static final long serialVersionUID = 2780458219281698748L;


    protected Integer id;

    protected String name;

    protected Long creationDate;

    protected Integer ownerId;

    protected String ownerLogin;

    protected Long dataCount;


    public Integer getId() {
        return id;
    }

    public DatasetItem setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public DatasetItem setName(String name) {
        this.name = name;
        return this;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public DatasetItem setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public DatasetItem setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public String getOwnerLogin() {
        return ownerLogin;
    }

    public DatasetItem setOwnerLogin(String ownerLogin) {
        this.ownerLogin = ownerLogin;
        return this;
    }

    public Long getDataCount() {
        return dataCount;
    }

    public DatasetItem setDataCount(Long dataCount) {
        this.dataCount = dataCount;
        return this;
    }
}
