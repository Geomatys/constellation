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
import org.geotoolkit.util.collection.UnmodifiableArrayList;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractMenuItem implements MenuItem {

    public static final Path SERVICES_PATH = new Path(null, "services", null, "org.constellation.icons.socket.png.mfRes",300);
    public static final Path PROVIDERS_PATH = new Path(null, "providers", null, "org.constellation.icons.nfs_unmount.png.mfRes",200);
    public static final Path SYSTEMS_PATH = new Path(null, "systems", null, "org.constellation.icons.utilities.png.mfRes",100);

    private final String bundlePath;
    private final List<Path> paths;

    public AbstractMenuItem(final String bundlePath, Path ... paths) {
        this.bundlePath = bundlePath;
        this.paths = UnmodifiableArrayList.wrap(paths);
    }

    @Override
    public String getResourceBundlePath() {
        return bundlePath;
    }

    @Override
    public List<Path> getPaths() {
        return paths;
    }

}
