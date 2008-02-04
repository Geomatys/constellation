/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.seagis.sos;

import net.seagis.catalog.Entry;

/**
 *
 * @author legal
 */
public class OfferingResponseModeEntry extends Entry{

    /**
     * L'identifiant de l'offering.
     */
    private String idOffering;
    
    /**
     * Le mode de reponse associe a cet offering.
     */
    private ResponseModeType mode;
    
    /**
     * Cree une nouveau lien entre une procedure et un offering. 
     */
    public OfferingResponseModeEntry(String idOffering, ResponseModeType mode) {
        super(mode.value());
        this.idOffering = idOffering;
        this.mode  = mode;
    }

    /**
     * Retourne l'id de l'offering
     */
    public String getIdOffering() {
        return idOffering;
    }

    /**
     * Retourne le mode de reponse associe.
     */
    public ResponseModeType getMode() {
        return mode;
    }

}
