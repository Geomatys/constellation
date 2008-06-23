
package net.seagis.swe;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import net.seagis.catalog.Entry;
import org.geotools.resources.Utilities;

/**
 * Cette classe n'as pas vraiment lieu d'etre.
 * Elle as été crée pour les besoin de JAXB qui ne supporte pas les interface.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({TextBlockEntry.class})
@XmlType(name="AbstractEncoding")
public class AbstractEncodingEntry extends Entry implements AbstractEncoding{
    
    /**
     * The encoding identifier.
     */
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    private String id;
    
    /**
     * constructor used by jaxB
     */
    AbstractEncodingEntry() {}

    /**
     *  An abstract encoding. used like super constructor
     */
    protected AbstractEncodingEntry(String id) {
        super(id);
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
    
    /**
     * Returne the numeric code identifiyng this entry.
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    /**
     * Verify that this entry is identical to the specified object. 
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
            final AbstractEncodingEntry that = (AbstractEncodingEntry) object;
            return Utilities.equals(this.id,              that.id);
    }
    
}
