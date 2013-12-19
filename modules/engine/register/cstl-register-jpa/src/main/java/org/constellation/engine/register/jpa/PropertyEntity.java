package org.constellation.engine.register.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.constellation.engine.register.Property;

@Entity
@Table(schema="`admin`", name="`property`")
public class PropertyEntity implements Property {

    @Id
    @Column(name="`key")
    private String key;
    
    @Column(name="`value`")
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    }
