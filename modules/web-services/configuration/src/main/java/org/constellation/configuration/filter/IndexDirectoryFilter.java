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
 * A file filter to retrieve all the index directory in a specified directory.
 *
 * @author Guilhem Legal
 */
public class IndexDirectoryFilter implements FilenameFilter {

        private String prefix;

        public IndexDirectoryFilter(String id) {
            prefix = "";
            if (id != null)
                prefix = id;
        }

        public boolean accept(File dir, String name) {
            File f = new File(dir, name);
            return (name.endsWith(prefix + "index") && f.isDirectory());
        }

    }