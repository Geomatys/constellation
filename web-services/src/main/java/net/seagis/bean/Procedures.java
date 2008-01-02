/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package net.seagis.bean;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

import java.util.Collection;

/**
 *
 * @author olivier
 */
public class Procedures implements Serializable {
    private static final long serialVersionUID = 1L;
    private String            description;
    private Layers[]          layersCollection;
    private String            name;

    public Procedures() {}

    public Procedures(String name) {
        this.name = name;
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

    public Layers[] getLayersCollection() {
        return layersCollection;
    }

    public void setLayersCollection(Layers[] layersCollection) {
        this.layersCollection = layersCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash += ((name != null)
                 ? name.hashCode()
                 : 0);

        return hash;
    }

    @Override
    public boolean equals(Object object) {

        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Procedures)) {
            return false;
        }

        Procedures other = (Procedures) object;

        if (((this.name == null) && (other.name != null)) || ((this.name != null) &&!this.name.equals(other.name))) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "fr.geomatys.bean.Procedures[name=" + name + "]";
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
