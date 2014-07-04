/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.ws;

import org.opengis.util.CodeList;

import java.util.ArrayList;
import java.util.List;


/**
 * Describes the type of an exception.
 *
 * @author Guilhem Legal
 * @author Martin Desruisseaux
 * @author Cédric Briançon
 *
 * @todo Rename as {@code ExceptionCode} and move to {@link org.opengis.webservice}.
 */
public final class ExceptionCode extends CodeList<ExceptionCode> {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 7234996844680200818L;

    /**
     * List of all enumerations of this type.
     * Must be declared before any enum declaration.
     */
    private static final List<ExceptionCode> VALUES = new ArrayList<ExceptionCode>(16);

    /**
     * Invalid format.
     * Used by WMS and WCS.
     */
    public static final ExceptionCode INVALID_FORMAT = new ExceptionCode("INVALID_FORMAT");
    
    /**
     * Invalid request.
     * Used by WMS and WCS.
     */
    public static final ExceptionCode INVALID_REQUEST = new ExceptionCode("INVALID_REQUEST");

    /**
     * Current update sequence.
     * Used by WMS and WCS.
     */
    public static final ExceptionCode CURRENT_UPDATE_SEQUENCE = new ExceptionCode("CURRENT_UPDATE_SEQUENCE");

    /**
     * Invalid update sequence.
     * Used by WMS and WCS.
     */
    public static final ExceptionCode INVALID_UPDATE_SEQUENCE = new ExceptionCode("INVALID_UPDATE_SEQUENCE");

    /**
     * Missing parameter value.
     * Used by WMS and WCS.
     */
    public static final ExceptionCode MISSING_PARAMETER_VALUE = new ExceptionCode("MISSING_PARAMETER_VALUE");

    /**
     * Invalid parameter value.
     * Used by WMS and WCS.
     */
    public static final ExceptionCode INVALID_PARAMETER_VALUE = new ExceptionCode("INVALID_PARAMETER_VALUE");

    /**
     * Operation not supported.
     * Used by WMS.
     */
    public static final ExceptionCode OPERATION_NOT_SUPPORTED = new ExceptionCode("OPERATION_NOT_SUPPORTED");

    /**
     * Version negotiation failed.
     * Used by WMS.
     */
    public static final ExceptionCode VERSION_NEGOTIATION_FAILED = new ExceptionCode("VERSION_NEGOTIATION_FAILED");

    /**
     * No applicable code.
     * Used by WMS.
     */
    public static final ExceptionCode NO_APPLICABLE_CODE = new ExceptionCode("NO_APPLICABLE_CODE");

    /**
     * Invalid CRS.
     * Used by WMS.
     */
    public static final ExceptionCode INVALID_CRS = new ExceptionCode("INVALID_CRS");

    /**
     * Invalid CRS.
     * Used by WMS 1.1.1
     */
    public static final ExceptionCode INVALID_SRS = new ExceptionCode("InvalidSRS");

    /**
     * Layer not defined.
     * Used by WMS.
     */
    public static final ExceptionCode LAYER_NOT_DEFINED = new ExceptionCode("LAYER_NOT_DEFINED");

    /**
     * Style not defined.
     * Used by WMS.
     */
    public static final ExceptionCode STYLE_NOT_DEFINED = new ExceptionCode("STYLE_NOT_DEFINED");

    /**
     * Layer not queryable.
     * Used by WMS.
     */
    public static final ExceptionCode LAYER_NOT_QUERYABLE = new ExceptionCode("LAYER_NOT_QUERYABLE");

    /**
     * Invalid point.
     * Used by WMS.
     */
    public static final ExceptionCode INVALID_POINT = new ExceptionCode("INVALID_POINT");

    /**
     * Missing dimension value.
     * Used by WMS.
     */
    public static final ExceptionCode MISSING_DIMENSION_VALUE = new ExceptionCode("MISSING_DIMENSION_VALUE");

    /**
     * Invalid dimension value.
     * Used by WMS.
     */
    public static final ExceptionCode INVALID_DIMENSION_VALUE = new ExceptionCode("INVALID_DIMENSION_VALUE");

    /**
     * Constructs an enum with the given name. The new enum is
     * automatically added to the list returned by {@link #values}.
     *
     * @param name The enum name. This name must not be in use by an other enum of this type.
     */
    private ExceptionCode(final String name) {
        super(name, VALUES);
    }

    /**
     * Returns the list of exception codes.
     *
     * @return The list of codes declared in the current JVM.
     */
    public static ExceptionCode[] values() {
        synchronized (VALUES) {
            return VALUES.toArray(new ExceptionCode[VALUES.size()]);
        }
    }

    /**
     * Returns the list of exception codes.
     */
    @Override
    public ExceptionCode[] family() {
        return values();
    }

    /**
     * Returns the exception code that matches the given string, or returns a
     * new one if none match it.
     *
     * @param code The name of the code to fetch or to create.
     * @return A code matching the given name.
     */
    public static ExceptionCode valueOf(String code) {
        return valueOf(ExceptionCode.class, code);
    }
}
