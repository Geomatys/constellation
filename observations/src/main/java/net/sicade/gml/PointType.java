package net.sicade.gml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import net.opengis.gml.AbstractGeometricPrimitiveType;
import org.geotools.resources.Utilities;


/**
 * Java class for PointType complex type.
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PointType", propOrder = {
    "pos",
    "coordinates"
})
public class PointType extends AbstractGeometricPrimitiveType {

    protected DirectPositionType pos;
    protected CoordinatesType coordinates;

    public PointType() {}
            
    public PointType(String id, DirectPositionType pos) {
        super.setId(id);
        this.pos = pos;
    }
     
    /**
     * Gets the value of the pos property.
     * 
     * @return
     *     possible object is
     *     {@link DirectPositionType }
     *     
     */
    public DirectPositionType getPos() {
        return pos;
    }

    /**
     * Sets the value of the pos property.
     * 
     * @param value
     *     allowed object is
     *     {@link DirectPositionType }
     *     
     */
    public void setPos(DirectPositionType value) {
        this.pos = value;
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
     * Retourne un description de l'objet.
     */
    @Override
    public String toString() {
        String s = "id = " + this.id + '\n'; 
        if(pos != null) {
            s = s + " position : " + pos.toString() + '\n'; 
        }
        
        if( coordinates != null) {
            s = s + " coordinates : " + coordinates.toString() + '\n'; 
        }
        
        return s;
    }
    
    /**
     * Vérifie que cette station est identique à l'objet spécifié
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final PointType that = (PointType) object;
            return  Utilities.equals(this.pos, that.pos) &&
                    Utilities.equals(this.coordinates, that.coordinates);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

}
