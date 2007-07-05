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
import java.util.Set;
import java.util.Map;
import java.util.Date;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Collections;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.io.IOException;
import java.sql.SQLException;
import java.rmi.RemoteException;

// OpenGIS dependencies
import org.opengis.coverage.Coverage;
import org.opengis.metadata.extent.GeographicBoundingBox;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.coverage.CoverageStack;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;

// Sicade dependencies
import net.sicade.util.DateRange;
import net.sicade.observation.sql.Entry;
import net.sicade.observation.sql.ObservableEntry;
import net.sicade.observation.CatalogException;
import net.sicade.observation.ServerException;
import net.sicade.observation.Procedure;
import net.sicade.observation.coverage.Model;
import net.sicade.observation.coverage.Series;
import net.sicade.observation.coverage.Format;
import net.sicade.observation.coverage.Thematic;
import net.sicade.observation.coverage.Operation;
import net.sicade.observation.coverage.SubSeries;
import net.sicade.observation.coverage.CoverageReference;
import net.sicade.observation.coverage.rmi.DataConnection;


/**
 * Implémentation d'une entrée représentant une {@linkplain Series séries d'images}.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
public class SeriesEntry extends ObservableEntry implements Series {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 5283559646740856038L;

    /**
     * Nombre de millisecondes dans une journée.
     */
    private static final long MILLIS_IN_DAY = 24*60*60*1000L;

    /**
     * L'intervalle de temps typique des images de cette série (en nombre
     * de jours), ou {@link Double#NaN} si elle est inconnue.
     */
    private final double timeInterval;

    /**
     * Si cette série est le résultat d'un modèle numérique, ce modèle.
     * Sinon, {@code null}. Ce champ sera initialisé par {@link SeriesTable}.
     */
    Model model;

    /**
     * Les sous-séries. Sera construit par {@link SeriesTable#postCreateEntry}.
     */
    Set<SubSeries> subseries;

    /**
     * Une série de second recours qui peut être utilisée si aucune données n'est disponible
     * dans cette série à une certaine position spatio-temporelle. Peut être {@code null} s'il
     * n'y a pas de série de second recours.
     * <p>
     * Lors de la construction d'une série, ce champ est initialement le nom de la série sous forme
     * d'objet {@link String}. Ce n'est que lors de l'appel de {@link SeriesTable#postCreateEntry}
     * que ce nom est convertit en objet {@link SeriesEntry}.
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
     * Construit une nouvelle séries.
     *
     * @param name         Le nom de la série.
     * @param thematic     La thématique de cette série de données.
     * @param procedure    La procédure ayant servit à obtenir les données de cette série.
     * @param timeInterval L'intervalle de temps typique des images de cette série (en nombre
     *                     de jours), ou {@link Double#NaN} si elle est inconnue.
     * @param remarks      Remarques s'appliquant à cette entrée, ou {@code null}.
     *
     * @throws CatalogException Si cette entrée n'a pas pu être construite.
     */
    protected SeriesEntry(final String    name,
                          final Thematic  thematic,
                          final Procedure procedure,
                          final double    timeInterval,
                          final String    remarks) throws CatalogException
    {
        super(name.hashCode() ^ (int)serialVersionUID, // Simulation d'un identifiant numérique.
              name, thematic, procedure, null, remarks);
        this.timeInterval = timeInterval;
        this.subseries    = subseries;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Thematic getPhenomenon() {
        return (Thematic) super.getPhenomenon();
    }

    /**
     * {@inheritDoc}
     */
    public Series getFallback() {
        final Object fallback = this.fallback; // Protect from changes in concurrent threads.
        return (fallback instanceof Series) ? (Series) fallback : null;
    }

    /**
     * {@inheritDoc}
     */
    public Set<SubSeries> getSubSeries() {
        if (subseries != null) {
            return subseries;
        } else {
            return Collections.emptySet();
        }
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
    public DateRange getTimeRange() throws CatalogException {
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
    public synchronized CoverageReference getCoverageReference(final Date time) throws CatalogException {
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
        final DataConnection server = this.server;   // Avoid synchronization.
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
    public Model getModel() throws CatalogException {
        return model;
    }

    /**
     * Retourne une connexion vers les données de la même série, mais avec l'opération spécifiée.
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
     * appellée une et une seule fois par {@link SeriesTable#postCreateEntry} pour chaque
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
     * Vérifie si l'objet spécifié est identique à cette série.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final SeriesEntry that = (SeriesEntry) object;
            return Double.doubleToLongBits(this.timeInterval) ==
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
