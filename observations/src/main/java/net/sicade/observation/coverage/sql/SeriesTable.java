/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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
package net.sicade.observation.coverage.sql;

// J2SE dependencies
import java.util.Collections;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.rmi.RemoteException;

// Sicade dependencies
import net.sicade.observation.coverage.Series;
import net.sicade.observation.coverage.DynamicCoverage;
import net.sicade.observation.ConfigurationKey;
import net.sicade.observation.CatalogException;
import net.sicade.observation.ServerException;
import net.sicade.observation.sql.Use;
import net.sicade.observation.sql.UsedBy;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.QueryType;
import net.sicade.observation.sql.ProcedureTable;
import net.sicade.observation.sql.BoundedSingletonTable;
import net.sicade.observation.coverage.rmi.DataConnection;
import net.sicade.observation.coverage.rmi.DataConnectionFactory;


/**
 * Connexion vers la table des {@linkplain Series séries}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo L'implémentation actuelle ne transmet pas l'enveloppe spatio-temporelle de {@code this}
 *       vers les objets {@link GridCoverageTable} créés. Il faudrait le faire, en prennant soin
 *       de transmettre cette informations aux objets RMI aussi. Dans la version actuelle, ce n'est
 *       pris en compte que pour les connections locales.
 */
@Use({ThematicTable.class, ProcedureTable.class, LinearModelTable.class, SubSeriesTable.class, GridCoverageTable.class})
@UsedBy(DescriptorTable.class)
public class SeriesTable extends BoundedSingletonTable<Series> {
    /**
     * Requête SQL utilisée pour obtenir une série à partir de son nom.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("Series:SELECT",
            "SELECT name, phenomenon, procedure, period, fallback, description\n" +
            "  FROM \"Layers\"\n"                                                 +
            " WHERE name=?");

    /**
     * Requête SQL utilisée pour obtenir une série à partir de son nom.
     * @todo Adapter les colonnes de la requête à un objet postgresql.BOX3D concernant la recherche 
     *       des limites de l'enveloppe (table "GridGeometries"). Dans le cas d'une base JavaDB,
     *       utiliser la requête telle qu'actuellement, car l'objet BOX3D n'est pas reconnu.
     */
    private static final ConfigurationKey LIST = new ConfigurationKey("Series:LIST",
            "SELECT name, phenomenon, procedure, period, fallback, description\n"      +
            "  FROM \"Layers\" "                                                       +
            "  JOIN (\n"                                                               +
            "   SELECT DISTINCT layer, visible FROM \"Series\"\n"                      +
            "   JOIN \"GridCoverages\""         + " ON series=\"Series\".identifier\n" +
            "   JOIN \"GridGeometries\""        + " ON extent=\"GridGeometries\".id\n" +
            "   WHERE (  \"endTime\" IS NULL OR   \"endTime\" >= ?)\n"                 +
            "     AND (\"startTime\" IS NULL OR \"startTime\" <= ?)\n"                 +
            "     AND (\"eastBoundLongitude\">=? AND \"westBoundLongitude\"<=?)\n"     +
            "     AND (\"northBoundLatitude\">=? AND \"southBoundLatitude\"<=?)\n"     +
            "  ) "                                                                     +
            "  AS \"Selected\" ON layer=\"Layers\".name\n"                             +
            "  WHERE visible=TRUE\n"                                                   +
            "  ORDER BY name");

    /** Numéro de colonne. */ private static final int NAME      =  1;
    /** Numéro de colonne. */ private static final int THEMATIC  =  2;
    /** Numéro de colonne. */ private static final int PROCEDURE =  3;
    /** Numéro de colonne. */ private static final int PERIOD    =  4;
    /** Numéro de colonne. */ private static final int FALLBACK  =  5;
    /** Numéro de colonne. */ private static final int REMARKS   =  6;

    /**
     * Connexion vers la table des thématiques.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    private ThematicTable thematics;

    /**
     * Connexion vers la table des procédures.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    private ProcedureTable procedures;

    /**
     * Connexion vers la table des modèles.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    private LinearModelTable models;

    /**
     * Connexion vers la table des sous-séries.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    private SubSeriesTable subseries;

    /**
     * Connections vers une fabrique de {@link DataConnection}, qui peut être locale ou sur un
     * serveur distant.
     */
    private DataConnectionFactory factory;

    /**
     * Une fabrique de {@link DataConnection} locale.
     */
    private final class Local implements DataConnectionFactory {
        /**
         * {@inheritDoc}
         */
        public DataConnection connectSeries(final String series) throws CatalogException, SQLException {
            final GridCoverageTable table = database.getTable(GridCoverageTable.class);
            table.setSeries(getEntry(series));
            table.setEnvelope(getEnvelope());
            table.trimEnvelope();
            return table;
        }

        /**
         * Retourne la couverture de données pour le descripteur spécifié. Cette méthode
         * ne devrait jamais être exécutée, puisque cette classe n'est utilisée que par
         * {@link SeriesTable#postCreateEntry} et que cette dernière n'utilise pas cette
         * méthode. Nous l'implémentons toujours par prudence.
         */
        public DynamicCoverage getDescriptorCoverage(final String descriptor) throws CatalogException, SQLException {
            return database.getTable(DescriptorTable.class).getEntryLenient(descriptor).getCoverage();
        }
    }

