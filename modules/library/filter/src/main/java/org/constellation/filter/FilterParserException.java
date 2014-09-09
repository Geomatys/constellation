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
package org.constellation.filter;

import org.geotoolkit.ows.xml.OWSExceptionCode;
import org.opengis.util.CodeList;


/**
 * Reports a failure in a {@link WebService}.
 *
 * @version $Id$
 *
 * @author Guihlem Legal
 * @author Cédric Briançon
 */
public class FilterParserException extends Exception {
    private static final long serialVersionUID = 1156609900372258061L;

    /**
     * The exception code.
     */
    private final CodeList exceptionCode;

    /**
     * The reason of the exception.
     */
    private final String locator;

    /**
     * Creates an exception with the specified details message.
     *
     * @param message The detail message.
     */
    public FilterParserException(final String message) {
        this(message, OWSExceptionCode.NO_APPLICABLE_CODE);
    }

    /**
     * Creates an exception with the specified details message and the exceptionCode chosen.
     *
     * @param message The detail message.
     * @param exceptionCode The exception code.
     */
    public FilterParserException(final String message, final CodeList exceptionCode)
    {
        this(message, exceptionCode, null);
    }

    /**
     * Creates an exception with the specified details message, exception code and locator value.
     *
     * @param message The detail message.
     * @param exceptionCode The exception code.
     * @param locator What causes the exception.
     */
    public FilterParserException(final String message, final CodeList exceptionCode, final String locator) {
        this(message, null, exceptionCode, locator);
    }

    /**
     * Creates an exception with the specified exception cause.
     *
     * @param cause The cause of this exception.
     */
    public FilterParserException(final Exception cause) {
        this(cause, OWSExceptionCode.NO_APPLICABLE_CODE);
    }

    /**
     * Creates an exception with the specified exception cause and code.
     *
     * @param cause The cause of this exception.
     * @param exceptionCode The exception code.
     */
    public FilterParserException(final Exception cause, final CodeList exceptionCode) {
        this(cause, exceptionCode, null);
    }

    /**
     * Creates an exception with the specified exception cause and code, and locator value.
     *
     * @param cause The cause of this exception.
     * @param exceptionCode The exception code.
     * @param locator What causes the exception.
     */
    public FilterParserException(final Exception cause, final CodeList exceptionCode, final String locator) {
        this(cause.getMessage(), cause, exceptionCode, locator);
    }

    /**
     * Creates an exception with the specified details message and cause.
     *
     * @param message The detail message.
     * @param cause The cause for this exception.
     * @param exceptionCode The exception code.
     */
    public FilterParserException(final String message, final Exception cause, final CodeList exceptionCode) {
        this(message, cause, exceptionCode, null);
    }

    /**
     * Creates an exception with the specified exception cause and code, and locator value.
     *
     * @param message The detail message.
     * @param cause The cause of this exception.
     * @param exceptionCode The exception code.
     * @param locator What causes the exception.
     */
    public FilterParserException(final String message, final Exception cause, final CodeList exceptionCode,
                                final String locator)
    {
        super(message, cause);
        this.exceptionCode = exceptionCode;
        this.locator = locator;
    }

    public CodeList getExceptionCode() {
        return exceptionCode;
    }

    public String getLocator() {
        return locator;
    }
}
