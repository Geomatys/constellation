package net.seagis.gml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import net.seagis.catalog.Entry;
import org.geotools.resources.Utilities;


/**
 * <p>Java class for EnvelopeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EnvelopeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;sequence>
 *           &lt;element name="lowerCorner" type="{http://www.opengis.net/gml/3.2}DirectPositionType"/>
 *           &lt;element name="upperCorner" type="{http://www.opengis.net/gml/3.2}DirectPositionType"/>
 *         &lt;/sequence>
 *         &lt;element ref="{http://www.opengis.net/gml/3.2}pos" maxOccurs="2" minOccurs="2"/>
 *         &lt;element ref="{http://www.opengis.net/gml/3.2}coordinates"/>
 *       &lt;/choice>
 *       &lt;attGroup ref="{http://www.opengis.net/gml/3.2}SRSReferenceGroup"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EnvelopeType",
         namespace = "http://www.opengis.net/gml/3.2",
         propOrder = {
    "id",
    "lowerCorner",
    "upperCorner",
    "pos",
    "coordinates"
})
public class EnvelopeEntry extends Entry {

    protected String id;
    protected DirectPositionType lowerCorner;
    protected DirectPositionType upperCorner;
    protected List<DirectPositionType> pos;
    protected CoordinatesType coordinates;
    @XmlAttribute
    protected int srsDimension;
    @XmlAttribute
    protected String srsName;
    @XmlAttribute
    protected List<String> axisLabels;
    @XmlAttribute
    protected List<String> uomLabels;

    /**
     * Un constructeur vide utilis par JAXB.
     */
    public EnvelopeEntry(){}
    
    /**
     * Construit une nouvelle envelope.  
     */
    public EnvelopeEntry(String id, DirectPositionType lowerCorner, DirectPositionType upperCorner
            , String srsName) {
        super(id);
        this.lowerCorner = lowerCorner;
        this.upperCorner = upperCorner;
        this.id          = id;
        this.srsName     = srsName;
    }
    /**
     * Gets the value of the lowerCorner property.
     * 
     * @return
     *     possible object is
     *     {@link DirectPositionType }
     *     
     */
    public DirectPositionType getLowerCorner() {
        return lowerCorner;
    }

    /**
     * Sets the value of the lowerCorner property.
     * 
     * @param value
     *     allowed object is
     *     {@link DirectPositionType }
     *     
     */
    public void setLowerCorner(DirectPositionType value) {
        this.lowerCorner = value;
    }

    /**
     * Gets the value of the upperCorner property.
     * 
     * @return
     *     possible object is
     *     {@link DirectPositionType }
     *     
     */
    public DirectPositionType getUpperCorner() {
        return upperCorner;
    }

    /**
     * Sets the value of the upperCorner property.
     * 
     * @param value
     *     allowed object is
     *     {@link DirectPositionType }
     *     
     */
    public void setUpperCorner(DirectPositionType value) {
        this.upperCorner = value;
    }

    /**
     * Gets the value of the pos property.
     * 
     */
    public List<DirectPositionType> getPos() {
        if (pos == null) {
            pos = new ArrayList<DirectPositionType>();
        }
        return this.pos;
    }

    /**
     * Gets the value of the coordinates property.
     * 
     * @return
     *     possible object is
     *     {@link CoordinatesType }
     *     
     */
    public CoordinatesType getCoordinates() {
        return coordinates;
    }

    /**
     * Sets the value of the coordinates property.
     * 
     * @param value
     *     allowed object is
     *     {@link CoordinatesType }
     *     
     */
    public void setCoordinates(CoordinatesType value) {
        this.coordinates = value;
    }

    /**
     * Gets the value of the srsDimension property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public int getSrsDimension() {
        return srsDimension;
    }

    /**
     * Sets the value of the srsDimension property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSrsDimension(int value) {
        this.srsDimension = value;
    }

    /**
     * Gets the value of the srsName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSrsName() {
        return srsName;
    }

    /**
     * Sets the value of the srsName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSrsName(String value) {
        this.srsName = value;
    }

    /**
     * Gets the value of the axisLabels property.
     */
    public List<String> getAxisLabels() {
        if (axisLabels == null) {
            axisLabels = new ArrayList<String>();
        }
        return this.axisLabels;
    }

    /**
     * Gets the value of the uomLabels property.
     * 
     */
    public List<String> getUomLabels() {
        if (uomLabels == null) {
            uomLabels = new ArrayList<String>();
        }
        return this.uomLabels;
    }
    /**
     * Verifie si cette entree est identique a l'objet specifie.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final EnvelopeEntry that = (EnvelopeEntry) object;

            return Utilities.equals(this.axisLabels,          that.axisLabels)           &&
                   Utilities.equals(this.coordinates,   that.coordinates)   &&
                   Utilities.equals(this.id,          that.id) &&
                   Utilities.equals(this.lowerCorner,    that.lowerCorner)    &&
                   Utilities.equals(this.pos,           that.pos)           &&
                   Utilities.equals(this.srsDimension,      that.srsDimension)      &&
                   Utilities.equals(this.uomLabels,        that.uomLabels)        &&
                   Utilities.equals(this.upperCorner,         that.upperCorner)         &&
                   Utilities.equals(this.srsName,             that.srsName);
        } 
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 67 * hash + this.srsDimension;
        hash = 67 * hash + (this.srsName != null ? this.srsName.hashCode() : 0);
        return hash;
    }


}
