package org.constellation.engine.register.jpa;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Id;


public class ServiceMetaDataEntityPk implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="`id`", insertable=false, updatable=false)
    int id;

    @Id
    @Column(name = "`lang`", insertable=false, updatable=false)
    String lang;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((lang == null) ? 0 : lang.hashCode());
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ServiceMetaDataEntityPk other = (ServiceMetaDataEntityPk) obj;
        if (lang == null) {
            if (other.lang != null)
                return false;
        } else if (!lang.equals(other.lang))
            return false;
        if (id != other.id)
            return false;
        return true;
    }
    

    
    
}
