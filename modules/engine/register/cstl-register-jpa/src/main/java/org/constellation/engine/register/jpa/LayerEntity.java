package org.constellation.engine.register.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.constellation.engine.register.Data;
import org.constellation.engine.register.Layer;
import org.constellation.engine.register.Service;
import org.constellation.engine.register.User;

@Entity
@Table(schema = "`admin`", name = "`layer`")
public class LayerEntity implements Layer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    private int id;

    @Column(name = "`name`")
    private String name;

    @Column(name = "`namespace`")
    private String namespace;

    @Column(name = "`alias`")
    private String alias;

    @ManyToOne(targetEntity=ServiceEntity.class)
    @JoinColumn(name = "`service`")
    private Service service;

    @ManyToOne(targetEntity=DataEntity.class)
    @JoinColumn(name = "`data`")
    private Data data;

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

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public Service getService() {
        return service;
    }

    @Override
    public void setService(Service service) {
        this.service = service;
    }

    @Override
    public Data getData() {
        return data;
    }

    @Override
    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public long getDate() {
        return date;
    }

    @Override
    public void setDate(long date) {
        this.date = date;
    }

    @Override
    public int getTitle() {
        return title;
    }

    @Override
    public void setTitle(int title) {
        this.title = title;
    }

    @Override
    public int getDescription() {
        return description;
    }

    @Override
    public void setDescription(int description) {
        this.description = description;
    }

    @Override
    public String getConfig() {
        return config;
    }

    @Override
    public void setConfig(String config) {
        this.config = config;
    }

    @Override
    public User getOwner() {
        return owner;
    }

    @Override
    public void setOwner(User owner) {
        this.owner = owner;
    }

}