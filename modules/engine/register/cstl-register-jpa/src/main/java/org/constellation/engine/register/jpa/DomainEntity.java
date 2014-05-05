package org.constellation.engine.register.jpa;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.constellation.engine.register.Data;
import org.constellation.engine.register.Domain;
import org.constellation.engine.register.Layer;
import org.constellation.engine.register.Service;
import org.constellation.engine.register.Style;

@Entity
@Table(schema = "`admin`", name = "`domain`")
public class DomainEntity implements Domain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    private int id;

    @Column(name = "`name`")
    private String name;

    @Column(name = "`description`")
    private String description;

    @ManyToMany(targetEntity = ServiceEntity.class)
    @JoinTable(schema="`admin`", name = "`service_x_domain`", 
       joinColumns = @JoinColumn(name = "`domain_id`", referencedColumnName = "`id`"),
       inverseJoinColumns = @JoinColumn(name = "`service_id`", referencedColumnName = "`id`"))
    private Set<Service> services;
    
    @ManyToMany(targetEntity = LayerEntity.class)
    @JoinTable(schema="`admin`", name = "`layer_x_domain`", 
       joinColumns = @JoinColumn(name = "`domain_id`", referencedColumnName = "`id`"),
       inverseJoinColumns = @JoinColumn(name = "`layer_id`", referencedColumnName = "`id`"))
    private Set<Layer> layers;
    
    @ManyToMany(targetEntity = DataEntity.class)
    @JoinTable(schema="`admin`", name = "`data_x_domain`", 
       joinColumns = @JoinColumn(name = "`domain_id`", referencedColumnName = "`id`"),
       inverseJoinColumns = @JoinColumn(name = "`data_id`", referencedColumnName = "`id`"))
    private Set<Data> datas;

    @ManyToMany(targetEntity = StyleEntity.class)
    @JoinTable(schema="`admin`", name = "`style_x_domain`", 
       joinColumns = @JoinColumn(name = "`domain_id`", referencedColumnName = "`id`"),
       inverseJoinColumns = @JoinColumn(name = "`style_id`", referencedColumnName = "`id`"))
    private Set<Style> styles;
    
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Set<? extends Layer> getLayers() {
        return layers;
    }
    
    
    public void setLayers(Set<Layer> layers) {
        this.layers = layers;
    }

    
   
    @Override
    public Set<? extends Data> getDatas() {
        return datas;
    }
    
    @Override
    public Set<? extends Style> getStyles() {
        return styles;
    }
    
    @Override
    public String toString() {
        return "DomainEntity [id=" + id + ", name=" + name + ", description=" + description + "]";
    }

    @Override
    public Set<? extends Service> getServices() {
        return services;
    }

    @Override
    public void setServices(Set<Service> services) {
        this.services = services;
        
    }





}
