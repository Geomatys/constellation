package net.seagis.se;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import net.seagis.ogc.ExpressionType;


/**
 * <p>Java class for FunctionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FunctionType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/ogc}ExpressionType">
 *       &lt;attribute name="fallbackValue" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FunctionType")
/*@XmlSeeAlso({
    CategorizeType.class,
    TrimType.class,
    InterpolateType.class,
    RecodeType.class,
    StringPositionType.class,
    SubstringType.class,
    ConcatenateType.class,
    StringLengthType.class,
    FormatDateType.class,
    ChangeCaseType.class,
    FormatNumberType.class
})*/
public abstract class FunctionType
    extends ExpressionType
{

    @XmlAttribute(required = true)
    protected String fallbackValue;

    /**
     * Gets the value of the fallbackValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFallbackValue() {
        return fallbackValue;
    }

    /**
     * Sets the value of the fallbackValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFallbackValue(String value) {
        this.fallbackValue = value;
    }

}
