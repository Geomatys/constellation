package org.constellation.engine.register.jpa;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.constellation.engine.register.Data;
import org.constellation.engine.register.Provider;
import org.constellation.engine.register.Style;
import org.constellation.engine.register.User;

@Entity
@Table(schema = "`admin`", name = "`data`")
public class DataEntity implements Data {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    private int id;

    @Column(name = "`name`")
    private String name;
    @Column(name = "`namespace`")
    private String namespace;

    @ManyToOne(targetEntity=ProviderEntity.class)
    @JoinColumn(name = "`provider`")
    private Provider provider;

    @Column(name = "`type`")
    private String type;

    @Column(name = "`date`")
    private long date;

    @Column(name = "`title`")
    private int title;

    @Column(name = "`description`")
    private int description;
    
    @ManyToOne(targetEntity=UserEntity.class)
    @JoinColumn(name = "`owner`")
    private User owner;

    @ManyToMany(mappedBy="datas", targetEntity=StyleEntity.class)
    private List<Style> styles;

    @Column(name = "`metadata`")
    private String metadata;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public int getDescription() {
        return description;
    }

    public void setDescription(int description) {
        this.description = description;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public List<Style> getStyles() {
        return styles;
    }

    public void setStyles(List<Style> styles) {
        this.styles = styles;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(final String metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "Data [id=" + id + ", name=" + name + ", namespace=" + namespace + ", provider=" + provider + ", type="
                + type + ", date=" + date + ", title=" + title + ", description=" + description + ", owner=" + owner
                + ", styles=" + styles + ", metadata="+metadata+"]";
    }
}
