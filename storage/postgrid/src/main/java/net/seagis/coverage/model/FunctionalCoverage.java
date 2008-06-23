/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2006, Institut de Recherche pour le Développement
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
package net.seagis.coverage.model;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.GregorianCalendar;
import static java.lang.Math.*;
import static java.lang.Double.NaN;
import static java.lang.Double.isNaN;
import static java.lang.Double.isInfinite;

import org.opengis.coverage.Coverage;
import org.opengis.coverage.SampleDimension;
import org.opengis.coverage.CannotEvaluateException;
import org.opengis.geometry.DirectPosition;
import org.geotools.coverage.AbstractCoverage;
import org.geotools.referencing.crs.DefaultTemporalCRS;
import static org.geotools.referencing.CRS.getTemporalCRS;

import net.seagis.catalog.CRS;
import net.seagis.coverage.catalog.GridCoverage;


/**
 * Une couverture à une seule bande dont les valeurs sont calculées par une fonction plutôt que
 * déterminée à partir de données. Le système de référence des coordonnées est fixé à celui de
 * {@link CRS#XYT}.
 * <p>
 * Un ensemble de couvertures sont pré-définies pour des descripteurs tels que {@code cos(t)}.
 * Ces couvertures peuvent être obtenues par un appel à {@link #getCoverage(String)}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
abstract class FunctionalCoverage extends AbstractCoverage implements GridCoverage {
    /**
     * For compatibility during cross-version serialization.
     */
    private static final long serialVersionUID = 3894665589165786383L;

    /**
     * Ensemble des couvertures prédéfinies.
     */
    private static final Map<String, GridCoverage> COVERAGES = new HashMap<String,GridCoverage>();

    /**
     * Ajoute à {@link #COVERAGES} des couvertures pré-définies.
     * L'ajout est effectué par le constructeur de {@link FunctionalCoverage}.
     */
    static {
        new Identity();
        new Longitude();
        new Latitude();
        new SinusLatitude();
        new CosinusLatitude();
        new CosinusTime();
    }

    /**
     * Construit une nouvelle instance pour le nom spécifié. L'instante construite sera
     * automatiquement ajoutée à l'ensemble des {@linkplain #getCoverage couvertures pré-définies}.
     *
     * @param  name Le nom de la nouvelle couverture.
     * @param  crs Son système de référence des coordonnées.
     * @throws IllegalArgumentException si une couverture était déjà enregistrée pour le nom spécifié.
     */
    protected FunctionalCoverage(final String name) throws IllegalArgumentException {
        super(name, CRS.XYT.getCoordinateReferenceSystem(), null, null);
        synchronized (COVERAGES) {
            final GridCoverage old = COVERAGES.put(name, this);
            if (old != null) {
                COVERAGES.put(name, old);
                throw new IllegalArgumentException(name);
            }
        }
    }

    /**
     * Retourne une couverture pour le nom spécifié, ou {@code null} s'il n'y en a pas.
     */
    public static GridCoverage getCoverage(final String name) {
        synchronized (COVERAGES) {
            return COVERAGES.get(name);
        }
    }

    /**
     * Retourne la valeur de la fonction pour la position spécifiée.
     *
     * @param  coord La coordonnées à laquelle évaluer cette fonction.
     * @throws CannotEvaluateException si la valeur ne peut pas être calculée.
     */
    protected abstract double compute(final DirectPosition coord) throws CannotEvaluateException;

    /**
     * Retourne la valeur de la fonction pour la position spécifiée, sous forme
     * d'un tableau de type {@code double[]} de longueur 1.
     */
    public final double[] evaluate(final DirectPosition coord) {
        return new double[] {compute(coord)};
    }

    /**
     * Retourne la valeur de la fonction pour la position spécifiée, sous forme
     * d'un tableau de type {@code double[]} de longueur 1.
     */
    @Override
    public final double[] evaluate(final DirectPosition coord, double[] dest) {
        if (dest == null) {
            dest = new double[1];
        }
        dest[0] = compute(coord);
        return dest;
    }

    /**
     * Retourne la valeur de la fonction pour la position spécifiée, sous forme
     * d'un tableau de type {@code float[]} de longueur 1.
     */
    @Override
    public final float[] evaluate(final DirectPosition coord, float[] dest) {
        if (dest == null) {
            dest = new float[1];
        }
        dest[0] = (float) compute(coord);
        return dest;
    }

    /**
     * Retourne la valeur de la fonction pour la position spécifiée, sous forme
     * d'un tableau de type {@code int[]} de longueur 1.
     */
    @Override
    public final int[] evaluate(final DirectPosition coord, int[] dest) {
        if (dest == null) {
            dest = new int[1];
        }
        dest[0] = (int) max(Integer.MIN_VALUE, min(Integer.MAX_VALUE, rint(compute(coord))));
        return dest;
    }

    /**
     * Retourne la valeur de la fonction pour la position spécifiée, sous forme
     * d'un tableau de type {@code byte[]} de longueur 1.
     */
    @Override
    public final byte[] evaluate(final DirectPosition coord, byte[] dest) {
        if (dest == null) {
            dest = new byte[1];
        }
        dest[0] = (byte) max(Byte.MIN_VALUE, min(Byte.MAX_VALUE, rint(compute(coord))));
        return dest;
    }

    /**
     * Retourne la valeur de la fonction pour la position spécifiée, sous forme
     * d'un tableau de type {@code boolean[]} de longueur 1.
     */
    @Override
    public final boolean[] evaluate(final DirectPosition coord, boolean[] dest) {
        if (dest == null) {
            dest = new boolean[1];
        }
        final double v = compute(coord);
        dest[0] = !Double.isNaN(v) && v!=0;
        return dest;
    }

    /**
     * Retourne le nombre de dimensions, qui sera toujours 1.
     */
    public final int getNumSampleDimensions() {
        return 1;
    }

    /**
     * Retourne une description de la bande.
     *
     * @todo Pas encore implémentée.
     */
    public final SampleDimension getSampleDimension(final int index) {
        throw new UnsupportedOperationException();
    }

    /**
     * Retourne la position spécifiée inchangée.
     */
    public final DirectPosition snap(final DirectPosition position) {
        return position;
    }

    /**
     * Retourne un ensemble toujours vide.
     */
    public final List<Coverage> coveragesAt(final DirectPosition position) {
        return Collections.emptyList();
    }

    /**
     * La couverture identity.
     */
    private static final class Identity extends FunctionalCoverage {
        public Identity() {
            super("\u2460");
        }
        protected double compute(final DirectPosition coord) {
            return 1;
        }
    }

    /**
     * Une couverture retournant la longitude.
     */
    private static final class Longitude extends FunctionalCoverage {
        public Longitude() {
            super("\u03BB");
        }
        protected double compute(final DirectPosition coord) {
            return coord.getOrdinate(0);
        }
    }

    /**
     * Une couverture retournant la latitude.
     */
    private static final class Latitude extends FunctionalCoverage {
        public Latitude() {
            super("\u03C6");
        }
        protected double compute(final DirectPosition coord) {
            return coord.getOrdinate(1);
        }
    }

    /**
     * Une couverture retournant le sinus de la latitude.
     */
    private static final class SinusLatitude extends FunctionalCoverage {
        public SinusLatitude() {
            super("sin(\u03C6)");
        }
        protected double compute(final DirectPosition coord) {
            return sin(toRadians(coord.getOrdinate(1)));
        }
    }

    /**
     * Une couverture retournant le cosinus de la latitude.
     */
    private static final class CosinusLatitude extends FunctionalCoverage {
        public CosinusLatitude() {
            super("cos(\u03C6)");
        }
        protected double compute(final DirectPosition coord) {
            return cos(toRadians(coord.getOrdinate(1)));
        }
    }

    /**
     * Une couverture retournant le cosinus de la date. Cette couverture simule le calcul
     * {@code EXTRACT(DOY FROM TIMESTAMP ...)} de PostgreSQL, ou {@code DOY} signifie
     * <cite>Day Of Year</cite> et est une valeur entre 1 et (365 ou 366) inclusivement.
     * Le facteur multiplicatif ({@code PI / 182.625}) doit être identique à celui qui
     * est utilisé dans la requête {@code "AllEnvironments"}.
     */
    private static final class CosinusTime extends FunctionalCoverage {
        /**
         * Le système de référence des coordonnées pour l'axe du temps.
         */
        private final DefaultTemporalCRS crs;

        /**
         * Le calendrier à utiliser pour manipuler les dates.
         */
        private final Calendar calendar;

        /**
         * Construit une converture pour le descripteur {@code cos(t)}.
         */
        public CosinusTime() {
            super("cos(t)");
            crs = DefaultTemporalCRS.wrap(getTemporalCRS(getCoordinateReferenceSystem()));
            calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.CANADA);
        }

        /**
         * Calcule la valeur de {@code cos(t)}.
         */
        protected synchronized double compute(final DirectPosition coord) {
            double t = coord.getOrdinate(2);
            if (isNaN(t) || isInfinite(t)) {
                return NaN;
            }
            calendar.setTime(crs.toDate(t));
            t = (((((calendar.get(Calendar.MILLISECOND)) / 1000.0 +
                     calendar.get(Calendar.SECOND     )) /   60.0 +
                     calendar.get(Calendar.MINUTE     )) /   60.0 +
                     calendar.get(Calendar.HOUR_OF_DAY)) /   24.0 +
                     calendar.get(Calendar.DAY_OF_YEAR));
            return cos(t * (PI / 182.625));
        }
    }
}
