package net.sicade.sos;

import net.sicade.catalog.Entry;
import net.sicade.observation.SamplingFeatureEntry;

/**
 *
 * @author legal
 */
public class OfferingSamplingFeatureEntry extends Entry {

    /**
     * L'identifiant de l'offering.
     */
    private String idOffering;
    
    /**
     * La station associe a cet offering.
     */
    private SamplingFeatureEntry component;
    
    /**
     * Cree une nouveau lien entre une Station et un offering. 
     */
    public OfferingSamplingFeatureEntry(String idOffering, SamplingFeatureEntry component) {
        super(component.getId());
        this.idOffering = idOffering;
        this.component  = component;
    }

    /**
     * Retourne l'id de l'offering.
     */
    public String getIdOffering() {
        return idOffering;
    }

    /**
     * Retourne le process associe a cet offering.
     */
    public SamplingFeatureEntry getComponent() {
        return component;
    }
    
}
