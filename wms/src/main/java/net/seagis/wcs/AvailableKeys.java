package net.seagis.wcs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Key" type="{http://www.opengis.net/wcs/1.1.1}IdentifierType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "key"
})
@XmlRootElement(name = "AvailableKeys")
public class AvailableKeys {

    @XmlElement(name = "Key", required = true)
    protected List<String> key = new ArrayList<String>();

    /**
     * An empty constructor used by JAXB.
     */
    AvailableKeys() {
    }
    
    /**
     * Build a new Available Keys
     */
    public AvailableKeys(String... keys) {
        for (final String element : keys) {
            key.add(element);
        }
    }
    
    /**
     * Gets the value of the key property.
     */
    public List<String> getKey() {
       return Collections.unmodifiableList(key);
    }

}
