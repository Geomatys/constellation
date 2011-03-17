/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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

package org.constellation.bean;

import java.util.List;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public interface MenuItem {

    public String getResourceBundlePath();

    public List<Path> getPaths();

    public static final class Path{

        public final Path parent;
        public final String i18nKey;
        public final String linkedPage;
        public final String icon;
        public final int priority;

        public Path(final Path parent, final String i18nKey, final String linkedPage, 
                final String icon, final int priority) {
            this.parent = parent;
            this.i18nKey = i18nKey;
            this.linkedPage = linkedPage;
            this.icon = icon;
            this.priority = priority;
        }

    }

}
