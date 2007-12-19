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
package net.seagis.coverage.catalog;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.sql.SQLException;

import org.opengis.coverage.Coverage;
import org.opengis.coverage.SampleDimension;
import org.opengis.coverage.CannotEvaluateException;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.operation.TransformException;

import org.geotools.coverage.CoverageStack;
import org.geotools.coverage.AbstractCoverage;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.TransformedDirectPosition;
import org.geotools.util.UnsupportedImplementationException;

import net.seagis.coverage.model.Operation;
import net.seagis.coverage.model.Descriptor;
import net.seagis.coverage.model.RegionOfInterest;
import net.seagis.catalog.CatalogException;
import net.seagis.catalog.ServerException;


/**
 * Expose les données d'un {@linkplain Descriptor descripteur} sous forme d'un objet
 * {@link org.opengis.coverage.Coverage}. Les données peuvent être calculées sur un
 * serveur distant via une référence vers un objet {@link DataConnection}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo Les déplacements horizontaux ne sont pas encore implémentés.
 * @todo Les transformations de coordonnées ne sont prises en compte.
 *
 * @deprecated Need a deep review.
 */
@Deprecated
public class DataCoverage extends AbstractCoverage {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = -7029210034299675921L;

    /**
     * Connection to the grid coverage table.
     */
    private final GridCoverageTable data;

    /**
     * Connection to the tables to use as fallback, or an empty array if none.
     * Should never be {@code null}.
     */
    private final GridCoverageTable[] fallback;

    /**
     * L'unique bande de cette couverture. Cette bande est déterminée dès la construction afin
     * d'éviter d'avoir à retenir une référence vers {@link Descriptor}, qui contient des
     * dépendences qui peuvent être assez lourdes.
     */
    private final GridSampleDimension sampleDimension;

    /**
     * Une instance d'une coordonnées à utiliser avec {@link #evaluate}.
     */
    private transient TransformedDirectPosition position;

    /**
     * Un buffer pré-alloué à utiliser avec {@link #evaluate}.
     */
    private transient double[] samples;

    /**
     * La position spatio-temporelle relative.
     */
    private final double dt;
    private static final double dx=0, dy=0; // TODO: déplacements horizontaux pas encore implémentés.

    /**
     * Le numéro de bande dans laquelle évaluer les valeurs de pixels des images.
     */
    private final short band;

    /**
     * L'envelope. Sera cachée après la première invocation de {@link #getEnvelope}.
     */
    private transient Envelope envelope;

    /**
     * Construit une vue vers le descripteur spécifié. La {@linkplain DataConnection connexion
     * vers les données} sera établie automatiquement à partir du descripteur spécifié.
     *
     * @param  descriptor Descripteur pour lequel on veut une vue des données.
     */
    public DataCoverage(final Descriptor descriptor) {
        this(descriptor, getGridCoverageTable(descriptor));
    }

    /**
     * Construit une vue vers le descripteur spécifié en utilisant la source de données spécifiée.
     *
     * @param  descriptor Descripteur pour lequel on veut une vue des données.
     * @param  data La source de données à utiliser.
     */
    private DataCoverage(final Descriptor descriptor, final GridCoverageTable data) {
        super(descriptor.getName(), data.getCoordinateReferenceSystem(), null, null);
        final RegionOfInterest offset = descriptor.getRegionOfInterest();
        this.data = data;
        this.dt   = offset.getDayOffset();
        this.band = descriptor.getBand();
        final Set<Series> series = descriptor.getLayer().getSeries();
        if (!series.isEmpty()) {
            final Format format = series.iterator().next().getFormat();
            final GridSampleDimension[] sd = format.getSampleDimensions();
            sampleDimension = sd[band];
        } else {
            sampleDimension = null;
        }
        if (super.getCoordinateReferenceSystem().equals(net.seagis.catalog.CRS.XYT)) {
            throw new UnsupportedOperationException("Transformation de coordonnées pas encore implémentée.");
        }
        if (offset.getEasting()!=0 || offset.getNorthing()!=0) {
            throw new UnsupportedOperationException("Les déplacements horizontaux ne sont pas encore implémentés.");
        }
        /*
         * Recherche les sources de données de second recours.
         */
        final Operation operation = descriptor.getOperation();
        final List<GridCoverageTable> fallback = new ArrayList<GridCoverageTable>();
        Layer layer = descriptor.getLayer();
        while ((layer=layer.getFallback()) != null) {
            if (layer instanceof LayerEntry) {
                final GridCoverageTable candidate = ((LayerEntry) layer).getGridCoverageTable(operation);
                if (crs.equals(candidate.getCoordinateReferenceSystem())) {
                    fallback.add(candidate);
                    continue;
                }
            }
            Layer.LOGGER.warning("Couche de second recours ignorée: \""+layer.getName() + '"');
        }
        this.fallback = fallback.toArray(new GridCoverageTable[fallback.size()]);
    }