    /**
     * Construit une table qui interrogera la base de données spécifiée.
     *
     * @param database  Connexion vers la base de données d'observations.
     */
    public SeriesTable(final Database database) {
        super(database, net.sicade.observation.sql.CRS.XYT);
    }

    /**
     * Retourne la requête SQL à utiliser pour obtenir les séries.
     */
    @Override
    protected String getQuery(final QueryType type) throws SQLException {
        switch (type) {
            case LIST:         return getProperty(LIST);
            case SELECT:       return getProperty(SELECT);
            case BOUNDING_BOX: return getProperty(GridCoverageTable.BOUNDING_BOX);
            default:           return super.getQuery(type);
        }
    }

    /**
     * Configure la requête spécifiée. Cette méthode est appelée automatiquement lorsque la table
     * a {@linkplain #fireStateChanged changé d'état}.
     */
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException {
        super.configure(type, statement);
        switch (type) {
            case BOUNDING_BOX: {
                statement.setString(GridCoverageTable.ARGUMENT_SERIES, "%");
                break;
            }
        }
    }

    /**
     * Construit une série pour l'enregistrement courant.
     */
    protected Series createEntry(final ResultSet results) throws CatalogException, SQLException {
        final String name      = results.getString(NAME);
        final String thematic  = results.getString(THEMATIC);
        final String procedure = results.getString(PROCEDURE);
        double       period    = results.getDouble(PERIOD); if (results.wasNull()) period=Double.NaN;
        final String fallback  = results.getString(FALLBACK);
        final String remarks   = results.getString(REMARKS);
        if (thematics == null) {
            thematics = database.getTable(ThematicTable.class);
        }
        if (procedures == null) {
            procedures = database.getTable(ProcedureTable.class);
        }
        /*
         * Utilise une table d'images distinctes pour chaque séries. La série ne devrait plus
         * changer après la construction.  Pour cette raison, l'instance de GridCoverageTable
         * utilisée ici ne devra jamais être accessible publiquement.
         */
        final SeriesEntry entry;
        entry = new SeriesEntry(name,
                                thematics .getEntry(thematic),
                                procedures.getEntry(procedure),
                                period, remarks);
        entry.fallback = fallback;
        return entry;
    }

    /**
     * Complète la construction de la série. Cette méthode construit les éléments suivants:
     * <p>
     * <ul>
     *   <li>La  {@linkplain Series#getFallback série de second recours}, s'il y en a une.</li>
     *   <li>Les {@linkplain Series#getSubSeries sous-séries}.</li>
     *   <li>La  {@linkplain SeriesEntry#setDataConnection connexion aux données}.</li>
     *   <li>Le  {@linkplain SeriesEntry#getModel modèle linéaire}.</li>
     * </ul>
     */
    @Override
    protected void postCreateEntry(final Series series) throws CatalogException, SQLException {
        super.postCreateEntry(series);
        final SeriesEntry entry = (SeriesEntry) series;
        if (subseries == null) {
            subseries = thematics.getTable(SubSeriesTable.class);
        }
        synchronized (subseries) {
            subseries.setSeries(entry);
            entry.subseries = Collections.unmodifiableSet(subseries.getEntries());
        }
        if (entry.fallback instanceof String) {
            entry.fallback = getEntry((String) entry.fallback);
        }
        /*
         * Etablit la connexion vers les données. Trois cas peuvent se produire ici:
         *
         * 1) Exécution locale:
         *    -----------------
         *    Si aucun serveur n'est définit pour la propriété REGISTRY_NAME. Dans ce cas, une
         *    instance privée (Local) sera utilisée, qui construira un GridCoverageTable local
         *    initialisé avec la série construite par cet SeriesTable.
         *
         * 2) Exécution sur un serveur distant:
         *    ---------------------------------
         *    Si un serveur est définit pour la propriété REGISTRY_NAME, un objet RMI sera obenu
         *    de ce serveur. Ce serveur construira un GridCoverageTable chez lui initialisé avec
         *    une copie de la série construite par un SeriesTable chez lui aussi.
         *
         * 3) Exécution comme serveur:
         *    ------------------------
         *    Si ce code est exécuté par net.sicade.observation.coverage.rmi.Server, alors
         *    DataConnectionFactory a déjà été exporté comme service RMI. On ne veut pas créer
         *    de nouvelle instance locale, mais réutiliser le service qui existe déjà. La
         *    méthode Database.getRemote aura été redéfinit par Server en ce sens.
         */
        if (factory == null) {
            try {
                factory = (DataConnectionFactory) database.getRemote(DataConnectionFactory.REGISTRY_NAME);
            } catch (RemoteException exception) {
                throw new ServerException(exception);
            }
            if (factory == null) {
                factory = new Local();
            }
        }
        final DataConnection data;
        try {
            data = factory.connectSeries(series.getName());
        } catch (RemoteException exception) {
            throw new ServerException(exception);
        }
        entry.setDataConnection(data);
        /*
         * Construit le modèle linéaire. Notez que l'on utilise 'createTable' plutôt que
         * 'getTable' car on ne veut pas partager cette instance de 'LinearModelTable',
         * à cause de notre appel à 'setDescriptorTable'.
         */
        if (models == null) {
            final DescriptorTable descriptors;
            descriptors = database.createTable(DescriptorTable .class); descriptors.setSeriesTable(this);
            models      = database.createTable(LinearModelTable.class); models.setDescriptorTable(descriptors);
        }
        entry.model = models.getEntry(entry);
    }
}
