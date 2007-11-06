package net.seagis.sos;

import net.seagis.catalog.Entry;
import net.seagis.observation.ProcessEntry;

/**
 *
 * @author legal
 */
public class OfferingProcedureEntry extends Entry {

    /**
     * L'identifiant de l'offering.
     */
    private String idOffering;
    
    /**
     * Le process associe a cet offering.
     */
    private ProcessEntry component;
    
    /**
     * Cree une nouveau lien entre une procedure et un offering. 
     */
    public OfferingProcedureEntry(String idOffering, ProcessEntry component) {
        super(component.getHref());
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
    public ProcessEntry getComponent() {
        return component;
    }
    
}
