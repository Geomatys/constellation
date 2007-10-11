
package net.sicade.sos;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for responseModeType.
 * 
 */
@XmlEnum
public enum ResponseMode {

    @XmlEnumValue("inline")
    INLINE("inline"),
    @XmlEnumValue("attached")
    ATTACHED("attached"),
    @XmlEnumValue("out-of-band")
    OUT_OF_BAND("out-of-band"),
    @XmlEnumValue("resultTemplate")
    RESULT_TEMPLATE("resultTemplate");
    private final String value;

    ResponseMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ResponseMode fromValue(String v) {
        for (ResponseMode c: ResponseMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}
