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
package net.sicade.observation.coverage.sql;

// J2SE dependencies
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.sql.SQLException;
import java.rmi.RemoteException;

// OpenGIS dependencies
import org.opengis.coverage.Coverage;
import org.opengis.coverage.SampleDimension;
import org.opengis.coverage.CannotEvaluateException;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.DirectPosition;

// Geotools dependencies
import org.geotools.coverage.AbstractCoverage;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.GeneralDirectPosition;

// Sicade dependencies
import net.sicade.observation.coverage.Format;
import net.sicade.observation.coverage.Series;
import net.sicade.observation.coverage.SubSeries;
import net.sicade.observation.coverage.Operation;
import net.sicade.observation.coverage.Descriptor;
import net.sicade.observation.coverage.LocationOffset;
import net.sicade.observation.coverage.DynamicCoverage;
import net.sicade.observation.coverage.rmi.DataConnection;
import net.sicade.observation.CatalogException;
import net.sicade.observation.ServerException;


/**
 * Expose les donn�es d'un {@linkplain Descriptor descripteur} sous forme d'un objet
 * {@link org.opengis.coverage.Coverage}. Les donn�es peuvent �tre calcul�es sur un
 * serveur distant via une r�f�rence vers un objet {@link DataConnection}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo Les d�placements horizontaux ne sont pas encore impl�ment�s.
 * @todo Les transformations de coordonn�es ne sont prises en compte.
 */
public class DataCoverage extends AbstractCoverage implements DynamicCoverage {
    /**
     * Pour compatibilit�s entre les enregistrements binaires de diff�rentes versions.
     */
    private static final long serialVersionUID = -7029210034299675921L;

    /**
     * Connexion vers la source de donn�es.
     */
    protected final DataConnection data;

    /**
     * Sources de donn�es de second recours, ou un tableau de longeur 0 si aucune.
     */
    private final DataConnection[] fallback;

    /**
     * L'unique bande de cette couverture. Cette bande est d�termin�e d�s la construction afin
     * d'�viter d'avoir � retenir une r�f�rence vers {@link Descriptor}, qui contient des
     * d�pendences qui peuvent �tre assez lourdes.
     */
    private final SampleDimension sampleDimension;

    /**
     * La position spatio-temporelle relative.
     */
    private final double dt;
    private static final double dx=0, dy=0; // TODO: d�placements horizontaux pas encore impl�ment�s.

    /**
     * Le num�ro de bande dans laquelle �valuer les valeurs de pixels des images.
     */
    private final short band;

    /**
     * L'envelope. Sera cach�e apr�s la premi�re invocation de {@link #getEnvelope}.
     */
    private transient Envelope envelope;

    /**
     * Construit une vue vers le descripteur sp�cifi�. La {@linkplain DataConnection connexion
     * vers les donn�es} sera �tablie automatiquement � partir du descripteur sp�cifi�.
     *
     * @param  descriptor Descripteur pour lequel on veut une vue des donn�es.
     * @throws RemoteException si une connexion � un serveur distant a �chou�.
     */
    public DataCoverage(final Descriptor descriptor) throws RemoteException {
        this(descriptor, getDataConnection(descriptor));
    }

    /**
     * Construit une vue vers le descripteur sp�cifi� en utilisant la source de donn�es sp�cifi�e.
     *
     * @param  descriptor Descripteur pour lequel on veut une vue des donn�es.
     * @param  data La source de donn�es � utiliser.
     * @throws RemoteException si une connexion � un serveur distant a �chou�.
     */
    private DataCoverage(final Descriptor descriptor, final DataConnection data) throws RemoteException {
        super(descriptor.getName(), data.getCoordinateReferenceSystem(), null, null);
        final LocationOffset offset = descriptor.getLocationOffset();
        this.data       = data;
        this.dt         = offset.getDayOffset();
        this.band       = descriptor.getBand();
        final Set<SubSeries> subseries = descriptor.getPhenomenon().getSubSeries();
        if (!subseries.isEmpty()) {
            final Format format = subseries.iterator().next().getFormat();
            final SampleDimension[] sd = format.getSampleDimensions();
            sampleDimension = sd[band];
        } else {
            sampleDimension = null;
        }
        if (super.getCoordinateReferenceSystem().equals(net.sicade.observation.sql.CRS.XYT)) {
            throw new UnsupportedOperationException("Transformation de coordonn�es pas encore impl�ment�e.");
        }
        if (offset.getEasting()!=0 || offset.getNorthing()!=0) {
            throw new UnsupportedOperationException("Les d�placements horizontaux ne sont pas encore impl�ment�s.");
        }
        /*
         * Recherche les sources de donn�es de second recours.
         */
        final Operation operation = descriptor.getProcedure();
        final List<DataConnection> fallback = new ArrayList<DataConnection>();
        Series series = descriptor.getPhenomenon();
        while ((series=series.getFallback()) != null) {
            if (series instanceof SeriesEntry) {
                final DataConnection candidate = ((SeriesEntry) series).getDataConnection(operation);
                if (crs.equals(candidate.getCoordinateReferenceSystem())) {
                    fallback.add(candidate);
                    continue;
                }
            }
            Series.LOGGER.warning("S�rie de second recours ignor�e: \""+series.getName() + '"');
        }
        this.fallback = fallback.toArray(new DataConnection[fallback.size()]);
    }

