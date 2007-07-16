/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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
package net.sicade.observation.coverage.sql;

// J2SE dependencies
import java.util.Collections;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.rmi.RemoteException;

// Sicade dependencies
import net.sicade.observation.coverage.Layer;
import net.sicade.observation.coverage.DynamicCoverage;
import net.sicade.observation.CatalogException;
import net.sicade.observation.ServerException;
import net.sicade.observation.sql.CRS;
import net.sicade.observation.sql.Use;
import net.sicade.observation.sql.UsedBy;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.QueryType;
import net.sicade.observation.sql.ProcedureTable;
import net.sicade.observation.sql.BoundedSingletonTable;
import net.sicade.observation.coverage.rmi.DataConnection;
import net.sicade.observation.coverage.rmi.DataConnectionFactory;
import net.sicade.observation.sql.Column;
import net.sicade.observation.sql.Parameter;
import net.sicade.observation.sql.Role;
import static net.sicade.observation.sql.QueryType.*;


/**
 * Connexion vers la table des {@linkplain Layer couches}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo L'implémentation actuelle ne transmet pas l'enveloppe spatio-temporelle de {@code this}
 *       vers les objets {@link GridCoverageTable} créés. Il faudrait le faire, en prennant soin
 *       de transmettre cette informations aux objets RMI aussi. Dans la version actuelle, ce n'est
 *       pris en compte que pour les connections locales.
 *
 * @todo Bounding box disabled for now.
 */
@Use({ThematicTable.class, ProcedureTable.class, LinearModelTable.class, SeriesTable.class, GridCoverageTable.class})
@UsedBy(DescriptorTable.class)
public class LayerTable extends BoundedSingletonTable<Layer> {
    /**
     * Column name declared in the {@linkplain #query query}.
     */
    private final Column name, thematic, period, fallback, remarks;

    /**
     * Parameter declared in the {@linkplain #query query}.
     */
    private final Parameter byName;
    
//    private static final SpatialConfigurationKey LIST = new SpatialConfigurationKey("Layer:LIST",
//            "SELECT name, phenomenon, procedure, period, fallback, description\n"      +
//            "  FROM \"Layers\" "                                                       +
//            "  JOIN (\n"                                                               +
//            "   SELECT DISTINCT layer, visible FROM \"Series\"\n"                      +
//            "   JOIN \"GridCoverages\""         + " ON series=\"Series\".identifier\n" +
//            "   JOIN \"GridGeometries\""        + " ON extent=\"GridGeometries\".id\n" +
//            "   WHERE (  \"endTime\" IS NULL OR   \"endTime\" >= ?)\n"                 +
//            "     AND (\"startTime\" IS NULL OR \"startTime\" <= ?)\n"                 +
//            "     AND (\"eastBoundLongitude\">=? AND \"westBoundLongitude\"<=?)\n"     +
//            "     AND (\"northBoundLatitude\">=? AND \"southBoundLatitude\"<=?)\n"     +
//            "     AND (\"altitudeMax\"       >=? AND \"altitudeMin\"<=?)\n"            +
//            "  ) "                                                                     +
//            "  AS \"Selected\" ON layer=\"Layers\".name\n"                             +
//            "  WHERE visible=TRUE\n"                                                   +
//            "  ORDER BY name",
//
//            "SELECT name, phenomenon, procedure, period, fallback, description\n"      +
//            "  FROM \"Layers\" "                                                       +
//            "  JOIN (\n"                                                               +
//            "   SELECT DISTINCT layer, visible FROM \"Series\"\n"                      +
//            "   JOIN \"GridCoverages\""         + " ON series=\"Series\".identifier\n" +
//            "   JOIN \"GridGeometries\""        + " ON extent=\"GridGeometries\".id\n" +
//            "   WHERE (  \"endTime\" IS NULL OR   \"endTime\" >= ?)\n"                 +
//            "     AND (\"startTime\" IS NULL OR \"startTime\" <= ?)\n"                 +
//            "     AND (\"spatialExtent\" && ?)\n"     +
//            "  ) "                                                                     +
//            "  AS \"Selected\" ON layer=\"Layers\".name\n"                             +
//            "  WHERE visible=TRUE\n"                                                   +
//            "  ORDER BY name");

