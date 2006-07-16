/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 2006, Institut de Recherche pour le D�veloppement
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sicade.observation.coverage;

// J2SE dependencies
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// Sicade dependencies
import net.sicade.observation.ServerException;
import net.sicade.observation.CatalogException;
import net.sicade.observation.sql.MeasurementTable;


/**
 * S�ries d'instructions {@code INSERT} envoy�es � la base de donn�es par {@link MeasurementTableFiller}.
 * Ex�cut�es dans un thread s�par� pour ne pas bloquer les calculs des valeurs pendant que le logiciel de
 * base de donn�es proc�de � l'insertion des valeurs.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class MeasurementInserts extends Thread {
    /**
     * La table des mesures � modifier.
     */
    private final MeasurementTable measures;

    /**
     * Liste des stations � ins�rer.
     */
    private final BlockingQueue<StationDescriptorPair> values = new LinkedBlockingQueue<StationDescriptorPair>();
    
    /**
     * Valeur sentinelle indiquant que les insertions sont termin�es.
     */
    private final StationDescriptorPair finish = new StationDescriptorPair(null, null);

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
     * Ajoute la valeur sp�cifi�e � la liste des valeurs � ins�rer.
     */
    public void add(final StationDescriptorPair value) throws CatalogException {
        assert isAlive();
        final CatalogException e = exception;
        if (e != null) {
            throw e;
        }
        values.add(value);
    }

    /**
     * Indique qu'il n'y a plus d'autres valeurs � ins�rer. Cette m�thode bloquera
     * jusqu'� ce que toutes les valeurs restantes aient �t� ins�r�es.
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
     * Ins�re toutes les valeurs dans la queue. Si une exception se produit lors de l'insertion
     * d'une valeur, alors cette m�thode s'arr�tera.
     */
    @Override
    public void run() {
        do {
            final StationDescriptorPair pair;
            try {
                pair = values.take();
            } catch (InterruptedException cause) {
                exception = new ServerException(cause);
                break;
            }
            if (pair == finish) {
                break;
            }
            measures.setObservable(pair.descriptor);
            measures.setStation   (pair.station);
            try {
                measures.setValue(pair.value, Float.NaN);
            } catch (CatalogException cause) {
                exception = cause;
                break;
            } catch (SQLException cause) {
                exception = new ServerException(cause);
                break;
            }
        } while (exception == null);
    }
}
