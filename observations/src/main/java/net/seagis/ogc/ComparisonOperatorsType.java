//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.0 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.09.13 at 04:28:10 PM CEST 
//


package net.seagis.ogc;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ComparisonOperatorsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ComparisonOperatorsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence maxOccurs="unbounded">
 *         &lt;element name="ComparisonOperator" type="{http://www.opengis.net/ogc}ComparisonOperatorType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ComparisonOperatorsType", propOrder = {
    "comparisonOperator"
})
public class ComparisonOperatorsType {

    @XmlElement(name = "ComparisonOperator", required = true)
    protected List<ComparisonOperatorType> comparisonOperator;

    /**
     * Gets the value of the comparisonOperator property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the comparisonOperator property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getComparisonOperator().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ComparisonOperatorType }
     * 
     * 
     */
    public List<ComparisonOperatorType> getComparisonOperator() {
        if (comparisonOperator == null) {
            comparisonOperator = new ArrayList<ComparisonOperatorType>();
        }
        return this.comparisonOperator;
    }

}
