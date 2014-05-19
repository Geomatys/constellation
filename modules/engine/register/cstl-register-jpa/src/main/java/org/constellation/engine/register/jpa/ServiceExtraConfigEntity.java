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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.constellation.engine.register.Service;
import org.constellation.engine.register.ServiceExtraConfig;

@Entity
@IdClass(ServiceExtraConfigEntityPk.class)
@Table(schema = "`admin`", name = "`service_extra_config`")
public class ServiceExtraConfigEntity implements ServiceExtraConfig {
    @Id
    @Column(name="`id`")
    private int id;
    @Id
    @Column(name = "`filename`")
    private String filename;

    @Column(name = "`content`")
    private String content;
    
    @ManyToOne(targetEntity=ServiceEntity.class)
    @JoinColumn(name="`id`")
    private Service service;


    @Override
    public void setFilename(String filename) {
        this.filename = filename;
        
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }
    
    @Override
    public String getContent() {
        return content;
    }

    @Override
    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public Service getService() {
        return service;
    }

    @Override
    public void setService(Service service) {
        this.service = service;
    }


   
}
