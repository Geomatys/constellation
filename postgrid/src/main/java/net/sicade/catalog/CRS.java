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
package net.sicade.catalog;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.metadata.iso.extent.ExtentImpl;
import org.geotools.referencing.cs.DefaultTimeCS;
import org.geotools.referencing.crs.DefaultTemporalCRS;
import org.geotools.referencing.crs.DefaultCompoundCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.datum.DefaultTemporalDatum;


/**
 * Set of predefined coordinate reference systems. They are geographic CRS using
 * (<var>x</var>, <var>y</var>, <var>z</var>, <var>t</var>) axis on WGS&nbsp;84
 * ellipsoid and 01/01/1950 00:00 UTC epoch. More specifically, the axis are:
 * <p>
 * <ul>
 *   <li>Longitude in decimal degrees relative to Greenwich meridian</li>
 *   <li>Latitude in decimal degrees</li>
 *   <li>Altitude in metre over the WGS 84 ellipsoid</li>
 *   <li>Time as the amount of day ellapsed since January 1st, 1950.</li>
 * </ul>
 * <p>
 * Not all those axis need to be present; the set of axis is determined from the enumeration
 * used. For example the CRS designated by {@link #XYT} do not have an altitude axis. However
 * when an axis is present, it shall appears in the above-cited order.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public enum CRS {
    /**
     * A (<var>x</var>, <var>y</var>) coordinate system.
     */
    XY(-1, -1),

    /**
     * A (<var>x</var>, <var>y</var>, <var>t</var>) coordinate system.
     */
    XYT(-1, 2),
    
    /**
     * A (<var>x</var>, <var>y</var>, <var>z</var>) coordinate system.
     */
    XYZ(2, -1),
    
    /**
     * A (<var>x</var>, <var>y</var>, <var>z</var>, <var>t</var>) coordinate system.
     */
    XYZT(2, 3);

    /**
     * Système de références de coordonnées temporelles en usage chez Aviso. Ce système compte le
     * nombre de jours écoulés depuis le 01/01/1950 00:00 UTC. Ce système est aussi en usage pour
     * certaines données de la Nasa.
     */
    static final DefaultTemporalCRS TEMPORAL = new Temporal("Nasa");

    /**
     * Dimension de la longitude.
     */
    final int xdim;

    /**
     * Dimension de la latitude.
     */
    final int ydim;

    /**
     * Dimension de la profondeur, ou -1 s'il n'y en a pas.
     */
    final int zdim;

    /**
     * Dimension du temps, ou -1 s'il n'y en a pas.
     */
    final int tdim;

    /**
     * Le système de référence des coordonnées.
     */
    private CoordinateReferenceSystem crs;

    /**
     * Affecte les valeurs aux champs {@link #crs} des énumérations.
     */
    static {
        final Map<String,Object> properties = new HashMap<String,Object>(4);
        properties.put(CoordinateReferenceSystem.NAME_KEY, "WGS84");
        properties.put(CoordinateReferenceSystem.VALID_AREA_KEY, ExtentImpl.WORLD);

        XY  .crs = DefaultGeographicCRS.WGS84;
        XYZ .crs = DefaultGeographicCRS.WGS84_3D;
        XYT .crs = new DefaultCompoundCRS(properties, new CoordinateReferenceSystem[] {XY .crs, TEMPORAL});
        XYZT.crs = new DefaultCompoundCRS(properties, new CoordinateReferenceSystem[] {XYZ.crs, TEMPORAL});
    }

    /**
     * Construit une énumération.
     */
    private CRS(final int z, final int t) {
        xdim = 0;
        ydim = 1;
        zdim = z;
        tdim = t;
    }

    /**
     * Returns the coordinate reference system for this enumeration.
     */
    public final CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }

    /**
     * Système de référence des coordonnées temporelles par défaut.
     */
    private static final class Temporal extends DefaultTemporalCRS {
        /**
         * Nombre de millisecondes entre le 01/01/1970 00:00 UTC et le 01/01/1950 00:00 UTC.
         * Le 1er janvier 1970 est l'epoch du Java, tandis que le 1er janvier 1950 est celui
         * de la Nasa (son jour julier "0"). La constante {@code EPOCH} sert à faire les
         * conversions d'un système à l'autre.
         */
        private static final long EPOCH = -631152000000L; // Pour 1958, utiliser -378691200000L;

        /**
         * Construit un système de référence du nom spécifié.
         */
        public Temporal(final String name) {
            super(name, new DefaultTemporalDatum(name, new Date(EPOCH)), DefaultTimeCS.DAYS);
        }

        /**
         * Convertit une date en valeur numérique, mais en remplaçant les deux extrèmes par
         * des valeurs infinies.
         */
        @Override
        public double toValue(final Date time) {
            final long t = time.getTime();
            if (t == Long.MIN_VALUE) return Double.NEGATIVE_INFINITY;
            if (t == Long.MAX_VALUE) return Double.POSITIVE_INFINITY;
            return super.toValue(time);
        }

        /**
         * Converts the specified value to a date, returning {@code null} if the value is infinity.
         * Those null values will be processed in a special way by {@link BoundedSingletonTable}.
         * Returning {@code null} pose a risk, but returning {@code Date(Long.MIN_VALUE)} would be
         * yet more dangerous; it is better to have a {@link NullPointerException} soon than
         * performing some computation on a wrong date value.
         */
        @Override
        public Date toDate(final double value) {
            return Double.isInfinite(value) || Double.isNaN(value) ? null : super.toDate(value);
        }
    }
}