    /**
     * Retourne la connexion vers les donn�es. Ce code devrait appara�tre directement
     * dans le premier constructeur si seulement Sun voulait bien faire le RFE #4093999
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private static DataConnection getDataConnection(final Descriptor descriptor) throws RemoteException {
        final Series series = descriptor.getPhenomenon();
        if (!(series instanceof SeriesEntry)) {
            throw new UnsupportedOperationException("Impl�mentation non-support�e de la s�rie.");
        }
        return ((SeriesEntry) series).getDataConnection(descriptor.getProcedure());
    }

    /**
     * Retourne l'enveloppe spatio-temporelle des donn�es.
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
     * Retourne la valeur � la position sp�cifi�e. Cette m�thode d�l�gue le travail �
     * <code>{@linkplain #data}.{@linkplain DataConnection#evaluate evaluate}(<var>x</var>,
     * <var>y</var>, <var>t</var>, <var>band</var>)</code>.
     *
     * @param  position La position � laquelle �valuer le descripteur.
     * @return La valeur �valu�e � la position sp�cifi�e.
     * @throws CannotEvaluateException si une erreur est survenue lors de l'�valuation.
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
     * Retourne la valeur � la position sp�cifi�e. Cette m�thode d�l�gue le travail �
     * <code>{@linkplain #data}.{@linkplain DataConnection#evaluate evaluate}(<var>x</var>,
     * <var>y</var>, <var>t</var>, <var>band</var>)</code>.
     *
     * @param  position La position � laquelle �valuer le descripteur.
     * @param  samples Un tableau pr�-allou� ou enregistrer le r�sultat, ou {@code null}.
     * @return La valeur �valu�e � la position sp�cifi�e.
     * @throws CannotEvaluateException si une erreur est survenue lors de l'�valuation.
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
     * Retourne la valeur � la position sp�cifi�e. Cette m�thode d�l�gue le travail �
     * <code>{@linkplain #data}.{@linkplain DataConnection#evaluate evaluate}(<var>x</var>,
     * <var>y</var>, <var>t</var>, <var>band</var>)</code>.
     *
     * @param  position La position � laquelle �valuer le descripteur.
     * @param  samples Un tableau pr�-allou� ou enregistrer le r�sultat, ou {@code null}.
     * @return La valeur �valu�e � la position sp�cifi�e.
     * @throws CannotEvaluateException si une erreur est survenue lors de l'�valuation.
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
     * Retourne la valeur � la position sp�cifi�e. Cette m�thode d�l�gue le travail �
     * <code>{@linkplain #data}.{@linkplain DataConnection#evaluate evaluate}(<var>x</var>,
     * <var>y</var>, <var>t</var>, <var>band</var>)</code>.
     *
     * @param  position La position � laquelle �valuer le descripteur.
     * @param  samples Un tableau pr�-allou� ou enregistrer le r�sultat, ou {@code null}.
     * @return La valeur �valu�e � la position sp�cifi�e.
     * @throws CannotEvaluateException si une erreur est survenue lors de l'�valuation.
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
     * Retourne la valeur � la position sp�cifi�e. Cette m�thode d�l�gue le travail �
     * <code>{@linkplain #data}.{@linkplain DataConnection#evaluate evaluate}(<var>x</var>,
     * <var>y</var>, <var>t</var>, <var>band</var>)</code>.
     *
     * @param  position La position � laquelle �valuer le descripteur.
     * @param  samples Un tableau pr�-allou� ou enregistrer le r�sultat, ou {@code null}.
     * @return La valeur �valu�e � la position sp�cifi�e.
     * @throws CannotEvaluateException si une erreur est survenue lors de l'�valuation.
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
     * Retourne la valeur � la position sp�cifi�e. Cette m�thode d�l�gue le travail �
     * <code>{@linkplain #evaluate(DirectPosition,double[]) evaluate}(<var>position</var>,
     * null)</code>.
     *
     * @param  position La position � laquelle �valuer le descripteur.
     * @return La valeur �valu�e � la position sp�cifi�e, sous forme de tableau {@code double[]}.
     * @throws CannotEvaluateException si une erreur est survenue lors de l'�valuation.
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
    public List<Coverage> coveragesAt(final double t) throws CatalogException {
        try {
            return data.coveragesAt(t + dt);
        } catch (SQLException exception) {
            throw new ServerException(exception);
        } catch (IOException exception) {
            throw new ServerException(exception);
        }
    }

    /**
     * Retourne le nombre de bandes dans cette couverture. Pour ce type de couverture, il
     * sera toujours �gal � 1.
     */
    public int getNumSampleDimensions() {
        return 1;
    }

    /**
     * Retourne la bande � l'index sp�cifi�e. Comme les couverture de type {@code DataCoverage}
     * n'ont qu'une seule bande, l'argument {@code index} devrait toujours �tre 0.
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
