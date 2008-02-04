/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package net.seagis.bean;

import java.io.Serializable;

/**
 *
 * @author olivier
 */
public class Layers implements Serializable {
    private boolean markedForDeletion = false;

    // private Layers[] layersArray;
    // private Layers fallback;

    /*
     * private Layers fallback;
     * private String procedure;
     * private String thematic;
     */
    private String description;
    private String name;
    private Double period;

    public Layers(String name) {
        this.name = name;
    }

    public Layers(String name, Double period, String description) {
        this.name        = name;
        this.period      = period;
        this.description = description;

        /* layersArray = read.getAll(); */
    }

    public boolean isMarkedForDeletion() {
        return markedForDeletion;
    }

    public void setMarkedForDeletion(boolean newValue) {
        markedForDeletion = newValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPeriod() {
        return period;
    }

    public void setPeriod(Double period) {
        this.period = period;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /*
     *  public Layers[] getLayersCollection() {
     *    return layersCollection;
     * }
     *
     * public void setLayersCollection(Layers[] layersCollection) {
     *    this.layersCollection = layersCollection;
     * }
     *
     * public Layers getFallback() {
     *    return fallback;
     * }
     *
     * public void setFallback(Layers fallback) {
     *    this.fallback = fallback;
     * }
     *
     * public String getProcedure() {
     *    return procedure;
     * }
     *
     * public void setProcedure(String procedure) {
     *    this.procedure = procedure;
     * }
     *
     * public String getThematic() {
     *    return thematic;
     * }
     *
     * public void setThematic(String thematic) {
     *    this.thematic = thematic;
     * }
     */
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
        if (!(object instanceof Layers)) {
            return false;
        }

        Layers other = (Layers) object;

        if (((this.name == null) && (other.name != null)) || ((this.name != null) &&!this.name.equals(other.name))) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "fr.geomatys.bean.Layers[name=" + name + "]";
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
