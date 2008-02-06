
package net.seagis.gml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * This type is deprecated for tuples with ordinate values that are numbers.
 * CoordinatesType is a text string, intended to be used to record an array of tuples or coordinates. 
 * While it is not possible to enforce the internal structure of the string through schema validation, some optional attributes have been provided in previous versions of GML to support a description of the internal structure. These attributes are deprecated. The attributes were intended to be used as follows:
 * Decimal	symbol used for a decimal point (default="." a stop or period)
 * cs        	symbol used to separate components within a tuple or coordinate string (default="," a comma)
 * ts        	symbol used to separate tuples or coordinate strings (default=" " a space)
 * Since it is based on the XML Schema string type, CoordinatesType may be used in the construction of tables of tuples or arrays of tuples, including ones that contain mixed text and numeric values.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CoordinatesType", propOrder = {
    "value"
})
public class CoordinatesType {

    @XmlValue
    private String value;
    @XmlAttribute
    private String cs;
    @XmlAttribute
    private String decimal;
    @XmlAttribute
    private String ts;

    /**
     * Gets the value of the value property.
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the value of the cs property.
     */
    public String getCs() {
        if (cs == null) {
            return ",";
        } else {
            return cs;
        }
    }

    /**
     * Gets the value of the decimal property.
     */
    public String getDecimal() {
        if (decimal == null) {
            return ".";
        } else {
            return decimal;
        }
    }

    /**
     * Gets the value of the ts property.
     */
    public String getTs() {
        if (ts == null) {
            return " ";
        } else {
            return ts;
        }
    }
}
