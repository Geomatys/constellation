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
package net.sicade.coverage.catalog;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.sql.SQLException;
import java.rmi.RemoteException;

import org.opengis.coverage.Coverage;
import org.opengis.coverage.SampleDimension;
import org.opengis.coverage.CannotEvaluateException;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.DirectPosition;
import org.geotools.coverage.AbstractCoverage;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.GeneralDirectPosition;

import net.sicade.coverage.model.Operation;
import net.sicade.coverage.model.Descriptor;
import net.sicade.coverage.model.RegionOfInterest;
import net.sicade.catalog.CatalogException;
import net.sicade.catalog.ServerException;


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
 */
public class DataCoverage extends AbstractCoverage implements GridCoverage {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = -7029210034299675921L;

    /**
     * Connexion vers la source de données.
     */
    private final DataConnection data;

    /**
     * Sources de données de second recours, ou un tableau de longeur 0 si aucune.
     */
    private final DataConnection[] fallback;

    /**
     * L'unique bande de cette couverture. Cette bande est déterminée dès la construction afin
     * d'éviter d'avoir à retenir une référence vers {@link Descriptor}, qui contient des
     * dépendences qui peuvent être assez lourdes.
     */
    private final GridSampleDimension sampleDimension;

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
     * @throws RemoteException si une connexion à un serveur distant a échoué.
     */
    public DataCoverage(final Descriptor descriptor) throws RemoteException {
        this(descriptor, getDataConnection(descriptor));
    }

    /**
     * Construit une vue vers le descripteur spécifié en utilisant la source de données spécifiée.
     *
     * @param  descriptor Descripteur pour lequel on veut une vue des données.
     * @param  data La source de données à utiliser.
     * @throws RemoteException si une connexion à un serveur distant a échoué.
     */
    private DataCoverage(final Descriptor descriptor, final DataConnection data) throws RemoteException {
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
        if (super.getCoordinateReferenceSystem().equals(net.sicade.catalog.CRS.XYT)) {
            throw new UnsupportedOperationException("Transformation de coordonnées pas encore implémentée.");
        }
        if (offset.getEasting()!=0 || offset.getNorthing()!=0) {
            throw new UnsupportedOperationException("Les déplacements horizontaux ne sont pas encore implémentés.");
        }
        /*
         * Recherche les sources de données de second recours.
         */
        final Operation operation = descriptor.getOperation();
        final List<DataConnection> fallback = new ArrayList<DataConnection>();
        Layer layer = descriptor.getLayer();
        while ((layer=layer.getFallback()) != null) {
            if (layer instanceof LayerEntry) {
                final DataConnection candidate = ((LayerEntry) layer).getDataConnection(operation);
                if (crs.equals(candidate.getCoordinateReferenceSystem())) {
                    fallback.add(candidate);
                    continue;
                }
            }
            Layer.LOGGER.warning("Couche de second recours ignorée: \""+layer.getName() + '"');
        }
        this.fallback = fallback.toArray(new DataConnection[fallback.size()]);
    }

    /**
     * Retourne la connexion vers les données. Ce code devrait apparaître directement
     * dans le premier constructeur si seulement Sun voulait bien faire le RFE #4093999
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private static DataConnection getDataConnection(final Descriptor descriptor) throws RemoteException {
        final Layer layer = descriptor.getLayer();
        if (!(layer instanceof LayerEntry)) {
            throw new UnsupportedOperationException("Implémentation non-supportée de la couche.");
        }
        return ((LayerEntry) layer).getDataConnection(descriptor.getOperation());
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
            } catch (RemoteException exception) {
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
            double v = data.evaluate(x, y, t, band);
            if (Double.isNaN(v)) {
                for (int f=0; f<fallback.length; f++) {
                    v = fallback[f].evaluate(x, y, t, band);
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
    @Override
    public Object evaluate(final DirectPosition position) throws CannotEvaluateException {
        return evaluate(position, (double[]) null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DirectPosition snap(final DirectPosition position) throws CatalogException {
        final double[] ordinates;
        try {
            ordinates = data.snap(position.getOrdinate(0) + dx,
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
    @Override
    public List<Coverage> coveragesAt(DirectPosition position) throws CatalogException {
        if (dt != 0) {
            position = new GeneralDirectPosition(position);
            final int dim = position.getDimension() - 1;
            position.setOrdinate(dim, position.getOrdinate(dim) + dt);
        }
        try {
            return data.coveragesAt(position);
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
    @Override
    public int getNumSampleDimensions() {
        return 1;
    }

    /**
     * Retourne la bande à l'index spécifiée. Comme les couverture de type {@code DataCoverage}
     * n'ont qu'une seule bande, l'argument {@code index} devrait toujours être 0.
     *
     * @throws IndexOutOfBoundsException si {@code index} est en dehors des limites permises.
     */
    @Override
    public SampleDimension getSampleDimension(final int index) throws IndexOutOfBoundsException {
        if (index != 0) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        return sampleDimension;
    }
}
