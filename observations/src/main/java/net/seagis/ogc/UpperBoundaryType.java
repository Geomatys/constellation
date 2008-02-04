package net.seagis.ogc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import net.seagis.ogc.ExpressionType;


/**
 * <p>Java class for UpperBoundaryType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UpperBoundaryType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element ref="{http://www.opengis.net/ogc}expression"/>
 *         &lt;element ref="{http://www.opengis.net/ogc}Literal"/>
 *         &lt;element ref="{http://www.opengis.net/ogc}PropertyName"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UpperBoundaryType", propOrder = {
    "expression",
    "literal",
    "propertyName"
})
public class UpperBoundaryType {

    @XmlElement(nillable = true)
    protected ExpressionType expression;
    @XmlElement(name = "Literal", nillable = true)
    protected LiteralType literal;
    @XmlElement(name = "PropertyName", nillable = true)
    protected String propertyName;

    /**
     * Gets the value of the expression property.
     * 
     * @return
     *     possible object is
     *     {@link ExpressionType }
     *     
     */
    public ExpressionType getExpression() {
        return expression;
    }

    /**
     * Sets the value of the expression property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExpressionType }
     *     
     */
    public void setExpression(ExpressionType value) {
        this.expression = value;
    }

    /**
     * Gets the value of the literal property.
     * 
     * @return
     *     possible object is
     *     {@link LiteralType }
     *     
     */
    public LiteralType getLiteral() {
        return literal;
    }

    /**
     * Sets the value of the literal property.
     * 
     * @param value
     *     allowed object is
     *     {@link LiteralType }
     *     
     */
    public void setLiteral(LiteralType value) {
        this.literal = value;
    }

    /**
     * Gets the value of the propertyName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Sets the value of the propertyName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPropertyName(String value) {
        this.propertyName = value;
    }

}