    /**
     * Connexion vers la table des thématiques.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    private ThematicTable thematics;

    /**
     * Connexion vers la table des modèles.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    private LinearModelTable models;

    /**
     * Connexion vers la table des séries.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    private SeriesTable series;

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
        public DataConnection connectSeries(final String layer) throws CatalogException, SQLException {
            final GridCoverageTable table = getDatabase().getTable(GridCoverageTable.class);
            table.setLayer(getEntry(layer));
            table.setEnvelope(getEnvelope());
            table.trimEnvelope();
            return table;
        }

        /**
         * Retourne la couverture de données pour le descripteur spécifié. Cette méthode
         * ne devrait jamais être exécutée, puisque cette classe n'est utilisée que par
         * {@link LayerTable#postCreateEntry} et que cette dernière n'utilise pas cette
         * méthode. Nous l'implémentons toujours par prudence.
         */
        public DynamicCoverage getDescriptorCoverage(final String descriptor) throws CatalogException, SQLException {
            return getDatabase().getTable(DescriptorTable.class).getEntryLenient(descriptor).getCoverage();
        }
    }

    /**
     * Construit une table qui interrogera la base de données spécifiée.
     *
     * @param database Connexion vers la base de données d'observations.
     */
    public LayerTable(final Database database) {
        super(database, CRS.XYT);
        final QueryType[] usage = {SELECT, LIST};
        name      = new Column   (query, "Layers", "name",        usage);
        thematic  = new Column   (query, "Layers", "thematic",    usage);
        period    = new Column   (query, "Layers", "period",      usage);
        fallback  = new Column   (query, "Layers", "fallback",    usage);
        remarks   = new Column   (query, "Layers", "description", usage);
        byName    = new Parameter(query, name, SELECT);
        name.setRole(Role.NAME);
    }

    /**
     * Construit une couche pour l'enregistrement courant.
     */
    protected Layer createEntry(final ResultSet results) throws CatalogException, SQLException {
        final String name      = results.getString(indexOf(this.name     ));
        final String thematic  = results.getString(indexOf(this.thematic ));
        double       period    = results.getDouble(indexOf(this.period   ));
        if (results.wasNull()) {
            period = Double.NaN;
        }
        final String fallback  = results.getString(indexOf(this.fallback));
        final String remarks   = results.getString(indexOf(this.remarks ));
        if (thematics == null) {
            thematics = getDatabase().getTable(ThematicTable.class);
        }
        /*
         * Utilise une table d'images distinctes pour chaque couches. La couche ne devrait plus
         * changer après la construction.  Pour cette raison, l'instance de GridCoverageTable
         * utilisée ici ne devra jamais être accessible publiquement.
         */
        final LayerEntry entry;
        entry = new LayerEntry(name, thematics.getEntry(thematic), null, period, remarks);
        entry.fallback = fallback;
        return entry;
    }

    /**
     * Complète la construction de la couche. Cette méthode construit les éléments suivants:
     * <p>
     * <ul>
     *   <li>La  {@linkplain Layer#getFallback couche de second recours}, s'il y en a une.</li>
     *   <li>Les {@linkplain Layer#getSeries séries}.</li>
     *   <li>La  {@linkplain LayerEntry#setDataConnection connexion aux données}.</li>
     *   <li>Le  {@linkplain LayerEntry#getModel modèle linéaire}.</li>
     * </ul>
     */
    @Override
    protected void postCreateEntry(final Layer layer) throws CatalogException, SQLException {
        super.postCreateEntry(layer);
        final LayerEntry entry = (LayerEntry) layer;
        if (series == null) {
            series = getDatabase().getTable(SeriesTable.Shareable.class);
            // We use a shareable instance because we will always use it in synchronized block.
        }
        synchronized (series) {
            series.setLayer(entry);
            entry.series = Collections.unmodifiableSet(series.getEntries());
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
         *    initialisé avec la couche construite par cet LayerTable.
         *
         * 2) Exécution sur un serveur distant:
         *    ---------------------------------
         *    Si un serveur est définit pour la propriété REGISTRY_NAME, un objet RMI sera obenu
         *    de ce serveur. Ce serveur construira un GridCoverageTable chez lui initialisé avec
         *    une copie de la couche construite par un LayerTable chez lui aussi.
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
                factory = (DataConnectionFactory) getDatabase().getRemote(DataConnectionFactory.REGISTRY_NAME);
            } catch (RemoteException exception) {
                throw new ServerException(exception);
            }
            if (factory == null) {
                factory = new Local();
            }
        }
        final DataConnection data;
        try {
            data = factory.connectSeries(layer.getName());
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
            final Database database = getDatabase();
            descriptors = database.createTable(DescriptorTable .class); descriptors.setLayerTable(this);
            models      = database.createTable(LinearModelTable.class); models.setDescriptorTable(descriptors);
        }
        entry.model = models.getEntry(entry);
    }
}
