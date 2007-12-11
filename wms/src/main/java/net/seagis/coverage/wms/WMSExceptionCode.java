/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.seagis.coverage.wms;

import java.util.ArrayList;
import java.util.List;
import org.opengis.util.CodeList;


/**
 * Describes the type of an exception.
 *
 * @author Guilhem Legal
 * @author Martin Desruisseaux
 *
 * @todo Rename as {@code ExceptionCode} and move to {@link org.opengis.webservice}.
 */
public class WMSExceptionCode extends CodeList<WMSExceptionCode> {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 7234996844680200818L;

    /**
     * List of all enumerations of this type.
     * Must be declared before any enum declaration.
     */
    private static final List<WMSExceptionCode> VALUES = new ArrayList<WMSExceptionCode>(15);

    /**
     * Invalid format.
     */
    public static final WMSExceptionCode INVALID_FORMAT = new WMSExceptionCode("INVALID_FORMAT");

    /**
     * Current update sequence.
     */
    public static final WMSExceptionCode CURRENT_UPDATE_SEQUENCE = new WMSExceptionCode("CURRENT_UPDATE_SEQUENCE");

    /**
     * Invalid update sequence.
     */
    public static final WMSExceptionCode INVALID_UPDATE_SEQUENCE = new WMSExceptionCode("INVALID_UPDATE_SEQUENCE");

    /**
     * Missing parameter value.
     */
    public static final WMSExceptionCode MISSING_PARAMETER_VALUE = new WMSExceptionCode("MISSING_PARAMETER_VALUE");

    /**
     * Invalid parameter value.
     */
    public static final WMSExceptionCode INVALID_PARAMETER_VALUE = new WMSExceptionCode("INVALID_PARAMETER_VALUE");

    /**
     * Operation not supported.
     */
    public static final WMSExceptionCode OPERATION_NOT_SUPPORTED = new WMSExceptionCode("OPERATION_NOT_SUPPORTED");

    /**
     * Version negotiation failed.
     */
    public static final WMSExceptionCode VERSION_NEGOTIATION_FAILED = new WMSExceptionCode("VERSION_NEGOTIATION_FAILED");

    /**
     * No applicable code.
     */
    public static final WMSExceptionCode NO_APPLICABLE_CODE = new WMSExceptionCode("NO_APPLICABLE_CODE");

    /**
     * Invalid CRS.
     */
    public static final WMSExceptionCode INVALID_CRS = new WMSExceptionCode("INVALID_CRS");

    /**
     * Layer not defined.
     */
    public static final WMSExceptionCode LAYER_NOT_DEFINED = new WMSExceptionCode("LAYER_NOT_DEFINED");

    /**
     * Style not defined.
     */
    public static final WMSExceptionCode STYLE_NOT_DEFINED = new WMSExceptionCode("STYLE_NOT_DEFINED");

    /**
     * Layer not queryable.
     */
    public static final WMSExceptionCode LAYER_NOT_QUERYABLE = new WMSExceptionCode("LAYER_NOT_QUERYABLE");

    /**
     * Invalid point.
     */
    public static final WMSExceptionCode INVALID_POINT = new WMSExceptionCode("INVALID_POINT");

    /**
     * Missing dimension value.
     */
    public static final WMSExceptionCode MISSING_DIMENSION_VALUE = new WMSExceptionCode("MISSING_DIMENSION_VALUE");

    /**
     * Invalid dimension value.
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
     * Returns the list of exception codes.
     */
    public static WMSExceptionCode[] values() {
        synchronized (VALUES) {
            return VALUES.toArray(new WMSExceptionCode[VALUES.size()]);
        }
    }

    /**
     * Returns the list of exception codes.
     */
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
