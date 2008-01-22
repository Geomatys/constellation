/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.seagis.coverage.web;

import java.io.File;
import java.util.Map;
import java.util.LinkedHashMap;


/**
 * Files creates for an image request.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@SuppressWarnings("serial")
final class ImageRequestMap extends LinkedHashMap<ImageRequest,File> {
    /**
     * The maximal amount of files to be allowed.
     */
    private static final int MAXIMUM_FILES = 100;

    /**
     * Creates an initially empty map.
     */
    public ImageRequestMap() {
        super(MAXIMUM_FILES + MAXIMUM_FILES/4, 0.75f, true);
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
}
