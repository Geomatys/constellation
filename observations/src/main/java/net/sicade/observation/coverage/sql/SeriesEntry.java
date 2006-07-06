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
import java.util.Set;
import java.util.Map;
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
 * Impl�mentation d'une entr�e repr�sentant une {@linkplain Series s�ries d'images}.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
public class SeriesEntry extends ObservableEntry implements Series {
    /**
     * Pour compatibilit�s entre les enregistrements binaires de diff�rentes versions.
     */
    private static final long serialVersionUID = 5283559646740856038L;

    /**
     * L'intervalle de temps typique des images de cette s�rie (en nombre
     * de jours), ou {@link Double#NaN} si elle est inconnue.
     */
    private final double timeInterval;

    /**
     * Si cette s�rie est le r�sultat d'un mod�le num�rique, ce mod�le.
     * Sinon, {@code null}. Ce champ sera initialis� par {@link SeriesTable}.
     */
    Model model;

    /**
     * Les sous-s�ries. Sera construit par {@link SeriesTable#postCreateEntry}.
     */
    Set<SubSeries> subseries;

    /**
     * Une s�rie de second recours qui peut �tre utilis�e si aucune donn�es n'est disponible
     * dans cette s�rie � une certaine position spatio-temporelle. Peut �tre {@code null} s'il
     * n'y a pas de s�rie de second recours.
     * <p>
     * Lors de la construction d'une s�rie, ce champ est initialement le nom de la s�rie sous forme
     * d'objet {@link String}. Ce n'est que lors de l'appel de {@link SeriesTable#postCreateEntry}
     * que ce nom est convertit en objet {@link SeriesEntry}.
     */
    Object fallback;

    /**
     * Une vue tri-dimensionnelle des donn�es retourn�e par {@link #getCoverage}.
     * Sera construit la premi�re fois o� elle sera demand�e.
     */
    private transient Reference<Coverage> coverage;

    /**
     * La fabrique � utiliser pour construire des objets {@link CoverageReference}. Cette fabrique
     * peut �tre un objet {@link java.rmi.server.RemoteServer}, et donc construire les images sur
     * un serveur distant.
     */
    private DataConnection server;

    /**
     * Ensemble des fabriques pour diff�rentes op�rations. Ne seront construites que lorsque
     * n�cessaire. Si {@link #server} �tait une connexion vers un objet RMI, alors il en sera
     * de m�me pour les valeurs de ce {@code Map}.
     */
    private transient Map<Operation,DataConnection> servers;

    /**
     * Construit une nouvelle s�ries.
     *
     * @param name         Le nom de la s�rie.
     * @param thematic     La th�matique de cette s�rie de donn�es.
     * @param procedure    La proc�dure ayant servit � obtenir les donn�es de cette s�rie.
     * @param timeInterval L'intervalle de temps typique des images de cette s�rie (en nombre
     *                     de jours), ou {@link Double#NaN} si elle est inconnue.
     * @param remarks      Remarques s'appliquant � cette entr�e, ou {@code null}.
     *
     * @throws CatalogException Si cette entr�e n'a pas pu �tre construite.
     */
    protected SeriesEntry(final String    name,
                          final Thematic  thematic,
                          final Procedure procedure,
                          final double    timeInterval,
                          final String    remarks) throws CatalogException
    {
        super(name.hashCode() ^ (int)serialVersionUID, // Simulation d'un identifiant num�rique.
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
            LOGGER.fine("Reconstruit � nouveau la converture de \"" + getName() + "\".");
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
     * Retourne une connexion vers les donn�es de la m�me s�rie, mais avec l'op�ration sp�cifi�e.
     * Cette m�thode est appel�e par le constructeur de {@link DataCoverage}.
     *
     * @param  operation L'op�ration d�sir�e, ou {@code null} si aucune.
     * @return Une connexion vers les donn�es produites par l'op�ration sp�cifi�e.
     * @throws RemoteException  si un probl�me est survenu lors de la communication avec le serveur.
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
     * D�finit la connexion vers les donn�es � utiliser pour cette entr�e. Cette m�thode est
     * appell�e une et une seule fois par {@link SeriesTable#postCreateEntry} pour chaque
     * entr�e cr��e.
     *
     * @param data Connexion vers une vue des donn�es comme une matrice tri-dimensionnelle.
     * @throws IllegalStateException si une connexion existait d�j� pour cette entr�e.
     */
    protected synchronized void setDataConnection(final DataConnection data) throws IllegalStateException {
        if (server != null) {
            throw new IllegalStateException(getName());
        }
        server = data;
    }

    /**
     * V�rifie si l'objet sp�cifi� est identique � cette s�rie.
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
             * On ne teste pas 'fallback' car la m�thode 'equals' doit fonctionner d�s que la
             * construction de l'entr�e est compl�t�e, alors que 'fallback' est d�finit un peu
             * plus tard ('postCreateEntry').
             */
        }
        return false;
    }
}
