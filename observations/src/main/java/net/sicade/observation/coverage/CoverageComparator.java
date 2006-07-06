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

// J2SE dependencies and extensions
import java.util.Comparator;
import java.rmi.RemoteException;
import javax.units.Unit;
import javax.units.NonSI;

// OpenGIS dependencies
import org.opengis.coverage.grid.GridRange;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.spatialschema.geometry.Envelope;

// Geotools dependencies
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.datum.DefaultEllipsoid;

//import org.geotools.resources.CTSUtilities;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.Utilities;


/**
 * Compare deux entr�es {@link CoverageReference} en fonction d'un crit�re arbitraire. Ce
 * comparateur sert � classer un tableau d'images en fonction de leur int�r�t par rapport
 * � ce qui avait �t� demand�. L'impl�mentation par d�faut favorise les images dont la plage
 * de temps couvre le mieux la plage demand�e (les dates de d�but et de fin), et n'examinera
 * la couverture spatiale que si deux images ont une couverture temporelle �quivalente. Cette
 * politique est appropri�e lorsque les images couvrent � peu pr�s la m�me r�gion, et que les
 * dates de ces images est le principal facteur qui varie. Les crit�res de comparaison utilis�s
 * sont:
 * <p>
 * <ul>
 *  <li>Pour chaque image, la quantit� [<i>temps � l'int�rieur de la plage de temps
 *      demand�e</i>]-[<i>temps � l'ext�rieur de la plage de temps demand�</i>] sera
 *      calcul�e. Si une des image � une quantit� plus grande, elle sera choisie.</li>
 *  <li>Sinon, si une image se trouve mieux centr�e sur la plage de temps demand�e, cette
 *      image sera choisie.</li>
 *  <li>Sinon, pour chaque image, l'intersection entre la r�gion de l'image et la r�gion
 *      demand�e sera obtenue, et la superficie de cette intersection calcul�e. Si une
 *      des images obtient une valeur plus grande, cette image sera choisie.</li>
 *  <li>Sinon, la superficie moyenne des pixels des images seront calcul�es. Si une image
 *      a des pixels d'une meilleure r�solution (couvrant une surface plus petite), cette
 *      image sera choisie.</li>
 * </ul>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class CoverageComparator implements Comparator<CoverageReference> {
    /**
     * Object � utiliser pour construire des transformations de coordonn�es.
     */
    private final CoordinateOperationFactory factory = FactoryFinder.getCoordinateOperationFactory(null);

    /**
     * Syst�me de reference des coordonn�es dans lequel faire les comparaisons. Il s'agit
     * du syst�me de coordonn�es de la {@linkplain Series series} d'origine.
     */
    private final CoordinateReferenceSystem crs;

    /**
     * Transformation permettant de passer du syst�me de coordonn�es d'un objet {@link CoverageReference}
     * vers le syst�me de coordonn�es de la {@linkplain Series s�ries} (c'est-�-dire {@link #crs}).
     * Cette transformation est conserv�e dans une cache interne afin d'�viter de construire
     * cet objet trop fr�quement.
     */
    private transient CoordinateOperation transformation;

    /**
     * Ellispo�de � utiliser pour calculer les distances orthodromiques.
     */
    private final DefaultEllipsoid ellipsoid;

    /**
     * Coordonn�es spatio-temporelles demand�es. Ils s'agit des coordonn�es qui avait �t� sp�cifi�e
     * � la {@linkplain Series series}. Cette envelope est exprim�e selon le syst�me de r�f�rence
     * des coordonn�es {@link #crs}. Cette enveloppe n'est pas clon�e. Elle ne doit donc pas �tre
     * modifi�e.
     */
    private final Envelope target;

    /**
     * Une estimation de la superficie de {@code target}.
     * Cette estimation est assez approximative.
     */
    private final double area;

    /**
     * Dimension des axes des <var>x</var> (longitude),
     * <var>y</var> (latitude) et <var>t</var> (temps).
     */
    private final int xDim, yDim, tDim;

    /**
     * Construit un comparateur pour les images de la s�ries sp�cifi�e.
     *
     * @param  Series series d'o� proviennent les images qui seront � comparer.
     * @throws RemoteException si la connexion au serveur a �chou�.
     */
