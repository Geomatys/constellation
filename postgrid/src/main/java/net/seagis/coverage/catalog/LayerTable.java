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
package net.seagis.coverage.catalog;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.rmi.RemoteException;

import net.seagis.catalog.CRS;
import net.seagis.catalog.Database;
import net.seagis.catalog.BoundedSingletonTable;
import net.seagis.catalog.CatalogException;
import net.seagis.catalog.ServerException;
import net.seagis.coverage.model.DescriptorTable;
import net.seagis.coverage.model.LinearModelTable;


/**
 * Connection to a table of {@linkplain Layer layers}.
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
public class LayerTable extends BoundedSingletonTable<Layer> {
    /**
     * Connexion vers la table des modèles.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    private LinearModelTable models;

    /**
     * Connection to the table of series. A new instance will be
     * created when first needed for each layer table.
     */
    // Note: a previous version was used to share this instance, but we had to
    // remove this sharing because it produces wrong values for Series.layer.
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
        public DataConnection connectLayer(final String layer) throws CatalogException, SQLException {
            final GridCoverageTable table = new GridCoverageTable(getDatabase().getTable(GridCoverageTable.class));
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
        public GridCoverage getDescriptorCoverage(final String descriptor) throws CatalogException, SQLException {
            return getDatabase().getTable(DescriptorTable.class).getEntryLenient(descriptor).getCoverage();
        }
    }

    /**
     * Creates a layer table.
     *
     * @param database Connection to the database.
     */
    public LayerTable(final Database database) {
        super(new LayerQuery(database), CRS.XYT);
        setIdentifierParameters(((LayerQuery) query).byName, null);
    }

    /**
     * Creates a layer from the current row in the specified result set.
     *
     * @param  results The result set to read.
     * @return The entry for current row in the specified result set.
     * @throws CatalogException if an inconsistent record is found in the database.
     * @throws SQLException if an error occured while reading the database.
     */
    protected Layer createEntry(final ResultSet results) throws CatalogException, SQLException {
        final LayerQuery query = (LayerQuery) super.query;
        final String name      = results.getString(indexOf(query.name     ));
        final String thematic  = results.getString(indexOf(query.thematic ));
        double       period    = results.getDouble(indexOf(query.period   ));
        if (results.wasNull()) {
            period = Double.NaN;
        }
        final String fallback  = results.getString(indexOf(query.fallback));
        final String remarks   = results.getString(indexOf(query.remarks ));
        /*
         * Utilise une table d'images distinctes pour chaque couches. La couche ne devrait plus
         * changer après la construction.  Pour cette raison, l'instance de GridCoverageTable
         * utilisée ici ne devra jamais être accessible publiquement.
         */
        final LayerEntry entry;
        entry = new LayerEntry(name, thematic, period, remarks);
        entry.fallback = fallback;
        return entry;
    }

    /**
     * Completes the creation of the specified element. This method add the following piece
     * to the given layer:
     * <p>
     * <ul>
     *   <li>The {@linkplain Layer#getFallback fallback layer}, if any.</li>
     *   <li>The {@linkplain Layer#getSeries series}.</li>
     *   <li>The {@linkplain LayerEntry#setDataConnection data connection}.</li>
     *   <li>The {@linkplain LayerEntry#getModel linear model}, if any.</li>
     * </ul>
     */
    @Override
    protected void postCreateEntry(final Layer layer) throws CatalogException, SQLException {
        super.postCreateEntry(layer);
        final LayerEntry entry = (LayerEntry) layer;
        if (series == null) {
            series = new SeriesTable(getDatabase().getTable(SeriesTable.class));
        }
        series.setLayer(entry);
        entry.setSeries(series.getEntries());
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
         *    Si ce code est exécuté par net.seagis.coverage.catalog.rmi.Server, alors
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
            data = factory.connectLayer(layer.getName());
        } catch (RemoteException exception) {
            throw new ServerException(exception);
        }
        entry.setDataConnection(data);
        if (models == null) {
            final Database database = getDatabase();
            DescriptorTable descriptors = database.getTable(DescriptorTable.class);
            descriptors = new DescriptorTable(descriptors); // Protect the shared instance from changes.
            descriptors.setLayerTable(this);
            models = new LinearModelTable(database.getTable(LinearModelTable.class));
            models.setDescriptorTable(descriptors);
        }
        entry.model = models.getEntry(entry);
    }
}
