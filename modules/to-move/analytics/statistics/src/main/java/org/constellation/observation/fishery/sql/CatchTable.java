/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
