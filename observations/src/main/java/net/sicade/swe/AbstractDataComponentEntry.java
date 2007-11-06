
package net.sicade.swe;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import net.sicade.catalog.Entry;
import org.geotools.resources.Utilities;

/**
 *
 * @version $Id:
 * @author Guilhem Legal
 */
@XmlSeeAlso({AbstractDataRecordEntry.class})
public class AbstractDataComponentEntry extends Entry implements AbstractDataComponent{
    
    /**
     * L'identifiant du composant (normalement herité de abstractGML Type).
     */
    @XmlAttribute
    private String id;
    
    @XmlAttribute
    private boolean fixed;
    
    /**
     * definition du record.
     */
    private String definition;
    
    /**
     * Constructeur utilisé par jaxb.
     */
    public AbstractDataComponentEntry() {}
    
    /**
     * un simple constructeur utilisé par les sous classes pour initialisé l'Entry.
     */
    public AbstractDataComponentEntry(String id, String definition, boolean fixed) {
        super(id);
        this.id         = id;
        this.definition = definition;
        this.fixed      = fixed;
    }
    
    /**
     * Retourne l'identifiant de ce data record.
     */
    public String getId() {
        return id;
    }
    
    /**
     * Retourne le code numérique identifiant cette entrée.
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefinition() {
        return definition;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFixed() {
        return fixed;
    }
    
    /**
     * Vérifie que cette station est identique à l'objet spécifié
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        final AbstractDataComponentEntry that = (AbstractDataComponentEntry) object;
        return Utilities.equals(this.id,         that.id)         &&
               Utilities.equals(this.definition, that.definition) &&
               Utilities.equals(this.fixed,      that.fixed);
    }
    
}
