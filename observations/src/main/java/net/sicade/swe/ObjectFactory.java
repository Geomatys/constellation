
package net.sicade.swe;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 *
 * @version $Id:
 * @author Guilhem Legal
 */
@XmlRegistry
public class ObjectFactory {
    
    private final static QName _Point_QNAME = new QName("http://www.opengis.net/swe/1.0", "Point");
    /**
     *
     */
    public ObjectFactory() {
    }
    
     /**
     * Create an instance of {@link Point }
     * 
     */
    public Point createPoint() {
        return new Point();
    }
    
     /**
     * Create an instance of {@link Position }
     * 
     */
    public Position createPosition() {
        return new Position();
    }
    
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ObservationEntry }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/swe/1.0", name = "Point")
    public JAXBElement<Point> createPoint(Point value) {
        return new JAXBElement<Point>(_Point_QNAME, Point.class, null, value);
    }
    
  

}
