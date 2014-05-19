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
package org.constellation.xacml.factory;

import org.constellation.xacml.XACMLException;


/**
 * Exception which comes during the use of factories, in the XACML process.
 *
 * @version $Id$
 * @author Cédric Briançon
 */
public class FactoryException extends XACMLException {
    /*
     * Serial version identifier.
     */
    private static final long serialVersionUID = 7421600909866689212L;

    /**
     * Creates an exception with no cause and no details message.
     */
    public FactoryException() {
        super();
    }

    /**
     * Creates an exception with the specified details message.
     *
     * @param message The detail message.
     */
    public FactoryException(final String message) {
        super(message);
    }

    /**
     * Creates an exception with the specified cause and no details message.
     *
     * @param cause The cause for this exception.
     */
    public FactoryException(final Exception cause) {
        super(cause);
    }

    /**
     * Creates an exception with the specified details message and cause.
     *
     * @param message The detail message.
     * @param cause The cause for this exception.
     */
    public FactoryException(final String message, final Exception cause) {
        super(message, cause);
    }
}
