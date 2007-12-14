package net.opengis.wms;

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
 *         &lt;element ref="{http://www.opengis.net/wms}Request"/>
 *         &lt;element ref="{http://www.opengis.net/wms}Exception"/>
 *         &lt;element ref="{http://www.opengis.net/wms}_ExtendedCapabilities" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wms}Layer" minOccurs="0"/>
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
    "request",
    "exception",
    "extendedCapabilities",
    "layer"
})
@XmlRootElement(name = "Capability")
public class Capability {

    @XmlElement(name = "Request", required = true)
    private Request request;
    @XmlElement(name = "Exception", required = true)
    private Exception exception;
    @XmlElement(name = "_ExtendedCapabilities")
    private List<Object> extendedCapabilities = new ArrayList<Object>();
    @XmlElement(name = "Layer")
    private Layer layer;

     /**
     * An empty constructor used by JAXB.
     */
     Capability() {
     }

    /**
     * Build a new capability object.
     */
    public Capability(final Request request, final Exception exception, final Layer layer,
            final Object... extendedCapabilities) {
        this.request   = request;
        this.exception = exception;
        this.layer     = layer;
        for (final Object element : extendedCapabilities) {
            this.extendedCapabilities.add(element);
        }
    }
    /**
     * Gets the value of the request property.
     */
    public Request getRequest() {
        return request;
    }

    /**
     * Gets the value of the exception property.
     */
    public Exception getException() {
        return exception;
    }

    /**
     * Gets the value of the extendedCapabilities property.
     */
    public List<Object> getExtendedCapabilities() {
        return Collections.unmodifiableList(extendedCapabilities);
    }

    /**
     * Gets the value of the layer property.
     */
    public Layer getLayer() {
        return layer;
    }
    
     /**
     * Gets the value of the layer property.
     */
    public void setLayer(Layer layer) {
        return layer;
    }

}
