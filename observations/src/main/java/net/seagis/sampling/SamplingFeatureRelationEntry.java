
package net.seagis.sampling;

import javax.xml.bind.annotation.XmlType;
import net.seagis.catalog.Entry;
import org.opengis.observation.sampling.SamplingFeatureRelation;
import org.opengis.util.GenericName;

/**
 *
 * @version $Id:
 * @author Guilhem Legal
 */
@XmlType(name="SamplingFeatureRelation")
public class SamplingFeatureRelationEntry extends Entry implements SamplingFeatureRelation {
    
    // JAXBISSUE private GenericNameEntry role;
    
    private SamplingFeatureEntry target;
    
    /**
     * Constructeur vide utilisé par JAXB
     */
    private SamplingFeatureRelationEntry() {}
    
    /**
     */
    public SamplingFeatureRelationEntry(String name, SamplingFeatureEntry target) {
        super(name);
        //this.role   = role;
        this.target = target;
    }
    
    /**
     * {@inheritDoc}
     */
    public GenericName getRole(){
        throw new UnsupportedOperationException("Not supported yet.");
        //return role;
    }
    
    /**
     * {@inheritDoc}
     */
    public SamplingFeatureEntry getTarget(){
        return target;
    }
}
