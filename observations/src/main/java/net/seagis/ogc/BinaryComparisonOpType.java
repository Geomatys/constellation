
package net.seagis.ogc;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import net.seagis.ogc.ExpressionType;


/**
 * <p>Java class for BinaryComparisonOpType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BinaryComparisonOpType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/ogc}ComparisonOpsType">
 *       &lt;sequence>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.opengis.net/ogc}expression"/>
 *           &lt;element ref="{http://www.opengis.net/ogc}Literal"/>
 *           &lt;element ref="{http://www.opengis.net/ogc}PropertyName"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="matchCase" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BinaryComparisonOpType", propOrder = {
    "expressionOrLiteralOrPropertyName"
})
public class BinaryComparisonOpType
    extends ComparisonOpsType
{

    @XmlElements({
        @XmlElement(name = "expression", type = ExpressionType.class, nillable = true),
        @XmlElement(name = "PropertyName", type = String.class, nillable = true),
        @XmlElement(name = "Literal", type = LiteralType.class, nillable = true)
    })
    protected List<Object> expressionOrLiteralOrPropertyName;
    @XmlAttribute
    protected Boolean matchCase;

    /**
     * Gets the value of the expressionOrLiteralOrPropertyName property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the expressionOrLiteralOrPropertyName property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExpressionOrLiteralOrPropertyName().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ExpressionType }
     * {@link String }
     * {@link LiteralType }
     * 
     * 
     */
    public List<Object> getExpressionOrLiteralOrPropertyName() {
        if (expressionOrLiteralOrPropertyName == null) {
            expressionOrLiteralOrPropertyName = new ArrayList<Object>();
        }
        return this.expressionOrLiteralOrPropertyName;
    }

    /**
     * Gets the value of the matchCase property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isMatchCase() {
        return matchCase;
    }

    /**
     * Sets the value of the matchCase property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMatchCase(Boolean value) {
        this.matchCase = value;
    }

}
