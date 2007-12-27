package net.seagis.sld;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DescribeLayerResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DescribeLayerResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Version" type="{http://www.opengis.net/ows}VersionType"/>
 *         &lt;element name="LayerDescription" type="{http://www.opengis.net/sld}LayerDescriptionType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DescribeLayerResponseType", propOrder = {
    "version",
    "layerDescription"
})
public class DescribeLayerResponseType {

    @XmlElement(name = "Version", required = true)
    private String version;
    @XmlElement(name = "LayerDescription", required = true)
    private List<LayerDescriptionType> layerDescription = new ArrayList<LayerDescriptionType>();

    /**
     * An empty constructor used by JAXB
     */
    DescribeLayerResponseType() {}
    
    /**
     * Build a new response to a DescribeLayer request.
     * 
     * @param version the version of sld specification.
     * @param layerDescriptions a list of layer description.
     */
    public DescribeLayerResponseType(String version, LayerDescriptionType... layerDescriptions) {
        this.version = version;
        for (final LayerDescriptionType element : layerDescriptions) {
            this.layerDescription.add(element);
        }
    }
        
    /**
     * Gets the value of the version property.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the value of the layerDescription property.
     */
    public List<LayerDescriptionType> getLayerDescription() {
        return this.layerDescription;
    }

}
