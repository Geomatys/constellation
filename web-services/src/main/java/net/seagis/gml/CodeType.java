
package net.seagis.gml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import org.geotools.resources.Utilities;


/**
 * gml:CodeType is a generalized type to be used for a term, keyword or name.
 * It adds a XML attribute codeSpace to a term, where the value of the codeSpace attribute (if present) shall indicate a dictionary, thesaurus, classification scheme, authority, or pattern for the term.
 * 
 * <p>Java class for CodeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CodeType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *       &lt;attribute name="codeSpace" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CodeType", namespace="http://www.opengis.net/gml", propOrder = {
    "value"
})
public class CodeType {

    @XmlValue
    private String value;
    @XmlAttribute
    private String codeSpace;

    /**
     * An empty constructor used by JAXB.
     */
    CodeType(){
    }
    
    /**
     * build a full CodeType.
     */
    public CodeType(String value, String codeSpace){
        this.codeSpace = codeSpace;
        this.value     = value;
    }
    
    /**
     * build a CodeType with no codespace.
     */
    public CodeType(String value){
        this.value     = value;
    }
    
    /**
     * Gets the value of the value property.
     * 
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the value of the codeSpace property.
     * 
     */
    public String getCodeSpace() {
        return codeSpace;
    }

    /**
     * Verifie si cette entree est identique l'objet specifie.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        final CodeType that = (CodeType) object;

        return Utilities.equals(this.codeSpace, that.codeSpace) &&
               Utilities.equals(this.value,     that.value);
        
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.value != null ? this.value.hashCode() : 0);
        hash = 97 * hash + (this.codeSpace != null ? this.codeSpace.hashCode() : 0);
        return hash;
    }


}
