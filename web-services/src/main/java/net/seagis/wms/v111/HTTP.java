package net.seagis.wms.v111;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import net.seagis.wms.AbstractHTTP;


/**
 * <p>Java class for anonymous complex type.
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "get",
    "post"
})
@XmlRootElement(name = "HTTP")
public class HTTP extends AbstractHTTP {

    @XmlElement(name = "Get", required = true)
    private Get get;
    @XmlElement(name = "Post")
    private Post post;

    /**
     * An empty constructor used by JAXB.
     */
     HTTP() {
     }

    /**
     * Build a new HTTP object.
     */
    public HTTP(final Get get, final Post post) {
        this.get  = get;
        this.post = post;
    }
    
    
    /**
     * Gets the value of the get property.
     * 
     */
    public Get getGet() {
        return get;
    }

    /**
     * Gets the value of the post property.
     * 
     */
    public Post getPost() {
        return post;
    }
}
