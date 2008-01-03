package net.seagis.gml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
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
@XmlRootElement(name="PointType")
public class PointType extends AbstractGeometricPrimitiveType {

    private DirectPositionType pos;
    private CoordinatesType coordinates;

    public PointType() {}
            
    public PointType(String id, DirectPositionType pos) {
        super.setId(id);
        this.pos = pos;
    }
     
    /**
     * Gets the value of the pos property.
     * 
     */
    public DirectPositionType getPos() {
        return pos;
    }

    /**
     * Gets the value of the coordinates property.
     */
    public CoordinatesType getCoordinates() {
        return coordinates;
    }
   
    
    /**
     * Retourne un description de l'objet.
     */
    @Override
    public String toString() {
        String s = "id = " + this.getId() + '\n'; 
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
        return this.getId().hashCode();
    }

}
