package org.constellation.engine.register.pojo;

import java.io.Serializable;

/**
 * @author Fabien Bernard (Geomatys).
 */
public class ServiceReference implements Serializable {

    private static final long serialVersionUID = 7905763341389113756L;


    protected Integer id;

    protected String identifier;

    protected String type;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
