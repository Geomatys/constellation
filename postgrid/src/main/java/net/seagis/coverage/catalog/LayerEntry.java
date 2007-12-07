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
 */
package net.seagis.coverage.catalog;

import java.util.Set;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.Iterator;
import java.util.Collections;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.rmi.RemoteException;

import org.opengis.coverage.Coverage;
import org.opengis.metadata.extent.GeographicBoundingBox;

import org.geotools.util.DateRange;
import org.geotools.util.NumberRange;
import org.geotools.resources.Utilities;
import org.geotools.coverage.CoverageStack;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;

import net.seagis.catalog.Entry;
import net.seagis.catalog.CatalogException;
import net.seagis.catalog.ServerException;
import net.seagis.coverage.model.Model;
import net.seagis.coverage.model.Operation;
import net.seagis.resources.XArray;
import net.seagis.resources.i18n.Resources;
import net.seagis.resources.i18n.ResourceKeys;


/**
 * Implémentation d'une entrée représentant une {@linkplain Layer couche d'images}.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
final class LayerEntry extends Entry implements Layer {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 5283559646740856038L;

    /**
     * Nombre de millisecondes dans une journée.
     */
    private static final long MILLIS_IN_DAY = 24*60*60*1000L;

    /**
     * Référence vers le {@linkplain Phenomenon phénomène} observé.
     */
    private final String thematic;

    /**
     * L'intervalle de temps typique des images de cette couche (en nombre
     * de jours), ou {@link Double#NaN} si elle est inconnue.
     */
    private final double timeInterval;

    /**
     * Si cette couche est le résultat d'un modèle numérique, ce modèle.
     * Sinon, {@code null}. Ce champ sera initialisé par {@link LayerTable}.
     */
    Model model;

    /**
     * The series associated with their names. This map will be created by
     * {@link LayerTable#postCreateEntry}.
     */
    private Map<String,Series> seriesMap;

    /**
     * A immutable view over the values of {@link #seriesMap}.
     */
    private Set<Series> series;

    /**
     * Une couche de second recours qui peut être utilisée si aucune données n'est disponible
     * dans cette couche à une certaine position spatio-temporelle. Peut être {@code null} s'il
     * n'y a pas de couche de second recours.
     * <p>
     * Lors de la construction d'une couche, ce champ est initialement le nom de la couche sous forme
     * d'objet {@link String}. Ce n'est que lors de l'appel de {@link LayerTable#postCreateEntry}
     * que ce nom est convertit en objet {@link LayerEntry}.
     */
    Object fallback;

    /**
     * Une vue tri-dimensionnelle des données retournée par {@link #getCoverage}.
     * Sera construit la première fois où elle sera demandée.
     */
    private transient Reference<Coverage> coverage;

    /**
     * La fabrique à utiliser pour construire des objets {@link CoverageReference}. Cette fabrique
     * peut être un objet {@link java.rmi.server.RemoteServer}, et donc construire les images sur
     * un serveur distant.
     */
    private DataConnection server;

    /**
     * Ensemble des fabriques pour différentes opérations. Ne seront construites que lorsque
     * nécessaire. Si {@link #server} était une connexion vers un objet RMI, alors il en sera
     * de même pour les valeurs de ce {@code Map}.
     */
    private transient Map<Operation,DataConnection> servers;

    /**
     * La fabrique à utiliser pour obtenir une seule image à une date spécifique.
     * Ne sera construit que la première fois où elle sera nécessaire.
     */
    private transient DataConnection singleCoverageServer;

    /**
     * Construit une nouvelle couches.
     *
     * @param name         Le nom de la couche.
     * @param thematic     La thématique de cette couche de données.
     * @param timeInterval L'intervalle de temps typique des images de cette couche (en nombre
     *                     de jours), ou {@link Double#NaN} si elle est inconnue.
     * @param remarks      Remarques s'appliquant à cette entrée, ou {@code null}.
     */
    protected LayerEntry(final String name, final String thematic, final double timeInterval,
                         final String remarks)
    {
        super(name, remarks);
        this.thematic     = thematic;
        this.timeInterval = timeInterval;
    }

    /**
     * {@inheritDoc}
     */
    public String getThematic() {
        return thematic;
    }

    /**
     * {@inheritDoc}
     */
    public Layer getFallback() {
        final Object fallback = this.fallback; // Protect from changes in concurrent threads.
        return (fallback instanceof Layer) ? (Layer) fallback : null;
    }

    /**
     * Sets the series for this layer.
     */
    final void setSeries(final Set<Series> series) {
        final Map<String,Series> map = new HashMap<String,Series>((int) (series.size() / 0.75f) + 1);
        for (final Iterator<Series> it=series.iterator(); it.hasNext();) {
            final Series entry = it.next();
            assert entry.getLayer() == this : entry;
            final String name = entry.getName().trim();
            if (map.put(name, entry) != null) {
                throw new IllegalArgumentException(Resources.format(
                        ResourceKeys.ERROR_DUPLICATED_RECORD_$1, name));
            }
            // Following is specific to SeriesEntry implementation. If faced with a different
            // implementation, we will conservatively assume that all series are to be shown.
            if (entry instanceof SeriesEntry) {
                if (!((SeriesEntry) entry).visible) {
                    it.remove();
                }
            }
        }
        this.seriesMap = map;
        this.series = Collections.unmodifiableSet(series);
    }

    /**
     * {@inheritDoc}
     */
    public Set<Series> getSeries() {
        if (series != null) {
            // Note: The series Set may have less entries than
            // the series Map since some series may be hidden.
            assert seriesMap.values().containsAll(series) : this;
            return series;
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Series getSeries(final String name) {
        return (seriesMap != null) ? seriesMap.get(name.trim()) : null;
    }

    /**
     * {@inheritDoc}
     */
    public double getTimeInterval() {
        return timeInterval;
    }

    /**
     * {@inheritDoc}
     */
    public SortedSet<Date> getAvailableTimes() throws CatalogException {
        final DataConnection server = this.server;   // Protect against concurrent changes.
        if (server != null) try {
            return server.getAvailableTimes();
        } catch (SQLException e) {
            throw new ServerException(e);
        } catch (RemoteException e) {
            throw new ServerException(e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public SortedSet<Number> getAvailableElevations() throws CatalogException {
        final DataConnection server = this.server;   // Protect against concurrent changes.
        if (server != null) try {
            return server.getAvailableElevations();
        } catch (SQLException e) {
            throw new ServerException(e);
        } catch (RemoteException e) {
            throw new ServerException(e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public NumberRange[] getSampleValueRanges() {
        NumberRange[] ranges = null;
        for (final Series series : getSeries()) {
            final Format format = series.getFormat();
            if (format != null) {
                final NumberRange[] candidates = format.getSampleValueRanges();
                if (ranges == null) {
                    ranges = candidates;
                } else {
                    final int length;
                    if (candidates.length <= ranges.length) {
                        length = candidates.length;
                    } else {
                        length = ranges.length;
                        ranges = XArray.resize(ranges, candidates.length);
                        System.arraycopy(candidates, length, ranges, length, candidates.length-length);
                    }
                    for (int i=0; i<length; i++) {
                        ranges[i] = ranges[i].intersect(candidates[i]);
                    }
                }
            }
        }
        return ranges;
    }

    /**
     * {@inheritDoc}
     */
    public DateRange getTimeRange() throws CatalogException {
        final DataConnection server = this.server;   // Protect against concurrent changes.
        if (server != null) try {
            return server.getTimeRange();
        } catch (RemoteException e) {
            throw new ServerException(e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public GeographicBoundingBox getGeographicBoundingBox() throws CatalogException {
        final DataConnection server = this.server;   // Protect against concurrent changes.
        if (server != null) try {
            return server.getGeographicBoundingBox();
        } catch (RemoteException e) {
            throw new ServerException(e);
        }
        return GeographicBoundingBoxImpl.WORLD;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized CoverageReference getCoverageReference(final Date time, final Number elevation)
            throws CatalogException
    {
        long delay = Math.round(timeInterval * (MILLIS_IN_DAY/2));
        if (delay <= 0) {
            delay = MILLIS_IN_DAY / 2;
        }
        final long t = time.getTime();
        final Date startTime = new Date(t - delay);
        final Date   endTime = new Date(t + delay);
        try {
            if (singleCoverageServer == null) {
                singleCoverageServer = server.newInstance(null);
            }
            singleCoverageServer.setTimeRange(startTime, endTime);
            final double z = (elevation != null) ? elevation.doubleValue() : 0;
            singleCoverageServer.setVerticalRange(z, z); // TODO: choose a better range.
            return singleCoverageServer.getEntry();
        } catch (RemoteException exception) {
            throw new ServerException(exception);
        } catch (SQLException exception) {
            throw new ServerException(exception);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Set<CoverageReference> getCoverageReferences() throws CatalogException {
        final DataConnection server = this.server;   // Protect against concurrent changes.
        if (server != null) try {
            return server.getEntries();
        } catch (RemoteException exception) {
            throw new ServerException(exception);
        } catch (SQLException exception) {
            throw new ServerException(exception);
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized Coverage getCoverage() throws CatalogException {
        Coverage c = null;
        if (coverage != null) {
            c = coverage.get();
            if (c != null) {
                return c;
            }
            LOGGER.fine("Reconstruit à nouveau la converture de \"" + getName() + "\".");
        }
        if (server != null) try {
            c = new CoverageStack(getName(),
                                  server.getCoordinateReferenceSystem(),
                                  getCoverageReferences());
            coverage = new SoftReference<Coverage>(c);
        } catch (IOException exception) {
            throw new ServerException(exception);
        }
        return c;
    }

    /**
     * {@inheritDoc}
     */
    public BufferedImage getLegend(final Dimension dimension) {
        final Map<Format,Integer> count = new HashMap<Format,Integer>();
        Format format = null;
        int occurs = 0;
        // Search for the most frequently used format.
        for (final Series series : getSeries()) {
            final Format candidate = series.getFormat();
            int n = 1;
            final Integer c = count.put(candidate, n);
            if (c != null) {
                n = c + 1;
                count.put(candidate, n);
            }
            if (n > occurs) {
                occurs = n;
                format = candidate;
            }
        }
        return (format != null) ? format.getLegend(dimension) : null;
    }

    /**
     * {@inheritDoc}
     */
    public Model getModel() throws CatalogException {
        return model;
    }

    /**
     * Retourne une connexion vers les données de la même couche, mais avec l'opération spécifiée.
     * Cette méthode est appelée par le constructeur de {@link DataCoverage}.
     *
     * @param  operation L'opération désirée, ou {@code null} si aucune.
     * @return Une connexion vers les données produites par l'opération spécifiée.
     * @throws RemoteException  si un problème est survenu lors de la communication avec le serveur.
     */
    protected synchronized DataConnection getDataConnection(final Operation operation) throws RemoteException {
        if (operation==null || server==null) {
            return server;
        }
        if (servers == null) {
            servers = new HashMap<Operation,DataConnection>();
        }
        DataConnection candidate = servers.get(operation);
        if (candidate == null) {
            candidate = server.newInstance(operation);
            servers.put(operation, candidate);
        }
        return candidate;
    }

    /**
     * Définit la connexion vers les données à utiliser pour cette entrée. Cette méthode est
     * appellée une et une seule fois par {@link LayerTable#postCreateEntry} pour chaque
     * entrée créée.
     *
     * @param data Connexion vers une vue des données comme une matrice tri-dimensionnelle.
     * @throws IllegalStateException si une connexion existait déjà pour cette entrée.
     */
    protected synchronized void setDataConnection(final DataConnection data) throws IllegalStateException {
        if (server != null) {
            throw new IllegalStateException(getName());
        }
        server = data;
    }

    /**
     * Vérifie si l'objet spécifié est identique à cette couche.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final LayerEntry that = (LayerEntry) object;
            return Utilities.equals(this.thematic, that.thematic) &&
                   Double.doubleToLongBits(this.timeInterval) ==
                   Double.doubleToLongBits(that.timeInterval);
            /*
             * On ne teste pas 'fallback' car la méthode 'equals' doit fonctionner dès que la
             * construction de l'entrée est complétée, alors que 'fallback' est définit un peu
             * plus tard ('postCreateEntry').
             */
        }
        return false;
    }
}
