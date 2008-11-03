/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.observation.fishery.sql;

import org.constellation.catalog.ConfigurationKey;
import org.constellation.catalog.Database;
import org.constellation.observation.fishery.Catch;
import org.constellation.observation.MeasurementTable;


/**
 * Connexion vers la table des {@linkplain Catch captures}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo Retourner une instance de {@link Catch}.
 */
@Deprecated
public class CatchTable extends MeasurementTable {
  
    /**
     * Construit une nouvelle connexion vers la table des captures.
     */
    public CatchTable(final Database database) {
        super(database);
    }
}