    /**
     * Retourne la connexion vers les données. Ce code devrait apparaître directement
     * dans le premier constructeur si seulement Sun voulait bien faire le RFE #4093999
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private static GridCoverageTable getGridCoverageTable(final Descriptor descriptor) {
        final Layer layer = descriptor.getLayer();
        if (!(layer instanceof LayerEntry)) {
            throw new UnsupportedImplementationException("Implémentation non-supportée de la couche.");
        }
        return ((LayerEntry) layer).getGridCoverageTable(descriptor.getOperation());
    }

    /**
     * Retourne l'enveloppe spatio-temporelle des données.
     */
    @Override
    public Envelope getEnvelope() {
        Envelope e = this.envelope;
        if (e == null) {
            try {
                e = data.getEnvelope();
            } catch (CatalogException exception) {
                throw new CannotEvaluateException(exception.getLocalizedMessage(), exception);
            } catch (SQLException exception) {
                throw new CannotEvaluateException(exception.getLocalizedMessage(), exception);
            }
            if (dt != 0) {
                final GeneralEnvelope ge = new GeneralEnvelope(e);
                ge.setRange(2, ge.getMinimum(2)+dt, ge.getMaximum(2)+dt);
                e = ge;
            }
            this.envelope = e;
        }
        return e;
    }

    /**
     * Prépare l'évaluation d'un point.
     */
    private void prepare(final DirectPosition location) throws CatalogException, SQLException, IOException {
        if (position == null) {
            position = new TransformedDirectPosition(null, getCoordinateReferenceSystem(), null);
        }
        try {
            position.transform(location);
        } catch (TransformException e) {
            throw new CatalogException(e);
        }
    }

    /**
     * @todo Copy javadoc from DataConnection
     */
    private double evaluate(final double x, final double y, final double t, final short band)
            throws CatalogException, SQLException, IOException
    {
        // prepare(x, y, t); // TODO
        samples = data.asCoverage().evaluate(position, samples);
        return samples[band];
    }

    /**
     * @todo Copy javadoc from DataConnection
     */
    private double[] snap(final double x, final double y, final double t)
            throws CatalogException, SQLException, IOException
    {
        //prepare(x, y, t); TODO
        ((CoverageStack) data.asCoverage()).snap(position);
        return position.ordinates.clone();
    }

    /**
     * Retourne la valeur à la position spécifiée. Cette méthode délègue le travail à
     * <code>{@linkplain #data}.{@linkplain DataConnection#evaluate evaluate}(<var>x</var>,
     * <var>y</var>, <var>t</var>, <var>band</var>)</code>.
     *
     * @param  position La position à laquelle évaluer le descripteur.
     * @return La valeur évaluée à la position spécifiée.
     * @throws CannotEvaluateException si une erreur est survenue lors de l'évaluation.
     */
    private double evaluateSingle(final DirectPosition position) throws CannotEvaluateException {
        final double x = position.getOrdinate(0) + dx;
        final double y = position.getOrdinate(1) + dy;
        final double t = position.getOrdinate(2) + dt;
        try {
            double v = evaluate(x, y, t, band);
            if (Double.isNaN(v)) {
                for (int f=0; f<fallback.length; f++) {
// TODO             v = fallback[f].evaluate(x, y, t, band);
                    if (!Double.isNaN(v)) break;
                }
            }
            return v;
        } catch (CatalogException exception) {
            throw new CannotEvaluateException(exception.getLocalizedMessage(), exception);
        } catch (SQLException exception) {
            throw new CannotEvaluateException(exception.getLocalizedMessage(), exception);
        } catch (IOException exception) {
            throw new CannotEvaluateException(exception.getLocalizedMessage(), exception);
        }
    }

