package org.constellation.engine.register.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.constellation.engine.register.Role;

@Entity
@Table(name="`role`", schema="`admin`")
public class RoleEntity implements Role {

    @Id
    @Column(name="`name`")
    private String name;
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;        
    }
   
}
