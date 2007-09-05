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
package net.sicade.coverage.io;

import java.util.Collections;
import java.util.Map;

import org.opengis.coverage.grid.Format;
import org.geotools.coverage.grid.io.GridFormatFactorySpi;


/**
 * A factory to create the format for a PostGrid database.
 *
 * @version $Id$
 * @author Cédric Briançon
 */
public class PostGridFormatFactorySpi implements GridFormatFactorySpi {
    /**
     * Default constructor.
     */
    public PostGridFormatFactorySpi() {
    }

    /**
     * The format is created if the needed classes in JAI and JAI Image IO are found.
     */
    public Format createFormat() {
        return new PostGridFormat("coriolis");
    }

    /**
     * Returns {@code true} in all cases.
     */
    public boolean isAvailable() {
        return true;
    }

    /**
     * Returns the implementation hints. The default implementation returns an empty map.
     */
    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }
}
