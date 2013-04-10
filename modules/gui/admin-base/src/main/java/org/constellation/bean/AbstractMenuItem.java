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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.constellation.admin.service.ConstellationServer;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.collection.UnmodifiableArrayList;
import org.geotoolkit.util.logging.Logging;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractMenuItem implements MenuItem {

    protected static final Logger LOGGER = Logging.getLogger(AbstractMenuItem.class);
    
    public static final Path SERVICES_PATH       = new Path(null, "services", "/base/services.xhtml", "org.constellation.icons.socket.png.mfRes",400);
    public static final Path PROVIDERS_PATH      = new Path(null, "providers", "/base/providers.xhtml", "org.constellation.icons.nfs_unmount.png.mfRes",300);
    //public static final Path STYLE_PATH          = new Path(null, "styles", null, "org.constellation.icons.colors.png.mfRes",250);
    public static final Path SYSTEMS_PATH        = new Path(null, "systems", null, "org.constellation.icons.configure.png.mfRes",200);
    public static final Path TOOLS_PATH          = new Path(null, "tools", null, "org.constellation.icons.tool.png.mfRes",100);

    private final List<String> pages;
    private final String bundlePath;
    private final Path path;

    public AbstractMenuItem(final String[] pages, final String bundlePath, final Path path) {
        ArgumentChecks.ensureNonNull("pages", pages);
        ArgumentChecks.ensureNonNull("path", path);
        this.pages      = UnmodifiableArrayList.wrap(pages);
        this.bundlePath = bundlePath;
        this.path       = path;
    }

    @Override
    public boolean isAvailable(final ConstellationServer server) {
        return true;
    }
    
    protected boolean serviceAvailable(final ConstellationServer server, final String serviceName) {
        if (server != null) {
            final Map<String, List<String>> availableService = server.services.getAvailableService();
            if (availableService.containsKey(serviceName)) {
                return true;
            } else {
                LOGGER.log(Level.INFO, "remote server does not embbed a {0} server", serviceName);
            }
        }
        return false;
    }
    
    @Override
    public String getId() {
        return path.i18nKey;
    }
    
    @Override
    public String getTitle() {
        return path.i18nKey;
    }

    @Override
    public List<String> getPages() {
        return pages;
    }

    @Override
    public String getResourceBundlePath() {
        return bundlePath;
    }

    @Override
    public Path getPath() {
        return path;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[").append(this.getClass().getSimpleName()).append("]");
        sb.append("pages:\n");
        for (String page : pages) {
            sb.append(page).append('\n');
        }
        sb.append("bundle path:").append(bundlePath).append('\n');
        if (path != null) {
            sb.append("path:").append(path).append('\n');
        }
        return sb.toString();
    }

}
