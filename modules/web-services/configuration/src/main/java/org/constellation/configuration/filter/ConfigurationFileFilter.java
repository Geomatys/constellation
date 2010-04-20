/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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

package org.constellation.configuration.filter;

import java.io.File;
import java.io.FilenameFilter;

/**
 * A filter to retrieve configuration files in the specified configuration directory.
 * 
 * @author Guilhem Legal
 */
public class ConfigurationFileFilter implements FilenameFilter {

    private String prefix;

    public ConfigurationFileFilter(String id) {
        prefix = "";
        if (id != null) {
            prefix = id;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(File dir, String name) {
        return name.endsWith(prefix + "config.xml");
    }
}
