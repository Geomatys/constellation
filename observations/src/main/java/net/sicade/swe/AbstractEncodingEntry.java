
package net.sicade.swe;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import net.sicade.catalog.Entry;
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
public class AbstractEncodingEntry extends Entry implements AbstractEncoding{
    
    /**
     * l'identifiant de l'encodage
     */
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    private String id;
    
    /**
     * constructeur utilisé par jaxB
     */
    protected AbstractEncodingEntry() {}

    /**
     *  Un encodage abstrait. utilisé comme super constructeur
     */
    protected AbstractEncodingEntry(String id) {
        super(id);
        this.id = id;
    }
    
    @Override
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
     * Vérifie que cette station est identique à l'objet spécifié
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
