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
package net.sicade.observation.sql;

// J2SE dependencies
import java.util.Set;
import java.util.Collections;
import java.sql.SQLException;

// Sicade dependencies
import net.sicade.observation.Station;
import net.sicade.observation.Platform;
import net.sicade.observation.ServerException;
import net.sicade.observation.CatalogException;


/**
 * Implémentation d'une entrée représentant une {@link Platform plateforme}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public class PlatformEntry extends LocatedEntry implements Platform {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 791454287176154131L;

    /**
     * L'ensemble des stations. Ne sera construit que la première fois où il sera nécessaire.
     */
    private Set<? extends Station> elements;

    /**
     * Connexion vers la table des stations.
     * Sera mis à {@code null} lorsqu'elle ne sera plus nécessaire.
     */
    private transient StationTable stations;

    /**
     * Construit une entrée pour l'identifiant de plateforme spécifié.
     *
     * @param table La table qui a produit cette entrée.
     * @param name  Le nom de la plateforme (parfois assimilé à une campagne d'échantillonage).
     */
    protected PlatformEntry(final PlatformTable table,
                            final String        name)
    {
        super(table.getLocationTable(), name, null, null);
        stations = table.getStationTable();
    }

    /**
     * {@inheritDoc}
     */
    public synchronized Set<? extends Station> getStations() throws CatalogException {
        if (elements == null) try {
            if (stations != null) {
                final Set<Station> set;
                synchronized (stations) {
                    assert equals(stations.getPlatform()) : this;
                    stations.setPlatform(this);
                    set = stations.getEntries();
                }
                elements = Collections.unmodifiableSet(set);
            }
        } catch (SQLException exception) {
            throw new ServerException(exception);
        }
        return elements;
    }

    /**
     * Complète les informations manquantes avant l'enregistrement binaire de cette entrée.
     */
    @Override
    protected void preSerialize() throws Exception {
        super.preSerialize();
        if (elements == null) {
            elements = getStations();
        }
    }
}
