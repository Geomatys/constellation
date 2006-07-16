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
package net.sicade.observation.coverage;

// J2SE dependencies
import java.util.Date;
import java.lang.reflect.UndeclaredThrowableException;

// Geotools dependencies
import org.geotools.coverage.SpatioTemporalCoverage3D;

// Sicade dependencies.
import net.sicade.observation.Station;
import net.sicade.observation.CatalogException;


/**
 * Une paire {@linkplain Station station} - {@linkplain Descriptor descripteur}.
 * Utilis�e par {@link MeasurementTableFiller} pour d�terminer un ordre optimal
 * dans lequel ces �l�ments devraient �tre �valu�s.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class StationDescriptorPair implements Comparable<StationDescriptorPair> {
    /**
     * La station.
     */
    final Station station;

    /**
     * Le descripteur du paysage oc�anique.
     */
    final Descriptor descriptor;

    /**
     * La valeur. Sera calcul�e par {@link MeasurementTableFiller}.
     */
    float value = Float.NaN;

    /**
     * Construit une nouvelle paire pour la station et le descripteur sp�cifi�.
     */
    public StationDescriptorPair(final Station station, final Descriptor descriptor) {
        this.station    = station;
        this.descriptor = descriptor;
    }

    /**
     * Retourne la date � laquelle le descripteur sera �valu�.
     */
    private long getTime() throws CatalogException {
        final Date time = station.getTime();
        if (time == null) {
            /*
             * Place les stations dont la date est ind�termin�e � la fin. C'est coh�rent
             * avec le classement des valeurs NaN de type 'float' par exemple.
             */
            return Long.MAX_VALUE;
        }
        return time.getTime() + Math.round((24*60*60*1000) * descriptor.getLocationOffset().getDayOffset());
    }

    /**
     * Compare cette paire avec la paire sp�cifi�e.
     */
    public int compareTo(final StationDescriptorPair that) {
        final long t1, t2;
        try {
            t1 = this.getTime();
            t2 = that.getTime();
        } catch (CatalogException exception) {
            // Sera trait� de mani�re particuli�re par MeasurementTableFiller
            throw new UndeclaredThrowableException(exception);
        }
        if (t1 < t2) return -1;
        if (t1 > t2) return +1;
        return 0;
    }

    /**
     * Retourne une repr�sentation textuelle de cette paire, � des fins de d�boguage.
     */
    @Override
    public String toString() {
        return '(' + station.getName() + ", " + descriptor.getName() + ')';
    }
}
