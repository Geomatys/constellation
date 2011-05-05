/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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

/**
 *
 * @author Guilhem Legal
 */
public class ConfigurationException extends Exception {
    private static final long serialVersionUID = 6771141662229835127L;
    
    private final String message;
    
    private final String cause;
    
    public ConfigurationException(String message) {
        super(message);
        this.message = message;
        this.cause   = "";
    }
    
    public ConfigurationException(String message, String cause) {
        super(message);
        this.message = message;
        this.cause   = cause;
    }
    
    @Override
    public String toString() {
        return message + '\n' + "Cause: " + cause;
    }

}
