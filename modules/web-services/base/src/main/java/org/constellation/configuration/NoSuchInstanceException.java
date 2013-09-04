/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.constellation.configuration;

import org.constellation.ServiceDef.Specification;

/**
 * @author Bernard Fabien (Geomatys)
 * @since 0.9
 */
public final class NoSuchInstanceException extends ConfigurationException {

    public NoSuchInstanceException(final String message) {
        super(message);
    }

    public static NoSuchInstanceException noConfigDirectory(final Specification specification, final String identifier) {
        return new NoSuchInstanceException(specification + " service instance with \"" + identifier +
                "\" not found. The service instance directory does not exist.");
    }

    public static NoSuchInstanceException noConfigFile(final Specification specification, final String identifier) {
        return new NoSuchInstanceException(specification + " service instance with \"" + identifier +
                "\" not found. The service instance directory exists but there is not configuration file inside.");
    }
}
