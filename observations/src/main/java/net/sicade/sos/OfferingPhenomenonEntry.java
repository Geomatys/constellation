/*
 * OfferingPhenomenon.java
 * 
 * Created on 10 oct. 2007, 18:05:27
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sicade.sos;

import net.sicade.catalog.Entry;
import net.sicade.observation.PhenomenonEntry;

/**
 *
 * @author legal
 */
public class OfferingPhenomenonEntry extends Entry {
    
    /**
     * L'identifiant de l'offering.
     */
    private String idOffering;
    
    /**
     * Le phenomene associe a cet offering.
     */
    private PhenomenonEntry component;
    
    /**
     * Cree une nouveau lien entre une procedure et un offering. 
     */
    public OfferingPhenomenonEntry(String idOffering, PhenomenonEntry component) {
        super(component.getId());
        this.idOffering = idOffering;
        this.component  = component;
    }

    /**
     * Retourne l'id du phénomène composé.
     */
    public String getIdOffering() {
        return idOffering;
    }

    /**
     * Retourne le phénomène associé.
     */
    public PhenomenonEntry getComponent() {
        return component;
    }

}
