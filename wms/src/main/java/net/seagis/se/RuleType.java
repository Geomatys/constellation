package net.seagis.se;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;
//import net.opengis.ogc.FilterType;


/**
 * <p>Java class for RuleType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RuleType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/se}Name" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/se}Description" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/se}LegendGraphic" minOccurs="0"/>
 *         &lt;choice minOccurs="0">
 *           &lt;element ref="{http://www.opengis.net/ogc}Filter"/>
 *           &lt;element ref="{http://www.opengis.net/se}ElseFilter"/>
 *         &lt;/choice>
 *         &lt;element ref="{http://www.opengis.net/se}MinScaleDenominator" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/se}MaxScaleDenominator" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/se}Symbolizer" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RuleType", propOrder = {
    "name",
    "description",
    "legendGraphic",
    "filter",
    "elseFilter",
    "minScaleDenominator",
    "maxScaleDenominator",
    "symbolizer"
})
public class RuleType {

    @XmlElement(name = "Name")
    protected String name;
    @XmlElement(name = "Description")
    protected DescriptionType description;
    @XmlElement(name = "LegendGraphic")
    protected LegendGraphicType legendGraphic;
    //@XmlElement(name = "Filter", namespace = "http://www.opengis.net/ogc")
    //protected FilterType filter;
    @XmlElement(name = "ElseFilter")
    protected ElseFilterType elseFilter;
    @XmlElement(name = "MinScaleDenominator")
    protected Double minScaleDenominator;
    @XmlElement(name = "MaxScaleDenominator")
    protected Double maxScaleDenominator;
    @XmlElementRef(name = "Symbolizer", namespace = "http://www.opengis.net/se", type = JAXBElement.class)
    protected List<JAXBElement<? extends SymbolizerType>> symbolizer;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link DescriptionType }
     *     
     */
    public DescriptionType getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link DescriptionType }
     *     
     */
    public void setDescription(DescriptionType value) {
        this.description = value;
    }

    /**
     * Gets the value of the legendGraphic property.
     * 
     * @return
     *     possible object is
     *     {@link LegendGraphicType }
     *     
     */
    public LegendGraphicType getLegendGraphic() {
        return legendGraphic;
    }

    /**
     * Sets the value of the legendGraphic property.
     * 
     * @param value
     *     allowed object is
     *     {@link LegendGraphicType }
     *     
     */
    public void setLegendGraphic(LegendGraphicType value) {
        this.legendGraphic = value;
    }

    /**
     * Gets the value of the filter property.
     * 
     * @return
     *     possible object is
     *     {@link FilterType }
     *     
     
    public FilterType getFilter() {
        return filter;
    }

    /**
     * Sets the value of the filter property.
     * 
     * @param value
     *     allowed object is
     *     {@link FilterType }
     *     
     
    public void setFilter(FilterType value) {
        this.filter = value;
    }*/

    /**
     * Gets the value of the elseFilter property.
     * 
     * @return
     *     possible object is
     *     {@link ElseFilterType }
     *     
     */
    public ElseFilterType getElseFilter() {
        return elseFilter;
    }

    /**
     * Sets the value of the elseFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link ElseFilterType }
     *     
     */
    public void setElseFilter(ElseFilterType value) {
        this.elseFilter = value;
    }

    /**
     * Gets the value of the minScaleDenominator property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getMinScaleDenominator() {
        return minScaleDenominator;
    }

    /**
     * Sets the value of the minScaleDenominator property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setMinScaleDenominator(Double value) {
        this.minScaleDenominator = value;
    }

    /**
     * Gets the value of the maxScaleDenominator property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getMaxScaleDenominator() {
        return maxScaleDenominator;
    }

    /**
     * Sets the value of the maxScaleDenominator property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setMaxScaleDenominator(Double value) {
        this.maxScaleDenominator = value;
    }

    /**
     * Gets the value of the symbolizer property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the symbolizer property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSymbolizer().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link PolygonSymbolizerType }{@code >}
     * {@link JAXBElement }{@code <}{@link SymbolizerType }{@code >}
     * {@link JAXBElement }{@code <}{@link RasterSymbolizerType }{@code >}
     * {@link JAXBElement }{@code <}{@link TextSymbolizerType }{@code >}
     * {@link JAXBElement }{@code <}{@link PointSymbolizerType }{@code >}
     * {@link JAXBElement }{@code <}{@link LineSymbolizerType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<? extends SymbolizerType>> getSymbolizer() {
        if (symbolizer == null) {
            symbolizer = new ArrayList<JAXBElement<? extends SymbolizerType>>();
        }
        return this.symbolizer;
    }

}
