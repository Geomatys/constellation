/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.engine.register.jpa;


import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.constellation.engine.register.Data;
import org.constellation.engine.register.Provider;
import org.constellation.engine.register.User;



@Entity
@Table(schema = "`admin`", name = "`provider`")
public class ProviderEntity implements Provider {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    private int id;

    @Column(name = "`identifier`")
    private String identifier;

    @Column(name = "`type`")
    private String type;

    @Column(name = "`impl`")
    private String impl;

    @Column(name = "`config`")
    private String config;

    @Column(name = "`metadata`")
    private String metadata;

    @ManyToOne(targetEntity=UserEntity.class)
    @JoinColumn(name = "`owner`")
    private User owner;

    @OneToMany(mappedBy="provider", targetEntity=DataEntity.class)
    private List<Data> datas;
    
    @ManyToOne(targetEntity=ProviderEntity.class)
    @JoinColumn(name="`parent`", referencedColumnName="`identifier`")
    private Provider parent; 

    @Override
    public String toString() {
        return "Provider [id=" + id + ", identifier=" + identifier + ", type=" + type + ", impl=" + impl + ", config="
                + config + ", owner=" + owner + ", metadata=" + metadata + "]";
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    
    public void setParent(Provider parent) {
        this.parent = parent;
    }
    
    @Override
    public Provider getParent() {
        return parent;
    }
    
    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getImpl() {
        return impl;
    }

    @Override
    public void setImpl(String impl) {
        this.impl = impl;
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

    @Override
    public String getMetadata() {
        return metadata;
    }

    @Override
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

}
