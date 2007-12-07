package net.opengis.ogc;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *         &lt;element name="ServiceException" type="{http://www.opengis.net/ogc}ServiceExceptionType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}string" fixed="1.3.0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "serviceException"
})
@XmlRootElement(name = "ServiceExceptionReport")
public class ServiceExceptionReport {

    @XmlElement(name = "ServiceException")
    protected List<ServiceExceptionType> serviceException = new ArrayList<ServiceExceptionType>();
    @XmlAttribute
    protected String version;

    /**
     * An empty constructor used by jaxB
     */
    protected ServiceExceptionReport(ServiceExceptionType exception) {
        this.serviceException.add(exception);
        version = "1.3.0";
    }
    
    /**
     * Build a new Exception report with one exception
     */
    public ServiceExceptionReport() {
    }
    
    /**
     * Return a list of Service exception.
     */
    public List<ServiceExceptionType> getServiceException() {
        return this.serviceException;
    }

    /**
     * Gets the value of the version property.
     * 
     */
    public String getVersion() {
        if (version == null) {
            return "1.3.0";
        } else {
            return version;
        }
    }

    /**
     * Sets the value of the version property.
     * 
     */
    public void setVersion(String value) {
        this.version = value;
    }

}
