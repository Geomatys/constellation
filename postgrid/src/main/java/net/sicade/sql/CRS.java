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
package net.sicade.sql;

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
 * Ensemble de systèmes de référence des coordonnées prédéfinis. Ces systèmes de référence représentent
 * des coordonnées géographiques (<var>x</var>, <var>y</var>, <var>z</var>, <var>t</var>) par rapport à
 * l'elllipsoïde WGS&nbsp;84 et l'epoch 01/01/1950 00:00 UTC, soit dans l'ordre:
 * <p>
 * <ul>
 *   <li>La longitude en degrés relatif au méridien de Greenwich</li>
 *   <li>La latitude en degrés</li>
 *   <li>L'altitude en mètres au dessus de l'ellipsoïde WGS 84</li>
 *   <li>Le temps en nombre de jours écoulés depuis l'epoch.</li>
 * </ul>
 * <p>
 * Ces coordonnées ne sont pas nécessairement toutes présentes; cela dépend de l'énumération
 * utilisée. Par exemple le système désigné par {@link #XYT} ne comprend pas l'altitude. Mais
 * les coordonnées présentes seront toujours dans cet ordre.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public enum CRS {
    /**
     * Un système de référence de coordonnées (<var>x</var>, <var>y</var>)
     */
    XY(-1, -1),

    /**
     * Un système de référence de coordonnées (<var>x</var>, <var>y</var>, <var>t</var>)
     */
    XYT(-1, 2),
    
    /**
     * Un système de référence de coordonnées (<var>x</var>, <var>y</var>, <var>z</var>)
     */
    XYZ(2, -1),
    
    /**
     * Un système de référence de coordonnées (<var>x</var>, <var>y</var>, <var>z</var>, <var>t</var>)
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
        properties.put(CoordinateReferenceSystem.NAME_KEY, "SEAS");
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
     * Retourne le système de référence des coordonnées correspondant à cette énumération.
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
    }
}
