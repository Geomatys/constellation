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
package net.seagis.coverage.web;

import java.io.File;
import java.util.Map;
import java.util.Iterator;
import java.util.LinkedHashMap;


/**
 * Files creates for an image request. This map is automatically registered for cleaning
 * at shutdown time. Invoke {@link #dispose()} for unregistering it.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@SuppressWarnings("serial")
final class ImageRequestMap extends LinkedHashMap<ImageRequest,File> implements Runnable {
    /**
     * The maximal amount of files to be allowed.
     */
    private static final int MAXIMUM_FILES = 100;

    /**
     * The shutdown hook.
     */
    private transient Thread hook;

    /**
     * Creates an initially empty map.
     */
    public ImageRequestMap() {
        super(MAXIMUM_FILES + MAXIMUM_FILES/4, 0.75f, true);
        hook = new Thread(this, "Delete temporary WCS files");
        Runtime.getRuntime().addShutdownHook(hook);
    }

    /**
     * Removes the eldest entry.
     */
    @Override
    protected boolean removeEldestEntry(final Map.Entry<ImageRequest,File> eldest) {
        if (size() > MAXIMUM_FILES) {
            eldest.getValue().delete();
            return true;
        }
        return false;
    }

    /**
     * Deletes every file. Deleted files are removed from the map.
     * The files that can't be deleted remain.
     */
    public void run() {
        for (final Iterator<File> it=values().iterator(); it.hasNext();) {
            if (it.next().delete()) {
                it.remove();
            }
        }
    }

    /**
     * Disposes this map and unregister the shutdown hook.
     */
    public void dispose() {
        if (hook != null) {
            Runtime.getRuntime().removeShutdownHook(hook);
            hook = null;
        }
        run();
    }
}
