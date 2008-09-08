/*
 *    Constellation - An open source and standard compliant SDI
 *    http://constellation.codehaus.org
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
package net.seagis.observation.fishery.sql;

import net.seagis.catalog.ConfigurationKey;
import net.seagis.catalog.Database;
import net.seagis.observation.fishery.Catch;
import net.seagis.observation.MeasurementTable;


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
