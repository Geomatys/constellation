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

// J2SE dependencies
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

// OpenGIS dependencies
import org.opengis.coverage.grid.Format;

// Geotools dependencies
import org.geotools.coverage.grid.io.GridFormatFactorySpi;


/**
 * A factory to create the format for a PostGrid database. It verifies if JAI and JAI-IO are reachable.
 *
 * @version $Id$
 * @author Cédric Briançon
 */
public class PostGridFormatFactorySpi implements GridFormatFactorySpi {    
    /**
     * Not used in this implementation.
     */
    public PostGridFormatFactorySpi() {}
      
    /**
     * The format is created if the needed classes in JAI and JAI Image IO are found.
     */
    public Format createFormat() {
        if (!isAvailable()) {
            throw new UnsupportedOperationException(
                    "The PostGrid plugin requires the JAI and JAI ImageI/O libraries.");
        }
        
        return new PostGridFormat();
    }
    
    /**
     * Verifies if the JAI and JAI-IO package are installed on your machine and reachables.
     *
     * @return True if the needed classes are found, false otherwise.
     */
    public boolean isAvailable() {
        boolean available = true;
        // verifies if these classes are found.
        try {
            Class.forName("javax.media.jai.JAI");
            Class.forName("com.sun.media.jai.operator.ImageReadDescriptor");
        } catch (ClassNotFoundException cnf) {
            available = false;
        }
        return available;
    }
    
    /**
     * Returns the implementation hints. The default implementation returns en
     * empty map.
     *
     * @return Empty Map.
     */
    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }

}
