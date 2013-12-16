package org.constellation.engine.register.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.constellation.engine.register.Service;
import org.constellation.engine.register.ServiceExtraConfig;
import org.constellation.engine.register.ServiceMetaData;
import org.constellation.engine.register.User;

@Entity
@Table(schema = "`admin`", name = "`service`")
public class ServiceEntity implements Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    private int id;

    @Column(name = "`identifier`")
    private String identifier;

    @Column(name = "`type`")
    private String type;

    @Column(name = "`date`")
    private long date;

    @Column(name = "`title`")
    private int title;
    @Column(name = "`description`")
    private int description;
    @Column(name = "`config`")
    private String config;

    @ManyToOne(targetEntity=UserEntity.class)
    @JoinColumn(name = "`owner`")
    private User owner;
    
    @OneToOne(mappedBy="service", targetEntity=ServiceExtraConfigEntity.class)
    private ServiceExtraConfig extraConfig;

    @OneToOne(mappedBy="service", targetEntity=ServiceMetaDataEntity.class)
    private ServiceMetaData metaData;

    /* (non-Javadoc)
     * @see org.constellation.engine.register.jpa.Service#getId()
     */
    @Override
    public int getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.constellation.engine.register.jpa.Service#setId(int)
     */
    @Override
    public void setId(int id) {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see org.constellation.engine.register.jpa.Service#getIdentifier()
     */
    @Override
    public String getIdentifier() {
        return identifier;
    }

    /* (non-Javadoc)
     * @see org.constellation.engine.register.jpa.Service#setIdentifier(java.lang.String)
     */
    @Override
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /* (non-Javadoc)
     * @see org.constellation.engine.register.jpa.Service#getType()
     */
    @Override
    public String getType() {
        return type;
    }

    /* (non-Javadoc)
     * @see org.constellation.engine.register.jpa.Service#setType(java.lang.String)
     */
    @Override
    public void setType(String type) {
        this.type = type;
    }

    /* (non-Javadoc)
     * @see org.constellation.engine.register.jpa.Service#getDate()
     */
    @Override
    public long getDate() {
        return date;
    }

    /* (non-Javadoc)
     * @see org.constellation.engine.register.jpa.Service#setDate(long)
     */
    @Override
    public void setDate(long date) {
        this.date = date;
    }

    /* (non-Javadoc)
     * @see org.constellation.engine.register.jpa.Service#getTitle()
     */
    @Override
    public int getTitle() {
        return title;
    }

    /* (non-Javadoc)
     * @see org.constellation.engine.register.jpa.Service#setTitle(int)
     */
    @Override
    public void setTitle(int title) {
        this.title = title;
    }

    /* (non-Javadoc)
     * @see org.constellation.engine.register.jpa.Service#getDescription()
     */
    @Override
    public int getDescription() {
        return description;
    }

    /* (non-Javadoc)
     * @see org.constellation.engine.register.jpa.Service#setDescription(int)
     */
    @Override
    public void setDescription(int description) {
        this.description = description;
    }

    /* (non-Javadoc)
     * @see org.constellation.engine.register.jpa.Service#getConfig()
     */
    @Override
    public String getConfig() {
        return config;
    }

    /* (non-Javadoc)
     * @see org.constellation.engine.register.jpa.Service#setConfig(java.sql.Clob)
     */
    @Override
    public void setConfig(String config) {
        this.config = config;
    }

    /* (non-Javadoc)
     * @see org.constellation.engine.register.jpa.Service#getOwner()
     */
    @Override
    public User getOwner() {
        return owner;
    }

    /* (non-Javadoc)
     * @see org.constellation.engine.register.jpa.Service#setOwner(org.constellation.engine.register.User)
     */
    @Override
    public void setOwner(User owner) {
        this.owner = owner;
    }

    /* (non-Javadoc)
     * @see org.constellation.engine.register.jpa.Service#getExtraConfig()
     */
    @Override
    public ServiceExtraConfig getExtraConfig() {
        return extraConfig;
    }

    /* (non-Javadoc)
     * @see org.constellation.engine.register.jpa.Service#setExtraConfig(org.constellation.engine.register.ServiceExtraConfig)
     */
    @Override
    public void setExtraConfig(ServiceExtraConfig extraConfig) {
        this.extraConfig = extraConfig;
    }

    /* (non-Javadoc)
     * @see org.constellation.engine.register.jpa.Service#getMetaData()
     */
    @Override
    public ServiceMetaData getMetaData() {
        return metaData;
    }

    /* (non-Javadoc)
     * @see org.constellation.engine.register.jpa.Service#setMetaData(org.constellation.engine.register.ServiceMetaData)
     */
    @Override
    public void setMetaData(ServiceMetaData metaData) {
        this.metaData = metaData;
    }

}
