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

import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import org.geotools.metadata.iso.extent.ExtentImpl;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.cs.DefaultTimeCS;
import org.geotools.referencing.crs.DefaultTemporalCRS;
import org.geotools.referencing.crs.DefaultCompoundCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.datum.DefaultTemporalDatum;


/**
 * Ensemble de syst�mes de r�f�rence des coordonn�es pr�d�finis. Ces syst�mes de r�f�rence repr�sentent
 * des coordonn�es g�ographiques (<var>x</var>, <var>y</var>, <var>z</var>, <var>t</var>) par rapport �
 * l'elllipso�de WGS&nbsp;84 et l'epoch 01/01/1950 00:00 UTC, soit dans l'ordre:
 * <p>
 * <ul>
 *   <li>La longitude en degr�s relatif au m�ridien de Greenwich</li>
 *   <li>La latitude en degr�s</li>
 *   <li>L'altitude en m�tres au dessus de l'ellipso�de WGS 84</li>
 *   <li>Le temps en nombre de jours �coul�s depuis l'epoch.</li>
 * </ul>
 * <p>
 * Ces coordonn�es ne sont pas n�cessairement toutes pr�sentes; cela d�pend de l'�num�ration
 * utilis�e. Par exemple le syst�me d�sign� par {@link #XYT} ne comprend pas l'altitude. Mais
 * les coordonn�es pr�sentes seront toujours dans cet ordre.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public enum CRS {
    /**
     * Un syst�me de r�f�rence de coordonn�es (<var>x</var>, <var>y</var>)
     */
    XY(-1, -1),

    /**
     * Un syst�me de r�f�rence de coordonn�es (<var>x</var>, <var>y</var>, <var>t</var>)
     */
    XYT(-1, 2),
    
    /**
     * Un syst�me de r�f�rence de coordonn�es (<var>x</var>, <var>y</var>, <var>z</var>)
     */
    XYZ(2, -1),
    
    /**
     * Un syst�me de r�f�rence de coordonn�es (<var>x</var>, <var>y</var>, <var>z</var>, <var>t</var>)
     */
    XYZT(2, 3);

    /**
     * Syst�me de r�f�rences de coordonn�es temporelles en usage chez Aviso. Ce syst�me compte le
     * nombre de jours �coul�s depuis le 01/01/1950 00:00 UTC. Ce syst�me est aussi en usage pour
     * certaines donn�es de la Nasa.
     */
    static final DefaultTemporalCRS TEMPORAL = new Temporal("Nasa");

    /**
     * Dimension de la longitude.
     */
    static final int X_DIMENSION = 0;

    /**
     * Dimension de la latitude.
     */
    static final int Y_DIMENSION = 1;

    /**
     * Dimension de la profondeur, ou -1 s'il n'y en a pas.
     */
    final int Z_DIMENSION;

    /**
     * Dimension du temps, ou -1 s'il n'y en a pas.
     */
    final int T_DIMENSION;

    /**
     * Le syst�me de r�f�rence des coordonn�es.
     */
    private CoordinateReferenceSystem crs;

    /**
     * Affecte les valeurs aux champs {@link #crs} des �num�rations.
     */
    static {
        final Map<String,Object> properties = new HashMap<String,Object>(4);
        properties.put(CoordinateReferenceSystem.NAME_KEY, "SEAS");
        properties.put(CoordinateReferenceSystem.VALID_AREA_KEY, ExtentImpl.WORLD);

        XY  .crs = DefaultGeographicCRS.WGS84;
        XYZ .crs = DefaultGeographicCRS.WGS84_3D;
        XYT .crs = new DefaultCompoundCRS(properties, new CoordinateReferenceSystem[] {XY .crs, TEMPORAL});
        XYZT.crs = new DefaultCompoundCRS(properties, new CoordinateReferenceSystem[] {XYZ.crs, TEMPORAL});
    }

    /**
     * Construit une �num�ration.
     */
    private CRS(final int z, final int t) {
        Z_DIMENSION = z;
        T_DIMENSION = t;
    }

    /**
     * Retourne le syst�me de r�f�rence des coordonn�es correspondant � cette �num�ration.
     */
    public final CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }

    /**
     * Syst�me de r�f�rence des coordonn�es temporelles par d�faut.
     */
    private static final class Temporal extends DefaultTemporalCRS {
        /**
         * Nombre de millisecondes entre le 01/01/1970 00:00 UTC et le 01/01/1950 00:00 UTC.
         * Le 1er janvier 1970 est l'epoch du Java, tandis que le 1er janvier 1950 est celui
         * de la Nasa (son jour julier "0"). La constante {@code EPOCH} sert � faire les
         * conversions d'un syst�me � l'autre.
         */
        private static final long EPOCH = -631152000000L; // Pour 1958, utiliser -378691200000L;

        /**
         * Construit un syst�me de r�f�rence du nom sp�cifi�.
         */
        public Temporal(final String name) {
            super(name, new DefaultTemporalDatum(name, new Date(EPOCH)), DefaultTimeCS.DAYS);
        }

        /**
         * Convertit une date en valeur num�rique, mais en rempla�ant les deux extr�mes par
         * des valeurs infinies.
         */
        @Override
        public double toValue(final Date time) {
            final long t = time.getTime();
            if (t == Long.MIN_VALUE) return Double.NEGATIVE_INFINITY;
            if (t == Long.MAX_VALUE) return Double.POSITIVE_INFINITY;
            return super.toValue(time);
        }
    }
}
