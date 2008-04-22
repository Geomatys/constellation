package net.seagis.wms;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
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
 *         &lt;element name="Name">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="WMS"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element ref="{http://www.opengis.net/wms}Title"/>
 *         &lt;element ref="{http://www.opengis.net/wms}Abstract" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wms}KeywordList" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wms}OnlineResource"/>
 *         &lt;element ref="{http://www.opengis.net/wms}ContactInformation" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wms}Fees" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wms}AccessConstraints" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wms}LayerLimit" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wms}MaxWidth" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wms}MaxHeight" minOccurs="0"/>
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
    "name",
    "title",
    "_abstract",
    "keywordList",
    "onlineResource",
    "contactInformation",
    "fees",
    "accessConstraints",
    "layerLimit",
    "maxWidth",
    "maxHeight"
})
@XmlRootElement(name = "Service")
public class Service {

    @XmlElement(name = "Name", required = true)
    private String name;
    @XmlElement(name = "Title", required = true)
    private String title;
    @XmlElement(name = "Abstract")
    private String _abstract;
    @XmlElement(name = "KeywordList")
    private KeywordList keywordList;
    @XmlElement(name = "OnlineResource", required = true)
    private OnlineResource onlineResource;
    @XmlElement(name = "ContactInformation")
    private ContactInformation contactInformation;
    @XmlElement(name = "Fees")
    private String fees;
    @XmlElement(name = "AccessConstraints")
    private String accessConstraints;
    @XmlElement(name = "LayerLimit")
    @XmlSchemaType(name = "positiveInteger")
    private int layerLimit;
    @XmlElement(name = "MaxWidth")
    @XmlSchemaType(name = "positiveInteger")
    private int maxWidth;
    @XmlElement(name = "MaxHeight")
    @XmlSchemaType(name = "positiveInteger")
    private int maxHeight;

    /**
     * An empty constructor used by JAXB.
     */
     Service() {
     }

    /**
     * Build a new Service object.
     */
    public Service(final String name, final String title, final String _abstract,
            final KeywordList keywordList, final OnlineResource onlineResource, 
            final ContactInformation contactInformation, String fees, String accessConstraints,
            final int layerLimit, final int maxWidth, final int maxHeight) {
        
        this._abstract          = _abstract;
        this.name               = name;
        this.onlineResource     = onlineResource;
        this.accessConstraints  = accessConstraints;
        this.contactInformation = contactInformation;
        this.fees               = fees;
        this.keywordList        = keywordList;
        this.layerLimit         = layerLimit;
        this.maxHeight          = maxHeight;
        this.maxWidth           = maxWidth;
        this.title              = title;
    }
    
    
    /**
     * Gets the value of the name property.
     * 
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the value of the title property.
     */
    public String getTitle() {
        return title;
    }

   /**
     * Gets the value of the abstract property.
     * 
     */
    public String getAbstract() {
        return _abstract;
    }

    /**
     * Gets the value of the keywordList property.
     */
    public KeywordList getKeywordList() {
        return keywordList;
    }

    /**
     * Gets the value of the onlineResource property.
     */
    public OnlineResource getOnlineResource() {
        return onlineResource;
    }

    /**
     * Gets the value of the contactInformation property.
     * 
     */
    public ContactInformation getContactInformation() {
        return contactInformation;
    }

    /**
     * Gets the value of the fees property.
     */
    public String getFees() {
        return fees;
    }

   /**
    * Gets the value of the accessConstraints property.
    * 
    */
    public String getAccessConstraints() {
        return accessConstraints;
    }

    /**
     * Gets the value of the layerLimit property.
     */
    public int getLayerLimit() {
        return layerLimit;
    }

    /**
     * Gets the value of the maxWidth property.
     */
    public int getMaxWidth() {
        return maxWidth;
    }

   /**
    * Gets the value of the maxHeight property.
    *     
    */
    public int getMaxHeight() {
        return maxHeight;
    }
}
