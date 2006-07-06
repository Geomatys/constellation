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
package net.sicade.observation.sql;

// J2SE dependencies
import java.util.Collection;
import java.awt.geom.Point2D;
import java.sql.SQLException;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.quality.DataQuality;

// Sicade dependencies
import net.sicade.util.DateRange;
import net.sicade.observation.Station;
import net.sicade.observation.Platform;
import net.sicade.observation.Observable;
import net.sicade.observation.Observation;
import net.sicade.observation.ServerException;
import net.sicade.observation.CatalogException;


/**
 * Impl�mentation d'une entr�e repr�sentant une {@link Station station}.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 *
 * @todo L'impl�mentation actuelle n'est pas <cite>serializable</cite> du fait qu'elle n�cessite
 *       une connexion � la base de donn�es. Une version future devrait r�tablir la connexion au
 *       moment de la <cite>deserialization</cite>.
 */
public class StationEntry extends LocatedEntry implements Station {
    /**
     * Pour compatibilit�s entre les enregistrements binaires de diff�rentes versions.
     */
    private static final long serialVersionUID = 8822736167506306189L;

    /**
     * L'identifiant num�rique de la station.
     */
    private final int identifier;

    /**
     * La plateforme (par exemple un bateau) sur laquelle a �t� prise cette station.
     * Peut �tre nul si cette information n'est pas disponible.
     */
    private final Platform platform;

    /**
     * La qualit� de la donn�e. Peut �tre nul si cette information n'est pas disponible.
     */
    private final DataQuality quality;

    /**
     * La provenance de la donn�e. Peut �tre nul si cette information n'est pas disponible.
     */
    private final Citation provider;

    /**
     * Connexion vers la table des observations. Contrairement � la plupart des autres
     * entr�es du paquet {@code net.sicade.observation}, les observations ne seront pas
     * conserv�es dans une cache car elle sont potentiellement tr�s nombreuses. Il nous
     * faudra donc conserver la connexion en permanence.
     */
    private final ObservationTable<? extends Observation> table;

    /** 
     * Construit une entr�e pour l'identifiant de station sp�cifi�.
     *
     * @param table      La table qui a produit cette entr�e.
     * @param identifier L'identifiant num�rique de la station.
     * @param name       Le nom de la station.
     * @param coordinate Une coordonn�e repr�sentative en degr�s de longitude et de latitude,
     *                   ou {@code null} si inconue.
     * @param timeRange  Plage de temps de cet �l�ment, ou {@code null} si inconue.
     * @param platform   La plateforme (par exemple un bateau) sur laquelle a �t� prise cette
     *                   station, ou {@code null} si inconnue.
     * @param quality    La qualit� de la donn�e, ou {@code null} si inconnue.
     * @param provider   La provenance de la donn�e, ou {@code null} si inconnue.
     */
    protected StationEntry(final StationTable table,
                           final int          identifier,
                           final String       name,
                           final Point2D      coordinate,
                           final DateRange    timeRange,
                           final Platform     platform,
                           final DataQuality  quality,
                           final Citation     provider)
    {
        super((table.isAbridged() && coordinate!=null && timeRange!=null) ? null :
               table.getLocationTable(), name, coordinate, timeRange);
        this.identifier = identifier;
        this.platform   = platform;
        this.quality    = quality;
        this.provider   = provider;
        this.table      = table.getObservationTable();
    }

    /**
     * {@inheritDoc}
     */
    public int getNumericIdentifier() {
        return identifier;
    }

    /**
     * {@inheritDoc}
     */
    public Citation getProvider() {
        return provider;
    }
    
    /**
     * {@inheritDoc}
     */
    public Platform getPlatform() {
        return platform;
    }

    /**
     * {@inheritDoc}
     */
    public DataQuality getQuality() {
        return quality;
    }

    /**
     * {@inheritDoc}
     */
    public Observation getObservation(final Observable observable) throws CatalogException {
        try {
            synchronized (table) {
                table.setStation(this);
                table.setObservable(observable);
                return table.getEntry();
            }
        } catch (SQLException exception) {
            throw new ServerException(exception);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Collection<? extends Observation> getObservations() throws CatalogException {
        try {
            synchronized (table) {
                table.setStation   (this);
                table.setObservable(null);
                return table.getEntries();
            }
        } catch (SQLException exception) {
            throw new ServerException(exception);
        }
    }

    /**
     * Retourne le code num�rique identifiant cette entr�e.
     */
    @Override
    public int hashCode() {
        return identifier;
    }

    /**
     * V�rifie que cette station est identique � l'objet sp�cifi�
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final StationEntry that = (StationEntry) object;
            return                 (this.identifier == that.identifier) &&
                   Utilities.equals(this.platform,     that.platform)   &&
                   Utilities.equals(this.quality,      that.quality)    &&
                   Utilities.equals(this.provider,     that.provider);
        }
        return false;
    }
}
