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
import org.constellation.admin.service.ConstellationServer;

/**
 * 
 * @author Johann Sorel (Geomatys)
 */
public interface MenuItem {

    /**
     * 
     * @param server : Server to test
     * @return true if this menu item is available for the given server.
     */
    public boolean isAvailable(ConstellationServer server);
    
    /**
     * List of string path to xhtml pages that need to be copied in the web application.
     * 
     * @return list never null
     */
    public List<String> getPages();

    /**
     *
     * @return path to the resource bundle used for pages and path translation.
     */
    public String getResourceBundlePath();

    /**
     * List of Path which will be added in the administration menu.
     * @return never null.
     */
    public List<Path> getPaths();

    /**
     * A Path describe a menu item displayed in the administration menu.
     */
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
