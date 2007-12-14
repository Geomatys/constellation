package net.opengis.sld;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TypeNameType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TypeNameType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element ref="{http://www.opengis.net/se}FeatureTypeName"/>
 *         &lt;element ref="{http://www.opengis.net/se}CoverageName"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TypeNameType")
public class TypeNameType {

    @XmlElement(name = "CoverageName", namespace = "http://www.opengis.net/se")
    private String coverageName;

    TypeNameType() {        
    }
    
    public TypeNameType(String coverageName) {
        this.coverageName = coverageName;
    }
    
    /**
     * Gets the value of the coverageName property.
     */
    public String getCoverageName() {
        return coverageName;
    }
}
