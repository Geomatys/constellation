/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2006, Institut de Recherche pour le Développement
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
package org.constellation.observation;

// J2SE dependencies
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// Sicade dependencies
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.ServerException;
import org.constellation.coverage.catalog.*;
import org.constellation.observation.MeasurementTable;


/**
 * Séries d'instructions {@code INSERT} envoyées à la base de données par {@link MeasurementTableFiller}.
 * Exécutées dans un thread séparé pour ne pas bloquer les calculs des valeurs pendant que le logiciel de
 * base de données procède à l'insertion des valeurs.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class MeasurementInserts extends Thread {
    /**
     * La table des mesures à modifier.
     */
    private final MeasurementTable measures;

    /**
     * Liste des stations à insérer.
     */
    private final BlockingQueue<SamplingFeatureDescriptorPair> values = new LinkedBlockingQueue<SamplingFeatureDescriptorPair>();
    
    /**
     * Valeur sentinelle indiquant que les insertions sont terminées.
     */
    private final SamplingFeatureDescriptorPair finish = new SamplingFeatureDescriptorPair(null, null);

    /**
     * Si non-null, alors l'erreur qui vient de se produire.
     */
    private volatile CatalogException exception;

    /**
     * Construit une liste initialement vide.
     */
    public MeasurementInserts(final MeasurementTable measures) {
        this.measures = measures;
    }

    /**
     * Ajoute la valeur spécifiée à la liste des valeurs à insérer.
     */
    public void add(final SamplingFeatureDescriptorPair value) throws CatalogException {
        assert isAlive();
        final CatalogException e = exception;
        if (e != null) {
            throw e;
        }
        values.add(value);
    }

    /**
     * Indique qu'il n'y a plus d'autres valeurs à insérer. Cette méthode bloquera
     * jusqu'à ce que toutes les valeurs restantes aient été insérées.
     */
    public void finished() throws CatalogException {
        assert isAlive();
        values.add(finish);
        try {
            join();
        } catch (InterruptedException exception) {
            throw new ServerException(exception);
        }
        final CatalogException e = exception;
        if (e != null) {
            throw e;
        }
    }

    /**
     * Insére toutes les valeurs dans la queue. Si une exception se produit lors de l'insertion
     * d'une valeur, alors cette méthode s'arrêtera.
     */
    @Override
    public void run() {
        do {
            final SamplingFeatureDescriptorPair pair;
            try {
                pair = values.take();
            } catch (InterruptedException cause) {
                exception = new ServerException(cause);
                break;
            }
            if (pair == finish) {
                break;
            }
            //measures.setObservable(pair.descriptor);  // TODO
            //measures.setStation   (pair.samplingFeature);
            /*try {
                measures.setValue(pair.value, Float.NaN); //TODO
            } catch (CatalogException cause) {
                exception = cause;
                break;
            } catch (SQLException cause) {
                exception = new ServerException(cause);
                break;
            }*/
        } while (exception == null);
    }
}
