/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
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
