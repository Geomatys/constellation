package net.seagis.wms.v111;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import net.seagis.wms.AbstractCapability;
import net.seagis.wms.AbstractLayer;


/**
 * <p>Java class for anonymous complex type.
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "request",
    "exception",
    "vendorSpecificCapabilities",
    "userDefinedSymbolization",
    "layer"
})
@XmlRootElement(name = "Capability")
public class Capability extends AbstractCapability {

    @XmlElement(name = "Request", required = true)
    private Request request;
    @XmlElement(name = "Exception", required = true)
    private Exception exception;
    @XmlElement(name = "VendorSpecificCapabilities")
    private VendorSpecificCapabilities vendorSpecificCapabilities;
    @XmlElement(name = "UserDefinedSymbolization")
    private UserDefinedSymbolization userDefinedSymbolization;
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
    public Capability(final Request request, final Exception exception, final Layer layer, UserDefinedSymbolization userDefinedSymbolization) {
        this.request   = request;
        this.exception = exception;
        this.layer     = layer;
        this.userDefinedSymbolization = userDefinedSymbolization;
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
     * Gets the value of the layer property.
     */
    public Layer getLayer() {
        return layer;
    }
    
     /**
     * Gets the value of the layer property.
     */
    public void setLayer(AbstractLayer layer) {
        if (layer instanceof Layer) {
            this.layer = (Layer) layer;
        } else {
            throw new IllegalArgumentException("not good version of layer. expected 1.1.1");
        }
    }

    public VendorSpecificCapabilities getVendorSpecificCapabilities() {
        return vendorSpecificCapabilities;
    }

    public UserDefinedSymbolization getUserDefinedSymbolization() {
        return userDefinedSymbolization;
    }

}
