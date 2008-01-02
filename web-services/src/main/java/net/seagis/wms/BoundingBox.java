package net.seagis.wms;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.geotools.util.Version;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="CRS" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="minx" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="miny" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="maxx" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="maxy" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="resx" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="resy" type="{http://www.w3.org/2001/XMLSchema}double" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "BoundingBox")
public class BoundingBox {

    /**
     * version 1.3.0
     */
    @XmlAttribute(name = "CRS")
    private String crs;
    /**
     * version 1.1.1
     */
    @XmlAttribute(name = "SRS")
    private String srs;
    
    @XmlAttribute(required = true)
    private double minx;
    @XmlAttribute(required = true)
    private double miny;
    @XmlAttribute(required = true)
    private double maxx;
    @XmlAttribute(required = true)
    private double maxy;
    @XmlAttribute
    private Double resx;
    @XmlAttribute
    private Double resy;

    /**
     * An empty constructor used by JAXB.
     */
    BoundingBox() {
    }

    /**
     * Build a new bounding box version 1.3.0.
     *
     */
    public BoundingBox(final String crs, final double minx, final double miny,
            final double maxx, final double maxy, final double resx, final double resy, Version version) {
        this.maxx = maxx;
        this.maxy = maxy;
        this.minx = minx;
        this.miny = miny;
       
        if (version.toString().equals("1.3.0")){
            this.crs  = crs;
            this.resx = resx;
            this.resy = resy;
        }
        else {
            this.srs = crs;
            this.resx = null;
            this.resy = null;
        }
    }
    
    /**
     * Gets the value of the crs property.
     */
    public String getCRS() {
        return crs;
    }

    /**
     * Gets the value of the minx property.
     * 
     */
    public double getMinx() {
        return minx;
    }

    /**
     * Gets the value of the miny property.
     * 
     */
    public double getMiny() {
        return miny;
    }

    /**
     * Gets the value of the maxx property.
     * 
     */
    public double getMaxx() {
        return maxx;
    }

    /**
     * Gets the value of the maxy property.
     * 
     */
    public double getMaxy() {
        return maxy;
    }

    /**
     * Gets the value of the resx property.
     */
    public Double getResx() {
        return resx;
    }

    /**
     */
    public Double getResy() {
        return resy;
    }

}
