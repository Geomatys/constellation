package org.constellation.engine.register.jpa;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.constellation.engine.register.Domain;
import org.constellation.engine.register.Layer;
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

    @ManyToOne(targetEntity=UserEntity.class, fetch=FetchType.LAZY)
    @JoinColumn(name = "`owner`")
    private User owner;
    
    @OneToMany(mappedBy="service", targetEntity=LayerEntity.class, fetch=FetchType.LAZY)
    private Set<Layer> layers;
    
    @OneToMany(mappedBy="service", targetEntity=ServiceExtraConfigEntity.class, fetch=FetchType.LAZY, cascade=CascadeType.ALL)
    private Set<ServiceExtraConfig> extraConfig;

    @OneToMany(mappedBy="service", targetEntity=ServiceMetaDataEntity.class, fetch=FetchType.LAZY, cascade=CascadeType.ALL)
    private Set<ServiceMetaData> metaDatas;

    @ManyToMany(mappedBy="services", targetEntity=DomainEntity.class)
    private Set<Domain> domains;

    
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
    public Set<ServiceExtraConfig> getExtraConfig() {
        return extraConfig;
    }

    /* (non-Javadoc)
     * @see org.constellation.engine.register.jpa.Service#setExtraConfig(org.constellation.engine.register.ServiceExtraConfig)
     */
    @Override
    public void setExtraConfig(Set<ServiceExtraConfig> extraConfig) {
        this.extraConfig = extraConfig;
    }

    /* (non-Javadoc)
     * @see org.constellation.engine.register.jpa.Service#getMetaData()
     */
    @Override
    public Set<ServiceMetaData> getMetaData() {
        return metaDatas;
    }

    /* (non-Javadoc)
     * @see org.constellation.engine.register.jpa.Service#setMetaData(org.constellation.engine.register.ServiceMetaData)
     */
    @Override
    public void setMetaData(Set<ServiceMetaData> metaDatas) {
        this.metaDatas = metaDatas;
    }

    @Override
    public String toString() {
        return "ServiceEntity [id=" + id + ", identifier=" + identifier + ", type=" + type + ", date=" + date
                + ", title=" + title + ", description=" + description + ", config=" + config + ", owner=" + owner
                + ", layers=" + layers + ", extraConfig=" + extraConfig + ", metaData=" + metaDatas + "]";
    }
    
    

}
