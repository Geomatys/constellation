
package net.sicade.observation;

import net.sicade.catalog.Entry;
import org.geotools.util.GenericName;
import org.opengis.observation.sampling.SamplingFeatureRelation;

/**
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class SamplingFeatureRelationEntry extends Entry implements SamplingFeatureRelation {
    
    private GenericName role;
    
    private SamplingFeatureEntry target;
    
    /**
     * Constructeur vide utilisé par JAXB
     */
    private SamplingFeatureRelationEntry() {}
    
    /**
     */
    public SamplingFeatureRelationEntry(String name, GenericName role, SamplingFeatureEntry target) {
        super(name);
        this.role   = role;
        this.target = target;
    }
    
    /**
     * {@inheritDoc}
     */
    public GenericName getRole(){
        return role;
    }
    
    /**
     * {@inheritDoc}
     */
    public SamplingFeatureEntry getTarget(){
        return target;
    }
}
