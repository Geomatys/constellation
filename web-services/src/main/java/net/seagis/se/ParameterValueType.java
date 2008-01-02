package net.seagis.se;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
//import net.opengis.ogc.BinaryOperatorType;
//import net.opengis.ogc.ExpressionType;
//import net.opengis.ogc.LiteralType;
//import net.opengis.ogc.PropertyNameType;


/**
 * 
 *         The "ParameterValueType" uses WFS-Filter expressions to give
 *         values for SE graphic parameters.  A "mixed" element-content
 *         model is used with textual substitution for values.
 *       
 * 
 * <p>Java class for ParameterValueType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ParameterValueType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{http://www.opengis.net/ogc}expression"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParameterValueType", propOrder = {
    "content"
})
@XmlSeeAlso({
    SvgParameterType.class
})
public class ParameterValueType {

    @XmlElementRef(name = "expression", namespace = "http://www.opengis.net/ogc", type = JAXBElement.class)
    @XmlMixed
    protected List<Serializable> content;

    /**
     * 
     *         The "ParameterValueType" uses WFS-Filter expressions to give
     *         values for SE graphic parameters.  A "mixed" element-content
     *         model is used with textual substitution for values.
     *       Gets the value of the content property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the content property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link net.opengis.ogc.FunctionType }{@code >}
     * {@link JAXBElement }{@code <}{@link ExpressionType }{@code >}
     * {@link JAXBElement }{@code <}{@link LiteralType }{@code >}
     * {@link JAXBElement }{@code <}{@link CategorizeType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinaryOperatorType }{@code >}
     * {@link JAXBElement }{@code <}{@link SubstringType }{@code >}
     * {@link JAXBElement }{@code <}{@link PropertyNameType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinaryOperatorType }{@code >}
     * {@link JAXBElement }{@code <}{@link FormatNumberType }{@code >}
     * {@link JAXBElement }{@code <}{@link net.opengis.se.FunctionType }{@code >}
     * {@link JAXBElement }{@code <}{@link TrimType }{@code >}
     * {@link JAXBElement }{@code <}{@link InterpolationPointType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinaryOperatorType }{@code >}
     * {@link JAXBElement }{@code <}{@link StringPositionType }{@code >}
     * {@link JAXBElement }{@code <}{@link StringLengthType }{@code >}
     * {@link JAXBElement }{@code <}{@link RecodeType }{@code >}
     * {@link JAXBElement }{@code <}{@link BinaryOperatorType }{@code >}
     * {@link JAXBElement }{@code <}{@link ChangeCaseType }{@code >}
     * {@link JAXBElement }{@code <}{@link FormatDateType }{@code >}
     * {@link JAXBElement }{@code <}{@link ConcatenateType }{@code >}
     * {@link JAXBElement }{@code <}{@link MapItemType }{@code >}
     * {@link String }
     * {@link JAXBElement }{@code <}{@link InterpolateType }{@code >}
     * 
     * 
     */
    public List<Serializable> getContent() {
        if (content == null) {
            content = new ArrayList<Serializable>();
        }
        return this.content;
    }

}
