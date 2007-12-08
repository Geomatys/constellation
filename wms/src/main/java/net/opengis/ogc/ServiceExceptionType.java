package net.opengis.ogc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * <p>Java class for ServiceExceptionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ServiceExceptionType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *       &lt;attribute name="code" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="locator" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServiceExceptionType", propOrder = {
    "value"
})
public class ServiceExceptionType {

    @XmlValue
    protected String value;
    @XmlAttribute
    protected String code;
    @XmlAttribute
    protected String locator;

    /**
     * empty constructor used by JAXB
     */
    protected ServiceExceptionType() {}
    
    /**
     * Build a new Exception with the specified message and code.
     * 
     * @param value The message of the exception
     * @param code A standard code for exception (OWS).
     */
    public ServiceExceptionType(String value, String code) {
        this.value   = value;
        this.code    = code;
        this.locator = null;
    }
    
    /**
     * Gets the value of the value property.
     * 
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the code property.
     * 
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     */
    public void setCode(String value) {
        this.code = value;
    }

    /**
     * Gets the value of the locator property.
     * 
     */
    public String getLocator() {
        return locator;
    }

    /**
     * Sets the value of the locator property.
     */
    public void setLocator(String value) {
        this.locator = value;
    }

}
