package org.constellation.database.api.pojo;

import java.io.Serializable;

/**
 * @author Fabien Bernard (Geomatys).
 */
public class DataItem implements Serializable {

    private static final long serialVersionUID = 4520494765619273911L;


    protected Integer id;

    protected String namespace;

    protected String name;

    protected String type;

    protected String subtype;

    protected Long creationDate;

    protected Boolean sensorable;

    protected Integer datasetId;

    protected Integer providerId;

    protected Integer ownerId;

    protected String providerIdentifier;

    protected String ownerLogin;

    protected Long styleCount;

    protected Long layerCount;

    protected Long serviceCount;

    protected Long sensorCount;

    protected String pyramidProviderIdentifier;


    public Integer getId() {
        return id;
    }

    public DataItem setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public DataItem setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public DataItem setType(String type) {
        this.type = type;
        return this;
    }

    public String getSubtype() {
        return subtype;
    }

    public DataItem setSubtype(String subtype) {
        this.subtype = subtype;
        return this;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public DataItem setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public Boolean getSensorable() {
        return sensorable;
    }

    public DataItem setSensorable(Boolean sensorable) {
        this.sensorable = sensorable;
        return this;
    }

    public Integer getDatasetId() {
        return datasetId;
    }

    public DataItem setDatasetId(Integer datasetId) {
        this.datasetId = datasetId;
        return this;
    }

    public Integer getProviderId() {
        return providerId;
    }

    public DataItem setProviderId(Integer providerId) {
        this.providerId = providerId;
        return this;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public DataItem setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public String getProviderIdentifier() {
        return providerIdentifier;
    }

    public DataItem setProviderIdentifier(String providerIdentifier) {
        this.providerIdentifier = providerIdentifier;
        return this;
    }

    public String getOwnerLogin() {
        return ownerLogin;
    }

    public DataItem setOwnerLogin(String ownerLogin) {
        this.ownerLogin = ownerLogin;
        return this;
    }

    public Long getStyleCount() {
        return styleCount;
    }

    public DataItem setStyleCount(Long styleCount) {
        this.styleCount = styleCount;
        return this;
    }

    public Long getLayerCount() {
        return layerCount;
    }

    public DataItem setLayerCount(Long layerCount) {
        this.layerCount = layerCount;
        return this;
    }

    public Long getServiceCount() {
        return serviceCount;
    }

    public DataItem setServiceCount(Long serviceCount) {
        this.serviceCount = serviceCount;
        return this;
    }

    public Long getSensorCount() {
        return sensorCount;
    }

    public DataItem setSensorCount(Long sensorCount) {
        this.sensorCount = sensorCount;
        return this;
    }

    public String getPyramidProviderIdentifier() {
        return pyramidProviderIdentifier;
    }

    public DataItem setPyramidProviderIdentifier(String pyramidProviderIdentifier) {
        this.pyramidProviderIdentifier = pyramidProviderIdentifier;
        return this;
    }
}
