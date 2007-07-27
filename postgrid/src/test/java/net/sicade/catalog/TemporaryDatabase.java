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
package net.sicade.catalog;

import java.io.File;
import java.io.IOException;


/**
 * A temporary Derby database to be created for testing purpose only.
 * If a previous database existed, it will be destroyed.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class TemporaryDatabase extends Database {
    /**
     * Creates a temporary database.
     */
    public TemporaryDatabase() throws IOException {
        setProperty(ConfigurationKey.DATABASE, "postgrid-test;restoreFrom=postgrid2");
    }

    /**
     * Returns always {@code null} if order to bypass user configuration.
     * The default configuration should be suitable for the Derby database.
     */
    @Override
    File getConfigurationFile(final boolean create) {
        return null;
    }
}
