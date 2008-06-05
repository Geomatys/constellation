package net.seagis.wms.v111;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "name",
    "title",
    "_abstract",
    "legendURL",
    "styleSheetURL",
    "styleURL"
})
@XmlRootElement(name = "Style")
public class Style {

    @XmlElement(name = "Name", required = true)
    private String name;
    @XmlElement(name = "Title", required = true)
    private String title;
    @XmlElement(name = "Abstract")
    private String _abstract;
    @XmlElement(name = "LegendURL")
    private List<LegendURL> legendURL = new ArrayList<LegendURL>();
    @XmlElement(name = "StyleSheetURL")
    private StyleSheetURL styleSheetURL;
    @XmlElement(name = "StyleURL")
    private StyleURL styleURL;

    /**
     * An empty constructor used by JAXB.
     */
     Style() {
     }

    /**
     * Build a new Contact person primary object.
     */
    public Style(final String name, final String title, final String _abstract, 
            final StyleURL styleURL, final StyleSheetURL styleSheetURL,final LegendURL... legendURLs) {
        
        this._abstract     = _abstract;
        this.name          = name;
        this.styleSheetURL = styleSheetURL;
        this.styleURL      = styleURL;
        this.title         = title;
        for (final LegendURL element : legendURLs) {
            this.legendURL.add(element);
        }
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
     * Gets the value of the legendURL property.
     * 
     */
    public List<LegendURL> getLegendURL() {
        return Collections.unmodifiableList(legendURL);
    }

    /**
     * Gets the value of the styleSheetURL property.
     */
    public StyleSheetURL getStyleSheetURL() {
        return styleSheetURL;
    }

    /**
     * Gets the value of the styleURL property.
     * 
     */
    public StyleURL getStyleURL() {
        return styleURL;
    }
}
