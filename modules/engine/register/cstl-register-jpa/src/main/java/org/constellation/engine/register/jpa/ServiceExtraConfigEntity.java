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
