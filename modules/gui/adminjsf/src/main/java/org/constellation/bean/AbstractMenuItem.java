/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.bean;

import java.util.List;
import org.geotoolkit.util.collection.UnmodifiableArrayList;

/**
 *
 * @author jsorel
 */
public abstract class AbstractMenuItem implements MenuItem {

    public static final Path SERVICES_PATH = new Path(null, "services", null, "org.constellation.icons.socket.png.mfRes");
    public static final Path PROVIDERS_PATH = new Path(null, "providers", null, "org.constellation.icons.nfs_unmount.png.mfRes");
    public static final Path SYSTEMS_PATH = new Path(null, "systems", null, "org.constellation.icons.utilities.png.mfRes");

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
