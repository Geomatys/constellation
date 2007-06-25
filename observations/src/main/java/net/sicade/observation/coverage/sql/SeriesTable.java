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
 * Connexion vers la table des {@linkplain Series s�ries}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo L'impl�mentation actuelle ne transmet pas l'enveloppe spatio-temporelle de {@code this}
 *       vers les objets {@link GridCoverageTable} cr��s. Il faudrait le faire, en prennant soin
 *       de transmettre cette informations aux objets RMI aussi. Dans la version actuelle, ce n'est
 *       pris en compte que pour les connections locales.
 */
@Use({ThematicTable.class, ProcedureTable.class, LinearModelTable.class, SubSeriesTable.class, GridCoverageTable.class})
@UsedBy(DescriptorTable.class)
public class SeriesTable extends BoundedSingletonTable<Series> {
    /**
     * Requ�te SQL utilis�e pour obtenir une s�rie � partir de son nom.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("Series:SELECT",
            "SELECT name, phenomenon, procedure, period, fallback, description\n" +
            "  FROM \"Layers\"\n"                                                 +
            " WHERE name=?");

    /**
     * Requ�te SQL utilis�e pour obtenir une s�rie � partir de son nom.
     * @todo Adapter les colonnes de la requ�te � un objet postgresql.BOX3D concernant la recherche 
     *       des limites de l'enveloppe (table "GridGeometries"). Dans le cas d'une base JavaDB,
     *       utiliser la requ�te telle qu'actuellement, car l'objet BOX3D n'est pas reconnu.
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

    /** Num�ro de colonne. */ private static final int NAME      =  1;
    /** Num�ro de colonne. */ private static final int THEMATIC  =  2;
    /** Num�ro de colonne. */ private static final int PROCEDURE =  3;
    /** Num�ro de colonne. */ private static final int PERIOD    =  4;
    /** Num�ro de colonne. */ private static final int FALLBACK  =  5;
    /** Num�ro de colonne. */ private static final int REMARKS   =  6;

    /**
     * Connexion vers la table des th�matiques.
     * Une connexion (potentiellement partag�e) sera �tablie la premi�re fois o� elle sera n�cessaire.
     */
    private ThematicTable thematics;

    /**
     * Connexion vers la table des proc�dures.
     * Une connexion (potentiellement partag�e) sera �tablie la premi�re fois o� elle sera n�cessaire.
     */
    private ProcedureTable procedures;

    /**
     * Connexion vers la table des mod�les.
     * Une connexion (potentiellement partag�e) sera �tablie la premi�re fois o� elle sera n�cessaire.
     */
    private LinearModelTable models;

    /**
     * Connexion vers la table des sous-s�ries.
     * Une connexion (potentiellement partag�e) sera �tablie la premi�re fois o� elle sera n�cessaire.
     */
    private SubSeriesTable subseries;

    /**
     * Connections vers une fabrique de {@link DataConnection}, qui peut �tre locale ou sur un
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
         * Retourne la couverture de donn�es pour le descripteur sp�cifi�. Cette m�thode
         * ne devrait jamais �tre ex�cut�e, puisque cette classe n'est utilis�e que par
         * {@link SeriesTable#postCreateEntry} et que cette derni�re n'utilise pas cette
         * m�thode. Nous l'impl�mentons toujours par prudence.
         */
        public DynamicCoverage getDescriptorCoverage(final String descriptor) throws CatalogException, SQLException {
            return database.getTable(DescriptorTable.class).getEntryLenient(descriptor).getCoverage();
        }
    }

    /**
     * Construit une table qui interrogera la base de donn�es sp�cifi�e.
     *
     * @param database  Connexion vers la base de donn�es d'observations.
     */
    public SeriesTable(final Database database) {
        super(database, net.sicade.observation.sql.CRS.XYT);
    }

    /**
     * Retourne la requ�te SQL � utiliser pour obtenir les s�ries.
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
     * Configure la requ�te sp�cifi�e. Cette m�thode est appel�e automatiquement lorsque la table
     * a {@linkplain #fireStateChanged chang� d'�tat}.
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
     * Construit une s�rie pour l'enregistrement courant.
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
         * Utilise une table d'images distinctes pour chaque s�ries. La s�rie ne devrait plus
         * changer apr�s la construction.  Pour cette raison, l'instance de GridCoverageTable
         * utilis�e ici ne devra jamais �tre accessible publiquement.
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
     * Compl�te la construction de la s�rie. Cette m�thode construit les �l�ments suivants:
     * <p>
     * <ul>
     *   <li>La  {@linkplain Series#getFallback s�rie de second recours}, s'il y en a une.</li>
     *   <li>Les {@linkplain Series#getSubSeries sous-s�ries}.</li>
     *   <li>La  {@linkplain SeriesEntry#setDataConnection connexion aux donn�es}.</li>
     *   <li>Le  {@linkplain SeriesEntry#getModel mod�le lin�aire}.</li>
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
         * Etablit la connexion vers les donn�es. Trois cas peuvent se produire ici:
         *
         * 1) Ex�cution locale:
         *    -----------------
         *    Si aucun serveur n'est d�finit pour la propri�t� REGISTRY_NAME. Dans ce cas, une
         *    instance priv�e (Local) sera utilis�e, qui construira un GridCoverageTable local
         *    initialis� avec la s�rie construite par cet SeriesTable.
         *
         * 2) Ex�cution sur un serveur distant:
         *    ---------------------------------
         *    Si un serveur est d�finit pour la propri�t� REGISTRY_NAME, un objet RMI sera obenu
         *    de ce serveur. Ce serveur construira un GridCoverageTable chez lui initialis� avec
         *    une copie de la s�rie construite par un SeriesTable chez lui aussi.
         *
         * 3) Ex�cution comme serveur:
         *    ------------------------
         *    Si ce code est ex�cut� par net.sicade.observation.coverage.rmi.Server, alors
         *    DataConnectionFactory a d�j� �t� export� comme service RMI. On ne veut pas cr�er
         *    de nouvelle instance locale, mais r�utiliser le service qui existe d�j�. La
         *    m�thode Database.getRemote aura �t� red�finit par Server en ce sens.
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
         * Construit le mod�le lin�aire. Notez que l'on utilise 'createTable' plut�t que
         * 'getTable' car on ne veut pas partager cette instance de 'LinearModelTable',
         * � cause de notre appel � 'setDescriptorTable'.
         */
        if (models == null) {
            final DescriptorTable descriptors;
            descriptors = database.createTable(DescriptorTable .class); descriptors.setSeriesTable(this);
            models      = database.createTable(LinearModelTable.class); models.setDescriptorTable(descriptors);
        }
        entry.model = models.getEntry(entry);
    }
}
