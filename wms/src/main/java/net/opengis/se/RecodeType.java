package net.opengis.se;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RecodeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RecodeType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/se}FunctionType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/se}LookupValue"/>
 *         &lt;element ref="{http://www.opengis.net/se}MapItem" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RecodeType", propOrder = {
    "lookupValue",
    "mapItem"
})
public class RecodeType extends FunctionType  {

    @XmlElement(name = "LookupValue", required = true)
    protected ParameterValueType lookupValue;
    @XmlElement(name = "MapItem", required = true)
    protected List<MapItemType> mapItem;

    /**
     * Gets the value of the lookupValue property.
     * 
     * @return
     *     possible object is
     *     {@link ParameterValueType }
     *     
     */
    public ParameterValueType getLookupValue() {
        return lookupValue;
    }

    /**
     * Sets the value of the lookupValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParameterValueType }
     *     
     */
    public void setLookupValue(ParameterValueType value) {
        this.lookupValue = value;
    }

    /**
     * Gets the value of the mapItem property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mapItem property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMapItem().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MapItemType }
     * 
     * 
     */
    public List<MapItemType> getMapItem() {
        if (mapItem == null) {
            mapItem = new ArrayList<MapItemType>();
        }
        return this.mapItem;
    }

}
