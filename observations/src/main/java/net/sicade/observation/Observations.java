/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 2005, Institut de Recherche pour le D�veloppement
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
package net.sicade.observation;

// J2SE dependencies
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.sql.SQLException;
import java.rmi.RemoteException;

// OpenGIS dependencies
import org.opengis.coverage.Coverage;
import org.opengis.metadata.extent.GeographicBoundingBox;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.util.WeakValueHashMap;

// Sicade dependencies
import net.sicade.util.DateRange;
import net.sicade.observation.sql.Database;
import net.sicade.observation.coverage.Model;
import net.sicade.observation.coverage.Series;
import net.sicade.observation.coverage.Descriptor;
import net.sicade.observation.coverage.DynamicCoverage;
import net.sicade.observation.coverage.sql.SeriesTable;
import net.sicade.observation.coverage.sql.DescriptorTable;
import net.sicade.observation.coverage.rmi.DataConnectionFactory;


/**
 * M�thodes de commodit� permettant d'obtenir des observations.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Observations {
    /**
     * Une instance de {@code Observations} connect�e � la base de donn�es par d�faut. Cette
     * base de donn�es est habituellement d�clar�e dans un fichier {@code DatabaseQueries.xml}
     * situ� dans le r�pertoire de l'utilisateur.
     */
    private static Observations DEFAULT;

    /**
     * La connexion vers la base de donn�es. Si sa valeur est {@code null}, alors une connexion
     * par d�faut ne sera �tablie que la premi�re fois o� elle sera n�cessaire.
     */
    private Database database;

    /**
     * Connections vers les table des s�ries pour diff�rentes enveloppes spatio-temporelles.
     * Chaque connexion ne sera construite que la premi�re fois o� elle sera n�cessaire. La
     * valeur associ�e � la cl� {@code null} sera la table de toutes les s�ries sans restriction.
     */
    private final Map<Envelope,SeriesTable> series = new HashMap<Envelope,SeriesTable>();

    /**
     * L'ensemble des couvertures de donn�es obtenues par {@link #getCoverage}.
     */
    @SuppressWarnings("unchecked")
    private final Map<String,DynamicCoverage> coverages = new WeakValueHashMap();

    /**
     * Construit une instance de {@code Observations} pour la base de donn�es sp�cifi�e.
     *
     * @param database Connexion � la base de donn�es, ou {@code null} pour utiliser la
     *        base de donn�es par d�faut.
     */
    public Observations(final Database database) {
        this.database = database;
    }

    /**
     * Une instance de {@code Observations} connect�e � la base de donn�es par d�faut. Cette
     * base de donn�es est habituellement d�clar�e dans un fichier {@code DatabaseQueries.xml}
     * situ� dans le r�pertoire de l'utilisateur.
     */
    public static synchronized Observations getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new Observations(null);
        }
        return DEFAULT;
    }

    /**
     * Retourne une connexion � la base de donn�es.
     *
     * @return La connexion � la base de donn�es (jamais nulle).
     * @param  CatalogException si la base de donn�es n'a pas pu �tre obtenue.
     */
    public synchronized Database getDatabase() throws CatalogException {
        if (database == null) try {
            database = new Database();
        } catch (IOException exception) {
            throw new ServerException(exception);
        }
        return database;
    }

    /**
     * Retourne la table des descripteurs.
     *
     * @param  CatalogException si la table n'a pas pu �tre obtenue.
     *
     * @todo Envisager de cacher le r�sultat. Et aussi de fournir la possibilit� de
     *       r�duire la r�gion g�ographique.
     */
    private DescriptorTable getDescriptorTable() throws CatalogException {
        return getDatabase().getTable(DescriptorTable.class);
    }

    /**
     * Retourne la table des s�ries pour la r�gion spatio-temporelle sp�cifi�e.
     *
     * @param  area La r�gion g�ographique des s�ries d�sir�es, ou {@code null} pour ne placer aucune
     *         restriction g�ographique.
     * @param  timeRange La plage de temps des s�ries d�sir�es, ou {@code null} pour ne placer aucune
     *         restriction temporelle.
     * @return La table des s�ries interceptant la r�gion g�ographique et la plage de temps sp�cifi�es.
     * @throws SQLException si une erreur est survenue lors de l'interrogation de la base de donn�es.
     */
    private SeriesTable getSeriesTable(final GeographicBoundingBox area, final DateRange timeRange)
            throws CatalogException, SQLException
    {
        final Envelope envelope;
        if (area!=null || timeRange!=null) {
            envelope = new Envelope(area, timeRange);
        } else {
            envelope = null;
        }
        SeriesTable table = series.get(envelope);
        if (table == null) {
            table = getDatabase().getTable(SeriesTable.class);
            if (area != null) {
                table.setGeographicBoundingBox(area);
            }
            if (timeRange != null) {
                table.setTimeRange(timeRange);
            }
            series.put(envelope, table);
        }
        return table;
    }

    /**
     * Retourne l'ensemble des s�ries disponibles dans la base de donn�es. Si une r�gion g�ographique
     * ou une plage de temps sont sp�cifi�es, alors seules les s�ries interceptant ces r�gions seront
     * retourn�es.
     *
     * @param  area La r�gion g�ographique des s�ries d�sir�es, ou {@code null} pour ne placer aucune
     *         restriction g�ographique.
     * @param  timeRange La plage de temps des s�ries d�sir�es, ou {@code null} pour ne placer aucune
     *         restriction temporelle.
     * @return L'ensemble des s�ries interceptant la r�gion g�ographique et la plage de temps sp�cifi�es.
     * @throws CatalogException si une erreur est survenue lors de l'interrogation du catalogue.
     */
    public synchronized Set<Series> getSeries(final GeographicBoundingBox area,
                                              final DateRange        timeRange)
            throws CatalogException
    {
        try {
            return getSeriesTable(area, timeRange).getEntries();
        } catch (SQLException exception) {
            throw new ServerException(exception);
        }
    }

    /**
     * Retourne la s�ries de donn�es du nom sp�cifi� dans la r�gion spatio-temporelle sp�cifi�e.
     *
     * @param  area La r�gion g�ographique des s�ries d�sir�es, ou {@code null} pour ne placer aucune
     *         restriction g�ographique.
     * @param  timeRange La plage de temps des s�ries d�sir�es, ou {@code null} pour ne placer aucune
     *         restriction temporelle.
     * @param  name Nom de la s�rie d�sir�e.
     * @return Une s�rie de nom sp�cifi�.
     * @throws NoSuchRecordException si aucune s�rie n'a �t� trouv�e pour le nom sp�cifi�.
     * @throws CatalogException si une erreur est survenue lors de l'interrogation du catalogue.
     */
    public synchronized Series getSeries(final GeographicBoundingBox area,
                                         final DateRange        timeRange,
                                         final String                name)
            throws CatalogException
    {
        try {
            return getSeriesTable(area, timeRange).getEntry(name);
        } catch (SQLException exception) {
            throw new ServerException(exception);
        }
    }

    /**
     * Retourne la s�ries de donn�es du nom sp�cifi�.
     *
     * @param  name Nom de la s�rie d�sir�e.
     * @return Une s�rie de nom sp�cifi�.
     * @throws NoSuchRecordException si aucune s�rie n'a �t� trouv�e pour le nom sp�cifi�.
     * @throws CatalogException si une erreur est survenue lors de l'interrogation du catalogue.
     */
    public synchronized Series getSeries(final String name) throws CatalogException {
        try {
            return getSeriesTable(null, null).getEntry(name);
        } catch (SQLException exception) {
            throw new ServerException(exception);
        }
    }

    /**
     * Retourne les donn�es pour un descripteur du nom sp�cifi�.
     *
     * @param  name Le nom du {@linkplain Descriptor descripteur}.
     * @return La converture des donn�es pour le descripteur sp�cifi�.
     * @throws NoSuchRecordException si aucun descripteur n'a �t� trouv�e pour le nom sp�cifi�.
     * @throws CatalogException si une erreur est survenue lors de l'interrogation du catalogue.
     *
     * @todo Faire en sorte que le boulot soit enti�rement fait du c�t� du serveur RMI (sans
     *       qu'il ne soit n�ssaire de faire une connexion � la base de donn�es ici).
     */
    public synchronized DynamicCoverage getDescriptorCoverage(final String name)
            throws CatalogException
    {
        DynamicCoverage coverage = coverages.get(name);
        if (coverage == null) try {
            final Database database = getDatabase();
            final DataConnectionFactory factory = (DataConnectionFactory)
                    database.getRemote(DataConnectionFactory.REGISTRY_NAME);
            if (factory != null) {
                coverage = factory.getDescriptorCoverage(name);
            } else {
                coverage = getDescriptorTable().getEntryLenient(name).getCoverage();
            }
            coverages.put(name, coverage);
        } catch (RemoteException exception) {
            throw new ServerException(exception);
        } catch (SQLException exception) {
            throw new ServerException(exception);
        }
        return coverage;
    }

    /**
     * Retourne les donn�es pour un mod�le du nom sp�cifi�.
     *
     * @param  name Le nom du {@linkplain Descriptor descripteur} ou de la {@linkplain Series s�rie}.
     * @return Le mod�le pour le descripteur ou la s�rie sp�cifi�, ou {@code null} si la s�rie n'a pas de mod�le.
     * @throws NoSuchRecordException si aucun descripteur ou s�rie n'a �t� trouv�e pour le nom sp�cifi�.
     * @throws CatalogException si une erreur est survenue lors de l'interrogation du catalogue.
     *
     * @todo Faire en sorte que le boulot soit enti�rement fait du c�t� du serveur RMI (sans
     *       qu'il ne soit n�ssaire de faire une connexion � la base de donn�es ici).
     */
    public synchronized Coverage getModelCoverage(final String name) throws CatalogException {
        Series series;
        final Database database = getDatabase();
        try {
            series = getSeries(name);
        } catch (NoSuchRecordException ignore) {
            try {
                series = getDescriptorTable().getEntryLenient(name).getPhenomenon();
            } catch (SQLException exception) {
                throw new ServerException(exception);
            }
        }
        final Model model = series.getModel();
        return (model != null) ? model.asCoverage() : null;
    }

    /**
     * Retourne l'ensemble des descripteurs dans la base de donn�es.
     *
     * @throws CatalogException si une erreur est survenue lors de l'interrogation du catalogue.
     */
    public synchronized Set<Descriptor> getDescriptors() throws CatalogException {
        try {
            return getDescriptorTable().getEntries();
        } catch (SQLException exception) {
            throw new ServerException(exception);
        }
    }

    /**
     * Une enveloppe repr�sent�e par une {@linkplain GeographicBoundingBox r�gion g�ographique} et
     * une {@linkplain DateRange plage de temps}. Cette classe sert uniquement de cl�s pour la cache
     * des {@linkplain SeriesTable tables des s�ries}.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    private static final class Envelope {
        /**
         * La r�gion g�ographique des s�ries d�sir�es, ou {@code null} pour ne placer aucune
         * restriction g�ographique.
         */
        private final GeographicBoundingBox bbox;

        /**
         * La plage de temps des s�ries d�sir�es, ou {@code null} pour ne placer aucune
         * restriction temporelle.
         */
        private final DateRange timeRange;

        /**
         * Construit une enveloppe pour les limites spatio-temporelles sp�cifi�es.
         */
        public Envelope(final GeographicBoundingBox bbox, final DateRange timeRange) {
            this.bbox      = bbox;
            this.timeRange = timeRange;
        }

        /**
         * Retourne un code � peu pr�s unique pour cette enveloppe.
         */
        @Override
        public int hashCode() {
            int code = 0;
            if (bbox != null) {
                code = bbox.hashCode();
            }
            if (timeRange != null) {
                code ^= timeRange.hashCode();
            }
            return code;
        }

        /**
         * Compare cette enveloppe avec l'objet sp�cifi�.
         */
        @Override
        public boolean equals(final Object object) {
            if (object instanceof Envelope) {
                final Envelope that = (Envelope) object;
                return Utilities.equals(this.bbox,      that.bbox) &&
                       Utilities.equals(this.timeRange, that.timeRange);
            }
            return false;
        }
    }
}
