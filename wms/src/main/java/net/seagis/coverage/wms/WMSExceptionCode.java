package net.seagis.coverage.wms;

import java.util.ArrayList;
import java.util.List;
import org.opengis.util.CodeList;


/**
 *
 * @author Guilhem Legal
 */
public class WMSExceptionCode extends CodeList<WMSExceptionCode> {
    /**
     * List of all enumerations of this type.
     * Must be declared before any enum declaration.
     */
    private static final List<WMSExceptionCode> VALUES = new ArrayList<WMSExceptionCode>(15);

    /**
     *
     */
    public static final WMSExceptionCode INVALID_FORMAT = new WMSExceptionCode("INVALID_FORMAT");

    /**
     *
     */
    public static final WMSExceptionCode CURRENT_UPDATE_SEQUENCE = new WMSExceptionCode("CURRENT_UPDATE_SEQUENCE");

    /**
     *
     */
    public static final WMSExceptionCode INVALID_UPDATE_SEQUENCE = new WMSExceptionCode("INVALID_UPDATE_SEQUENCE");

    /**
     *
     */
    public static final WMSExceptionCode MISSING_PARAMETER_VALUE = new WMSExceptionCode("MISSING_PARAMETER_VALUE");

    /**
     *
     */
    public static final WMSExceptionCode INVALID_PARAMETER_VALUE = new WMSExceptionCode("INVALID_PARAMETER_VALUE");

    /**
     *
     */
    public static final WMSExceptionCode OPERATION_NOT_SUPPORTED = new WMSExceptionCode("OPERATION_NOT_SUPPORTED");

    /**
     *
     */
    public static final WMSExceptionCode VERSION_NEGOTIATION_FAILED = new WMSExceptionCode("VERSION_NEGOTIATION_FAILED");

    /**
     *
     */
    public static final WMSExceptionCode NO_APPLICABLE_CODE = new WMSExceptionCode("NO_APPLICABLE_CODE");

    /**
     *
     */
    public static final WMSExceptionCode INVALID_CRS = new WMSExceptionCode("INVALID_CRS");

    /**
     *
     */
    public static final WMSExceptionCode LAYER_NOT_DEFINED = new WMSExceptionCode("LAYER_NOT_DEFINED");

    /**
     *
     */
    public static final WMSExceptionCode STYLE_NOT_DEFINED = new WMSExceptionCode("STYLE_NOT_DEFINED");

    /**
     *
     */
    public static final WMSExceptionCode LAYER_NOT_QUERYABLE = new WMSExceptionCode("LAYER_NOT_QUERYABLE");

    /**
     *
     */
    public static final WMSExceptionCode INVALID_POINT = new WMSExceptionCode("INVALID_POINT");

    /**
     *
     */
    public static final WMSExceptionCode MISSING_DIMENSION_VALUE = new WMSExceptionCode("MISSING_DIMENSION_VALUE");

    /**
     *
     */
    public static final WMSExceptionCode INVALID_DIMENSION_VALUE = new WMSExceptionCode("INVALID_DIMENSION_VALUE");

    /**
     * Constructs an enum with the given name. The new enum is
     * automatically added to the list returned by {@link #values}.
     *
     * @param name The enum name. This name must not be in use by an other enum of this type.
     */
    private WMSExceptionCode(final String name) {
        super(name, VALUES);

    }

    /**
     * Returns the list of <code>ExceptionCode</code>s.
     */
    public static WMSExceptionCode[] values() {
        synchronized (VALUES) {
            return VALUES.toArray(new WMSExceptionCode[VALUES.size()]);
        }
    }

    public WMSExceptionCode[] family() {
        return values();
    }

    /**
     * Returns the exception code that matches the given string, or returns a
     * new one if none match it.
     */
    public static WMSExceptionCode valueOf(String code) {
        return valueOf(WMSExceptionCode.class, code);
    }
}