    /**
     * Retourne la valeur à la position spécifiée. Cette méthode délègue le travail à
     * <code>{@linkplain #data}.{@linkplain DataConnection#evaluate evaluate}(<var>x</var>,
     * <var>y</var>, <var>t</var>, <var>band</var>)</code>.
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
     * Retourne la valeur à la position spécifiée. Cette méthode délègue le travail à
     * <code>{@linkplain #data}.{@linkplain DataConnection#evaluate evaluate}(<var>x</var>,
     * <var>y</var>, <var>t</var>, <var>band</var>)</code>.
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
     * Retourne la valeur à la position spécifiée. Cette méthode délègue le travail à
     * <code>{@linkplain #data}.{@linkplain DataConnection#evaluate evaluate}(<var>x</var>,
     * <var>y</var>, <var>t</var>, <var>band</var>)</code>.
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
     * Retourne la valeur à la position spécifiée. Cette méthode délègue le travail à
     * <code>{@linkplain #data}.{@linkplain DataConnection#evaluate evaluate}(<var>x</var>,
     * <var>y</var>, <var>t</var>, <var>band</var>)</code>.
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
     * Retourne la valeur à la position spécifiée. Cette méthode délègue le travail à
     * <code>{@linkplain #evaluate(DirectPosition,double[]) evaluate}(<var>position</var>,
     * null)</code>.
     *
     * @param  position La position à laquelle évaluer le descripteur.
     * @return La valeur évaluée à la position spécifiée, sous forme de tableau {@code double[]}.
     * @throws CannotEvaluateException si une erreur est survenue lors de l'évaluation.
     */
    public Object evaluate(final DirectPosition position) throws CannotEvaluateException {
        return evaluate(position, (double[]) null);
    }

    /**
     * {@inheritDoc}
     */
    public DirectPosition snap(final DirectPosition position) throws CatalogException {
        final double[] ordinates;
        try {
            ordinates = snap(position.getOrdinate(0) + dx,
                             position.getOrdinate(1) + dy,
                             position.getOrdinate(2) + dt);
        } catch (SQLException exception) {
            throw new ServerException(exception);
        } catch (IOException exception) {
            throw new ServerException(exception);
        }
        return new GeneralDirectPosition(ordinates);
    }

    /**
     * {@inheritDoc}
     */
    public List<Coverage> coveragesAt(DirectPosition position) throws CatalogException {
        if (dt != 0) {
            position = new GeneralDirectPosition(position);
            final int dim = position.getDimension() - 1;
            position.setOrdinate(dim, position.getOrdinate(dim) + dt);
        }
        try {
            prepare(position);
            return ((CoverageStack) data.asCoverage()).coveragesAt(position.getOrdinate(position.getDimension() - 1));
        } catch (SQLException exception) {
            throw new ServerException(exception);
        } catch (IOException exception) {
            throw new ServerException(exception);
        }
    }

    /**
     * Retourne le nombre de bandes dans cette couverture. Pour ce type de couverture, il
     * sera toujours égal à 1.
     */
    public int getNumSampleDimensions() {
        return 1;
    }

    /**
     * Retourne la bande à l'index spécifiée. Comme les couverture de type {@code DataCoverage}
     * n'ont qu'une seule bande, l'argument {@code index} devrait toujours être 0.
     *
     * @throws IndexOutOfBoundsException si {@code index} est en dehors des limites permises.
     */
    public SampleDimension getSampleDimension(final int index) throws IndexOutOfBoundsException {
        if (index != 0) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        return sampleDimension;
    }
}
