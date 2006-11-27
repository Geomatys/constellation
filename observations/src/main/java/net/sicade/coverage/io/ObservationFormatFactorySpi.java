/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2006, Geomatys
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
import org.geotools.data.coverage.grid.GridFormatFactorySpi;

// Sicade dependencies
import net.sicade.observation.CatalogException;
import net.sicade.observation.Observations;


/**
 * Provider of {@link ObservationFormat} instances.
 *
 * @version $Id$
 * @author Cédric Briançon
 */
public class ObservationFormatFactorySpi implements GridFormatFactorySpi {  
    /**
     * Creates a default provider.
     */
    public ObservationFormatFactorySpi() {
    }
      
    /**
     * The format is created if the needed classes in JAI and JAI Image IO are found.
     */
    public Format createFormat() {
        if (!isAvailable()) {
            throw new UnsupportedOperationException(
                    "The Observations plugin requires a database connection.");
        }
        return new ObservationFormat();
    }
    
    /**
     * Checks if the {@linkplain ObservationFormat observation format is available}.
     * The default implementation checks if a SQL connection is available.
     */
    public boolean isAvailable() {
        try {
            return Observations.getDefault().getDatabase() != null;
        } catch (CatalogException e) {
            return false;
        }
    }
    
    /**
     * Returns the implementation hints. The default implementation returns en
     * empty map.
     */
    public Map getImplementationHints() {
        return Collections.EMPTY_MAP;
    }
}
