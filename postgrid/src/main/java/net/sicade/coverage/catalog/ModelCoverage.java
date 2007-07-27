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
package net.sicade.coverage.catalog;

// J2SE dependencies
import java.util.Map;
import java.util.List;
import java.util.HashMap;

// OpenGIS dependencies
import org.opengis.coverage.Coverage;
import org.opengis.coverage.SampleDimension;
import org.opengis.coverage.CannotEvaluateException;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

// Geotools dependencies
import org.geotools.coverage.AbstractCoverage;
import org.geotools.geometry.GeneralEnvelope;

// Sicade dependencies
import net.sicade.sql.CRS;

/**
 * Une couverture qui délèguera les {@linkplain #evaluate(DirectPosition,double[]) évaluations} à
 * un {@linkplain Model modèle}, pas nécessairement linéaire.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ModelCoverage extends AbstractCoverage {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = -1327909083485951850L;

    /**
     * The model.
     */
    private final Model model;

    /**
     * Coverages for each model descriptor.
     */
    private final Coverage[] coverages;

    /**
     * Band to extract for each coverage.
     */
    private final int[] bands;

    /**
     * Pre-allocated array of values to be given to the model.
     */
    private final double[] values;

    /**
     * Buffers to be given to the {@code evaluate} method for each descriptor.
     */
    private final double[][] buffers;

    /**
     * The envelope, computed only when first needed.
     */
    private transient GeneralEnvelope envelope;

    /**
     * Construit une nouvelle couverture pour le modèle spécifié.
     *
     * @throws CatalogException si la couverture n'a pas pu être construite.
     */
    public ModelCoverage(final Model model) throws CatalogException {
        super(model.getName(), getCoordinateReferenceSystem(model), null, null);
        this.model = model;
        final List<Descriptor> descriptors = model.getDescriptors();
        coverages = new Coverage[descriptors.size()];
        values    = new double[coverages.length];
        buffers   = new double[coverages.length][];
        bands     = new int   [coverages.length];
        for (int i=0; i<coverages.length; i++) {
            final Descriptor d = descriptors.get(i);
            if (!d.isIdentity()) {
                coverages[i] = d.getCoverage();
                bands    [i] = d.getBand();
            } else {
                values   [i] = 1.0;
            }
        }
    }

    /**
     * Workaround for RFE #4093999
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private static CoordinateReferenceSystem getCoordinateReferenceSystem(final Model model)
            throws CatalogException
    {
        int bestCount = 0;
        CoordinateReferenceSystem crs = CRS.XYT.getCoordinateReferenceSystem();
        final Map<CoordinateReferenceSystem,Integer> count = new HashMap<CoordinateReferenceSystem,Integer>();
        for (final Descriptor descriptor : model.getDescriptors()) {
            final CoordinateReferenceSystem candidate = descriptor.getCoverage().getCoordinateReferenceSystem();
            final Integer value = count.get(candidate);
            final int n = (value == null) ? 1 : value+1;
            count.put(candidate, n);
            if (n > bestCount) {
                bestCount = n;
                crs = candidate;
            }
        }
        return crs;
    }

    /**
     * Returns the envelope as the intersection of all descriptor envelopes.
     */
    @Override
    public Envelope getEnvelope() {
        if (envelope == null) {
            GeneralEnvelope e = null;
            for (final Coverage coverage : coverages) {
                final Envelope candidate = coverage.getEnvelope();
                if (e == null) {
                    e = new GeneralEnvelope(candidate);
                } else {
                    e.intersect(candidate);
                }
            }
            envelope = e;
        }
        return (Envelope) envelope.clone();
    }

    /**
     * Retourne la valeur à la position spécifiée.
     *
     * @param  position La position à laquelle évaluer le descripteur.
     * @return La valeur évaluée à la position spécifiée.
     * @throws CannotEvaluateException si une erreur est survenue lors de l'évaluation.
     */
    private synchronized double evaluateSingle(final DirectPosition position)
            throws CannotEvaluateException
    {
        for (int i=0; i<values.length; i++) {
            final Coverage coverage = coverages[i];
            if (coverage != null) {
                final double[] buffer;
                buffers[i] = buffer = coverage.evaluate(position, buffers[i]);
                values [i] = buffer[bands[i]];
            }
        }
        model.normalize(values);
        return model.evaluate(values);
    }

    /**
     * Retourne la valeur à la position spécifiée.
     *
     * @param  position La position à laquelle évaluer le descripteur.
     * @param  samples Un tableau pré-alloué ou enregistrer le résultat, ou {@code null}.
     * @return La valeur évaluée à la position spécifiée.
     * @throws CannotEvaluateException si une erreur est survenue lors de l'évaluation.
     */
    @Override
    public byte[] evaluate(final DirectPosition position, final byte[] samples) throws CannotEvaluateException {
        final byte value = (byte)Math.round(evaluateSingle(position));
        if (samples == null) {
            return new byte[] {value};
        } else {
            samples[0] = value;
            return samples;
        }
    }

    /**
     * Retourne la valeur à la position spécifiée.
     *
     * @param  position La position à laquelle évaluer le descripteur.
     * @param  samples Un tableau pré-alloué ou enregistrer le résultat, ou {@code null}.
     * @return La valeur évaluée à la position spécifiée.
     * @throws CannotEvaluateException si une erreur est survenue lors de l'évaluation.
     */
    @Override
    public int[] evaluate(final DirectPosition position, final int[] samples) throws CannotEvaluateException {
        final int value = (int)Math.round(evaluateSingle(position));
        if (samples == null) {
            return new int[] {value};
        } else {
            samples[0] = value;
            return samples;
        }
    }

    /**
     * Retourne la valeur à la position spécifiée.
     *
     * @param  position La position à laquelle évaluer le descripteur.
     * @param  samples Un tableau pré-alloué ou enregistrer le résultat, ou {@code null}.
     * @return La valeur évaluée à la position spécifiée.
     * @throws CannotEvaluateException si une erreur est survenue lors de l'évaluation.
     */
    @Override
    public float[] evaluate(final DirectPosition position, final float[] samples) throws CannotEvaluateException {
        final float value = (float)evaluateSingle(position);
        if (samples == null) {
            return new float[] {value};
        } else {
            samples[0] = value;
            return samples;
        }
    }

    /**
     * Retourne la valeur à la position spécifiée.
     *
     * @param  position La position à laquelle évaluer le descripteur.
     * @param  samples Un tableau pré-alloué ou enregistrer le résultat, ou {@code null}.
     * @return La valeur évaluée à la position spécifiée.
     * @throws CannotEvaluateException si une erreur est survenue lors de l'évaluation.
     */
    @Override
    public double[] evaluate(final DirectPosition position, final double[] samples) throws CannotEvaluateException {
        final double value = evaluateSingle(position);
        if (samples == null) {
            return new double[] {value};
        } else {
            samples[0] = value;
            return samples;
        }
    }

    /**
     * Retourne la valeur à la position spécifiée.
     *
     * @param  position La position à laquelle évaluer le descripteur.
     * @return La valeur évaluée à la position spécifiée, sous forme de tableau {@code double[]}.
     * @throws CannotEvaluateException si une erreur est survenue lors de l'évaluation.
     */
    public Object evaluate(final DirectPosition position) throws CannotEvaluateException {
        return evaluate(position, (double[]) null);
    }

    /**
     * Retourne le nombre de bandes dans cette couverture. Pour ce type de couverture, il
     * sera toujours égal à 1.
     */
    public int getNumSampleDimensions() {
        return 1;
    }

    /**
     * Retourne la bande à l'index spécifiée.
     *
     * @throws IndexOutOfBoundsException si {@code index} est en dehors des limites permises.
     */
    public SampleDimension getSampleDimension(final int index) throws IndexOutOfBoundsException {
        if (index != 0) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        throw new UnsupportedOperationException();
    }
}
