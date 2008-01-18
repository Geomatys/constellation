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
 *           &lt;element name="lowerCorner" type="{http://www.opengis.net/gml}DirectPositionType"/>
 *           &lt;element name="upperCorner" type="{http://www.opengis.net/gml}DirectPositionType"/>
 *         &lt;/sequence>
 *         &lt;element ref="{http://www.opengis.net/gml}pos" maxOccurs="2" minOccurs="2"/>
 *         &lt;element ref="{http://www.opengis.net/gml}coordinates"/>
 *       &lt;/choice>
 *       &lt;attGroup ref="{http://www.opengis.net/gml}SRSReferenceGroup"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EnvelopeType",
         namespace = "http://www.opengis.net/gml",
         propOrder = {
    "id",
    "lowerCorner",
    "upperCorner",
    "pos",
    "coordinates"
})
public class EnvelopeEntry extends Entry {

    private String id;
    private DirectPositionType lowerCorner;
    private DirectPositionType upperCorner;
    private List<DirectPositionType> pos;
    private CoordinatesType coordinates;
    @XmlAttribute
    private Integer srsDimension;
    @XmlAttribute
    private String srsName;
    @XmlAttribute
    private List<String> axisLabels;
    @XmlAttribute
    private List<String> uomLabels;

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
     * Construit une nouvelle envelope.  
     */
    public EnvelopeEntry(List<DirectPositionType> pos, String srsName) {
        this.srsName      = srsName;
        this.pos          = pos;
        this.srsDimension = null;
    }
    /**
     * Gets the value of the lowerCorner property.
     */
    public DirectPositionType getLowerCorner() {
        return lowerCorner;
    }

    /**
     * Gets the value of the upperCorner property.
     * 
     */
    public DirectPositionType getUpperCorner() {
        return upperCorner;
    }

    /**
     * Gets the value of the pos property.
     */
    public List<DirectPositionType> getPos() {
        if (pos == null) {
            pos = new ArrayList<DirectPositionType>();
        }
        return this.pos;
    }

    /**
     * Gets the value of the coordinates property.
     */
    public CoordinatesType getCoordinates() {
        return coordinates;
    }

    /**
     * Gets the value of the srsDimension property.
     * 
     */
    public int getSrsDimension() {
        return srsDimension;
    }

    /**
     * Gets the value of the srsName property.
     */
    public String getSrsName() {
        return srsName;
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
