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

import net.sicade.observation.Station;
import net.sicade.observation.Observable;
import net.sicade.observation.Observation;
import org.geotools.resources.Utilities;


/**
 * Impl�mentation d'une entr�e repr�sentant une {@linkplain Observation observation}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public class ObservationEntry extends Entry implements Observation {
    /**
     * Pour compatibilit�s entre les enregistrements binaires de diff�rentes versions.
     */
    private static final long serialVersionUID = 3269639171560208276L;

    /**
     * La station � laquelle a �t� pris cet �chantillon.
     */
    private final Station station;

    /**
     * Ce que l'on observe
     */
    private final Observable observable;

    /**
     * Construit une observation.
     * 
     * @param station     La station d'observation (par exemple une position de p�che).
     * @param observable  Ce que l'on observe (temp�rature, quantit� p�ch�e, <cite>etc.</cite>).
     */
    protected ObservationEntry(final Station    station, 
                               final Observable observable) 
    {
        super(null);
        this.station    = station;
        this.observable = observable;
    }

    /**
     * Construit un nom � partir des autres informations disponibles.
     */
    @Override
    StringBuilder createName() {
        final StringBuilder buffer = new StringBuilder(observable.getName());
        buffer.append('(');
        buffer.append(station.getName());
        buffer.append(')');
        return buffer;
    }

    /**
     * {@inheritDoc}
     */
    public Station getStation() {
        return station;
    }

    /**
     * {@inheritDoc}
     */
    public Observable getObservable() {
        return observable;
    }

    /**
     * Retourne un code repr�sentant cette observation.
     */
    @Override
    public final int hashCode() {
        return station.hashCode() ^ observable.hashCode();
    }
    
    /**
     * V�rifie si cette entr� est identique � l'objet sp�cifi�.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final ObservationEntry that = (ObservationEntry) object;
            return Utilities.equals(this.observable, that.observable) && 
                   Utilities.equals(this.station,    that.station);
        }
        return false;
    }
}
