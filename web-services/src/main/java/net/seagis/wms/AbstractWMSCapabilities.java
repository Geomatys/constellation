package net.seagis.wms;

/**
 *
 * @author legal
 */
public abstract class AbstractWMSCapabilities {

     /**
     * Gets the value of the service property.
     * 
     */
    public abstract Service getService(); 
    
    /**
     * Gets the value of the capability property.
     * 
     */
    public abstract Capability getCapability();

    /**
     * Gets the value of the version property.
     * 
     */
    public abstract String getVersion(); 

    /**
     * Gets the value of the updateSequence property.
     * 
     */
    public abstract String getUpdateSequence();
}
