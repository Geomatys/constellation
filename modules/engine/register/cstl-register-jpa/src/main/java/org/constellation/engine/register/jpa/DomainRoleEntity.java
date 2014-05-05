package org.constellation.engine.register.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.constellation.engine.register.DomainRole;

@Entity
@Table(schema="`admin`", name="`domainrole`")
public class DomainRoleEntity implements DomainRole  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="`id`")
    private int id;
    
    @Column(name="`name`")
    private String name;
    
    @Column(name="`description`")
    private String description;
    
    @Override
    public int getId() {
        return id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "DomainRoleEntity [id=" + id + ", name=" + name + ", description=" + description + "]";
    }
    
    
    
}
