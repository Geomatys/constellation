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
package org.constellation.coverage.catalog;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.sql.SQLException;
import java.rmi.RemoteException;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.NoSuchRecordException;
import org.constellation.catalog.ServerException;
import org.constellation.coverage.model.Descriptor;
import org.constellation.coverage.model.Model;

import org.opengis.coverage.Coverage;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.geotools.resources.Utilities;
import org.geotools.util.WeakValueHashMap;
import org.geotools.util.DateRange;

import org.constellation.catalog.Database;
import org.constellation.coverage.model.DescriptorTable;


/**
 * A grid coverage catalog.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
@Deprecated
public class Catalog {
    /**
     * Une instance de {@code Catalog} connectée à la base de données par défaut. Cette
     * base de données est habituellement déclarée dans un fichier {@code DatabaseQueries.xml}
     * situé dans le répertoire de l'utilisateur.
     */
    private static Catalog DEFAULT;

    /**
     * La connexion vers la base de données. Si sa valeur est {@code null}, alors une connexion
     * par défaut ne sera établie que la première fois où elle sera nécessaire.
     */
    private Database database;

    /**
     * Connections vers les table des couches pour différentes enveloppes spatio-temporelles.
     * Chaque connexion ne sera construite que la première fois où elle sera nécessaire. La
     * valeur associée à la clé {@code null} sera la table de toutes les couches sans restriction.
     */
    private final Map<Envelope,LayerTable> layers = new HashMap<Envelope,LayerTable>();

    /**
     * L'ensemble des couvertures de données obtenues par {@link #getCoverage}.
     */
    private final Map<String, GridCoverage> coverages = new WeakValueHashMap<String, GridCoverage>();

    /**
     * Construit une instance de {@code Catalog} pour la base de données spécifiée.
     *
     *
     * @param database Connexion à la base de données, ou {@code null} pour utiliser la
     *        base de données par défaut.
     */
    public Catalog(final Database database) {
        this.database = database;
    }

    /**
     * Une instance de {@code Catalog} connectée à la base de données par défaut. Cette
     * base de données est habituellement déclarée dans un fichier {@code DatabaseQueries.xml}
     * situé dans le répertoire de l'utilisateur.
     */
    public static synchronized Catalog getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new Catalog(null);
        }
        return DEFAULT;
    }

    /**
     * Retourne une connexion à la base de données.
     *
     * @return La connexion à la base de données (jamais nulle).
     * @param  CatalogException si la base de données n'a pas pu être obtenue.
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
     * @param  CatalogException si la table n'a pas pu être obtenue.
     *
     * @todo Envisager de cacher le résultat. Et aussi de fournir la possibilité de
     *       réduire la région géographique.
     */
    private DescriptorTable getDescriptorTable() throws CatalogException {
        return getDatabase().getTable(DescriptorTable.class);
    }

    /**
     * Retourne la table des couches pour la région spatio-temporelle spécifiée.
     *
     * @param  area La région géographique des couches désirées, ou {@code null} pour ne placer aucune
     *         restriction géographique.
     * @param  timeRange La plage de temps des couches désirées, ou {@code null} pour ne placer aucune
     *         restriction temporelle.
     * @return La table des couches interceptant la région géographique et la plage de temps spécifiées.
     * @throws SQLException si une erreur est survenue lors de l'interrogation de la base de données.
     */
    private LayerTable getLayerTable(final GeographicBoundingBox area, final DateRange timeRange)
            throws CatalogException, SQLException
    {
        final Envelope envelope;
        if (area!=null || timeRange!=null) {
            envelope = new Envelope(area, timeRange);
        } else {
            envelope = null;
        }
        LayerTable table = layers.get(envelope);
        if (table == null) {
            table = getDatabase().getTable(LayerTable.class);
            if (area != null) {
                table.setGeographicBoundingBox(area);
            }
            if (timeRange != null) {
                table.setTimeRange(timeRange);
            }
            layers.put(envelope, table);
        }
        return table;
    }

    /**
     * Retourne l'ensemble des couches disponibles dans la base de données. Si une région géographique
     * ou une plage de temps sont spécifiées, alors seules les couches interceptant ces régions seront
     * retournées.
     *
     * @param  area La région géographique des couches désirées, ou {@code null} pour ne placer aucune
     *         restriction géographique.
     * @param  timeRange La plage de temps des couches désirées, ou {@code null} pour ne placer aucune
     *         restriction temporelle.
     * @return L'ensemble des couches interceptant la région géographique et la plage de temps spécifiées.
     * @throws CatalogException si une erreur est survenue lors de l'interrogation du catalogue.
     */
    public synchronized Set<Layer> getLayer(final GeographicBoundingBox area,
                                            final DateRange        timeRange)
            throws CatalogException
    {
        try {
            return getLayerTable(area, timeRange).getEntries();
        } catch (SQLException exception) {
            throw new ServerException(exception);
        }
    }

    /**
     * Retourne la couches de données du nom spécifié dans la région spatio-temporelle spécifiée.
     *
     * @param  area La région géographique des couches désirées, ou {@code null} pour ne placer aucune
     *         restriction géographique.
     * @param  timeRange La plage de temps des couches désirées, ou {@code null} pour ne placer aucune
     *         restriction temporelle.
     * @param  name Nom de la couche désirée.
     * @return Une couche de nom spécifié.
     * @throws NoSuchRecordException si aucune couche n'a été trouvée pour le nom spécifié.
     * @throws CatalogException si une erreur est survenue lors de l'interrogation du catalogue.
     */
    public synchronized Layer getLayer(final GeographicBoundingBox area,
                                       final DateRange        timeRange,
                                       final String                name)
            throws CatalogException
    {
        try {
            return getLayerTable(area, timeRange).getEntry(name);
        } catch (SQLException exception) {
            throw new ServerException(exception);
        }
    }

    /**
     * Retourne la couches de données du nom spécifié.
     *
     * @param  name Nom de la couche désirée.
     * @return Une couche de nom spécifié.
     * @throws NoSuchRecordException si aucune couche n'a été trouvée pour le nom spécifié.
     * @throws CatalogException si une erreur est survenue lors de l'interrogation du catalogue.
     */
    public synchronized Layer getLayer(final String name) throws CatalogException {
        try {
            return getLayerTable(null, null).getEntry(name);
        } catch (SQLException exception) {
            throw new ServerException(exception);
        }
    }

    /**
     * Retourne les données pour un descripteur du nom spécifié.
     *
     * @param  name Le nom du {@linkplain Descriptor descripteur}.
     * @return La converture des données pour le descripteur spécifié.
     * @throws NoSuchRecordException si aucun descripteur n'a été trouvée pour le nom spécifié.
     * @throws CatalogException si une erreur est survenue lors de l'interrogation du catalogue.
     *
     * @todo Faire en sorte que le boulot soit entièrement fait du côté du serveur RMI (sans
     *       qu'il ne soit néssaire de faire une connexion à la base de données ici).
     */
    public synchronized GridCoverage getDescriptorCoverage(final String name)
            throws CatalogException
    {
        GridCoverage coverage = coverages.get(name);
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
     * Retourne les données pour un modèle du nom spécifié.
     *
     * @param  name Le nom du {@linkplain Descriptor descripteur} ou de la {@linkplain Layer couche}.
     * @return Le modèle pour le descripteur ou la couche spécifié, ou {@code null} si la couche n'a pas de modèle.
     * @throws NoSuchRecordException si aucun descripteur ou couche n'a été trouvée pour le nom spécifié.
     * @throws CatalogException si une erreur est survenue lors de l'interrogation du catalogue.
     *
     * @todo Faire en sorte que le boulot soit entièrement fait du côté du serveur RMI (sans
     *       qu'il ne soit néssaire de faire une connexion à la base de données ici).
     */
    public synchronized Coverage getModelCoverage(final String name) throws CatalogException {
        Layer layer;
        final Database database = getDatabase();
        try {
            layer = getLayer(name);
        } catch (NoSuchRecordException ignore) {
            try {
                layer = getDescriptorTable().getEntryLenient(name).getLayer();
            } catch (SQLException exception) {
                throw new ServerException(exception);
            }
        }
        final Model model = layer.getModel();
        return (model != null) ? model.asCoverage() : null;
    }

    /**
     * Retourne l'ensemble des descripteurs dans la base de données.
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
     * Une enveloppe représentée par une {@linkplain GeographicBoundingBox région géographique} et
     * une {@linkplain DateRange plage de temps}. Cette classe sert uniquement de clés pour la cache
     * des {@linkplain LayerTable tables des couches}.
     *
     *
     * @author Martin Desruisseaux
     * @version $Id$
     */
    private static final class Envelope {
        /**
         * La région géographique des couches désirées, ou {@code null} pour ne placer aucune
         * restriction géographique.
         */
        private final GeographicBoundingBox bbox;

        /**
         * La plage de temps des couches désirées, ou {@code null} pour ne placer aucune
         * restriction temporelle.
         */
        private final DateRange timeRange;

        /**
         * Construit une enveloppe pour les limites spatio-temporelles spécifiées.
         */
        public Envelope(final GeographicBoundingBox bbox, final DateRange timeRange) {
            this.bbox      = bbox;
            this.timeRange = timeRange;
        }

        /**
         * Retourne un code à peu près unique pour cette enveloppe.
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
         * Compare cette enveloppe avec l'objet spécifié.
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