//    public CoverageComparator(final Series series) throws RemoteException {
//        this(series.getCoordinateReferenceSystem(), series.getEnvelope());
//    }

    /**
     * Construit un comparateur avec les plages spatio-temporelles sp�cifi�es.
     *
     * @param crs Syst�me de coordonn�es dans lequel comparer les images.
     * @param envelope Coordonn�es spatio-temporelle de la r�gion qui avait �t� demand�e.
     *        Ces coordonn�es doivent �tre exprim�es selon le syst�me de coordonn�es {@code crs}.
     */
    public CoverageComparator(final CoordinateReferenceSystem crs, final Envelope envelope) {
        int xDim = -1;
        int yDim = -1;
        int tDim = -1;
        final CoordinateSystem cs = crs.getCoordinateSystem();
        for (int i=cs.getDimension(); --i>=0;) {
            final AxisDirection orientation = cs.getAxis(i).getDirection().absolute();
            if (orientation.equals(AxisDirection.EAST  )) xDim = i;
            if (orientation.equals(AxisDirection.NORTH )) yDim = i;
            if (orientation.equals(AxisDirection.FUTURE)) tDim = i;
        }
        this.xDim      = xDim;
        this.yDim      = yDim;
        this.tDim      = tDim;
        this.crs       = crs;
        this.target    = envelope;
        this.ellipsoid = DefaultEllipsoid.wrap(CRSUtilities.getEllipsoid(crs));
        this.area      = getArea(target);
    }

    /**
     * Retourne une estimation de la superficie occup�e par
     * la composante horizontale de l'envelope sp�cifi�e.
     */
    private double getArea(final Envelope envelope) {
        if (xDim<0 || yDim<0) {
            return Double.NaN;
        }
        return getArea(envelope.getMinimum(xDim), envelope.getMinimum(yDim),
                       envelope.getMaximum(xDim), envelope.getMaximum(yDim));
    }

    /**
     * Retourne une estimation de la superficie occup�e par
     * un rectangle d�limit�e par les coordonn�es sp�cifi�es.
     */
    private double getArea(double xmin, double ymin, double xmax, double ymax) {
        final CoordinateSystem cs = crs.getCoordinateSystem();
        final Unit xUnit = cs.getAxis(xDim).getUnit();
        final Unit yUnit = cs.getAxis(yDim).getUnit();
        xmin = xUnit.getConverterTo(NonSI.DEGREE_ANGLE).convert(xmin);
        xmax = xUnit.getConverterTo(NonSI.DEGREE_ANGLE).convert(xmax);
        ymin = yUnit.getConverterTo(NonSI.DEGREE_ANGLE).convert(ymin);
        ymax = yUnit.getConverterTo(NonSI.DEGREE_ANGLE).convert(ymax);

        if (xmin<xmax && ymin<ymax) {
            final double centerX = 0.5*(xmin+xmax);
            final double centerY = 0.5*(ymin+ymax);
            final double   width = ellipsoid.orthodromicDistance(xmin, centerY, xmax, centerY);
            final double  height = ellipsoid.orthodromicDistance(centerX, ymin, centerX, ymax);
            return width*height;
        } else {
            return 0;
        }
    }

    /**
     * Compare deux objets {@link CoverageReference}. Les classes d�riv�es peuvent
     * red�finir cette m�thode pour d�finir un autre crit�re de comparaison
     * que les crit�res par d�faut.
     *
     * @return +1 si l'image {@code entry1} repr�sente le plus grand int�r�t,
     *         -1 si l'image {@code entry2} repr�sente le plus grand int�r�t, ou
     *          0 si les deux images repr�sentent le m�me int�r�t.
     */
    public int compare(final CoverageReference entry1, final CoverageReference entry2) {
        final Evaluator ev1 = evaluator(entry1);
        final Evaluator ev2 = evaluator(entry2);
        if (ev1==null) return (ev2==null) ? 0 : +1;
        if (ev2==null) return                   -1;
        double t1, t2;

        t1 = ev1.uncoveredTime();
        t2 = ev2.uncoveredTime();
        if (t1 > t2) return +1;
        if (t1 < t2) return -1;

        t1 = ev1.timeOffset();
        t2 = ev2.timeOffset();
        if (t1 > t2) return +1;
        if (t1 < t2) return -1;

        t1 = ev1.uncoveredArea();
        t2 = ev2.uncoveredArea();
        if (t1 > t2) return +1;
        if (t1 < t2) return -1;

        t1 = ev1.resolution();
        t2 = ev2.resolution();
        if (t1 > t2) return +1;
        if (t1 < t2) return -1;

        return 0;
    }

    /**
     * Retourne un objet {@link Evaluator} pour l'image sp�cifi�e. Cette m�thode est
     * habituellement appel�e au d�but de {@link #compare},  afin d'obtenir une aide
     * pour comparer les images. Si cette m�thode n'a pas pu construire un
     * {@code Evaluator}, alors elle retourne {@code null}.
     */
    protected Evaluator evaluator(final CoverageReference entry) {
        try {
            return new Evaluator(entry);
        } catch (FactoryException exception) {
            unexpectedException(exception);
            return null;
        } catch (TransformException exception) {
            unexpectedException(exception);
            return null;
        } catch (RemoteException exception) {
            unexpectedException(exception);
            return null;
        }
    }

    /**
     * Signale qu'une exception inatendue est survenue lors de l'ex�cution de {@link #evaluator}.
     */
    private static void unexpectedException(final Exception exception) {
        Utilities.unexpectedException("net.sicade.observation.coverage",
                                      "CoverageComparator",
                                      "evaluator", exception);
    }

    /**
     * Evalue la qualit� de la couverture d'une image par rapport � ce qui a �t�
     * demand�e. En g�n�ral, deux instances de cette classe seront construites �
     * l'int�rieur de la m�thode {@link CoverageComparator#compare}. Les m�thodes
     * de {@code Evaluator} seront ensuite appel�es  (dans un ordre choisit
     * par {@link CoverageComparator#compare}) afin de d�terminer laquelle des deux
     * images correspond le mieux � ce que l'utilisateur a demand�.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    protected class Evaluator {
        /**
         * Coordonn�es spatio-temporelle d'une image. Il s'agit des coordonn�es de l'objet
         * {@link CoverageReference} en cours de comparaison. Ces coordonn�es doivent avoir
         * �t� transform�es selon le syst�me de coordonn�es {@link CoverageComparator#crs}.
         */
        private final Envelope source;

        /**
         * Largeur et hauteur de l'image en nombre de pixels, dans l'exe
         * Est-Ouest ({@code width}) ou Nord-Sud ({@code height}).
         */
        private final int width, height;

        /**
         * Construit un �valuateur pour l'image sp�cifi�e.
         *
         * @param  entry L'image qui sera a �valuer.
         * @throws RemoteException si la connexion au serveur a �chou�.
         * @throws TransformException si une transformation �tait
         *         n�cessaire et n'a pas pu �tre effectu�e.
         */
        public Evaluator(final CoverageReference entry)
                throws RemoteException, FactoryException, TransformException
        {
            Envelope envelope = entry.getEnvelope();
            CoordinateReferenceSystem sourceCRS = entry.getCoordinateReferenceSystem();
            if (!CRSUtilities.equalsIgnoreMetadata(crs, sourceCRS)) {
                if (transformation == null ||
                    !CRSUtilities.equalsIgnoreMetadata(transformation.getSourceCRS(), sourceCRS))
                {
                    transformation = factory.createOperation(sourceCRS, crs);
                }
                final MathTransform transform = transformation.getMathTransform();
                if (!transform.isIdentity()) {
                    envelope = CRSUtilities.transform(transform, envelope);
                }
            }
            this.source = envelope;
            int xDim = -1;
            int yDim = -1;
            final CoordinateSystem cs = sourceCRS.getCoordinateSystem();
            for (int i=cs.getDimension(); --i>=0;) {
                final AxisDirection orientation = cs.getAxis(i).getDirection().absolute();
                if (orientation.equals(AxisDirection.EAST  )) xDim = i;
                if (orientation.equals(AxisDirection.NORTH )) yDim = i;
            }
            final GridRange range = entry.getGridGeometry().getGridRange();
            width  = (xDim>=0) ? range.getLength(xDim) : 0;
            height = (yDim>=0) ? range.getLength(yDim) : 0;
        }

        /**
         * Retourne une mesure de la correspondance entre la plage de temps couverte par l'image
         * et la plage de temps qui avait �t� demand�e.  Une valeur de 0 indique que la plage de
         * l'image correspond exactement � la plage demand�e.  Une valeur sup�rieure � 0 indique
         * que l'image ne couvre pas toute la plage demand�e,   o� qu'elle couvre aussi du temps
         * en dehors de la plage demand�e.
         */
        public double uncoveredTime() {
            if (tDim<0) return Double.NaN;
            final double srcMin = source.getMinimum(tDim);
            final double srcMax = source.getMaximum(tDim);
            final double dstMin = target.getMinimum(tDim);
            final double dstMax = target.getMaximum(tDim);
            final double lower  = Math.max(srcMin, dstMin);
            final double upper  = Math.min(srcMax, dstMax);
            final double range  = Math.max(0, upper-lower); // Find intersection range.
            return ((dstMax-dstMin) - range) +  // > 0 if image do not cover all requested range.
                   ((srcMax-srcMin) - range);   // > 0 if image cover some part outside requested range.
        }

        /**
         * Retourne une mesure de l'�cart entre la date de l'image et la date demand�e.
         * Une valeur de 0 indique que l'image est exactement centr�e sur la plage de
         * dates demand�e. Une valeur sup�rieure � 0 indique que le centre de l'image
         * est d�cal�e.
         */
        public double timeOffset() {
            if (tDim<0) {
                return Double.NaN;
            }
            return Math.abs(source.getCenter(tDim)-target.getCenter(tDim));
        }

        /**
         * Retourne une mesure de la correspondance entre la r�gion g�ographique couverte
         * par l'image et la r�gion qui avait �t� demand�e. Une valeur de 0 indique que
         * l'image couvre au moins la totalit� de la r�gion demand�e, tandis qu'une valeur
         * sup�rieure � 0 indique que certaines r�gions ne sont pas couvertes.
         */
        public double uncoveredArea() {
            if (xDim<0 || yDim<0) return Double.NaN;
            return area - getArea(Math.max(source.getMinimum(xDim), target.getMinimum(xDim)),
                                  Math.max(source.getMinimum(yDim), target.getMinimum(yDim)),
                                  Math.min(source.getMaximum(xDim), target.getMaximum(xDim)),
                                  Math.min(source.getMaximum(yDim), target.getMaximum(yDim)));
        }

        /**
         * Retourne une estimation de la superficie occup�e par les pixels.
         * Une valeur de 0 signifierait qu'une image � une pr�cision infinie...
         */
        public double resolution() {
            final int num = width*height;
            return (num>0) ? getArea(source)/num : Double.NaN;
        }
    }
}
