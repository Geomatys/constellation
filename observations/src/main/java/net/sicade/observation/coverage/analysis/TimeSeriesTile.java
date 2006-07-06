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
package net.sicade.observation.coverage.analysis;

// J2SE dependencies
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.Writer;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.FieldPosition;
import java.text.NumberFormat;
import javax.imageio.ImageIO;
import javax.media.jai.RasterFactory;

// OpenGIS dependencies
import org.opengis.coverage.Coverage;
import org.opengis.coverage.CannotEvaluateException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

// Geotools dependencies
import org.geotools.coverage.FactoryFinder;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.GeneralGridGeometry;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.util.ProgressListener;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * Un ensemble de {@linkplain TimeSeries s�ries temporelles} ayant des propri�t�s communes.
 * Toutes les s�ries temporelles sont construite � partir du m�me descripteur du paysage oc�anique.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Toura�vane
 */
public class TimeSeriesTile {
    /**
     * Petite valeur pour �viter des erreurs d'arrondissement.
     */
    private static final double EPS = 1e-6;

    /**
     * La source de donn�es tri-dimensionnelle.
     */
    private final Coverage coverage;
    
    /**
     * La transforamtion math�matique qui permet de passer des coordonn�es image aux coordonn�es
     * r�elles.
     */
    private final MathTransform gridToCRS;
    
    /**
     * La fabrique � utiliser pour la cr�ation d'objets {@link GridCoverage2D}.
     */
    private final GridCoverageFactory gridCoverageFactory = FactoryFinder.getGridCoverageFactory(null);
    
    /**
     * L'enveloppe de la grille de destination. Ce n'est pas n�cessairement l'enveloppe de
     * {@link #coverage}, car l'utilisateur peut ne souhaiter extraire que les s�ries temporelles
     * d'une sous-r�gion. En outre, cette enveloppe sera corrig�e pour correspondre � la g�om�trie
     * de la grille de destination. En d'autres termes, le nombre de cellules de la grille de
     * destination sera un nombre entier selon chaque dimension.
     */
    private final GeneralEnvelope envelope;

    /**
     * La bande � prendre en compte dans {@link #coverage}.
     */
    private final int band = 0;

    /**
     * Un tableau contenant toutes les valeurs d'une bande � une position donn�es.
     * N'est conserv� ici qu'afin d'�viter de le recr�er trop souvent.
     */
    private transient double[] samples;

    /**
     * La dimension de {@link TimeSeries#position} que l'on fera varier.
     * Il s'agit habituellement de la dimension temporelle.
     */
    final int varyingDimension;

    /**
     * La taille de la grille de destination selon chacune des dimensions.
     * <code>size[{@linkplain #varyingDimension}]</code> sera la longueur
     * de chaque s�rie temporelle, en nombre de valeurs.
     */
    private final int[] size;

    /**
     * Le pas selon chaque dimension.
     */
    private final double[] step;

    /**
     * Le tableau de s�ries temporelles.
     */
    private final TimeSeries[] series;

    /**
     * Le fichier temporaire qui correspond au canal {@link #channel}.
     * Ce fichier devrait �tre d�truit � la fin du programme.
     */
    private final File file;

    /**
     * Le canal dans lequel lire et �crire les valeurs des s�rie temporelle.
     * Chaque objet {@link TimeSeries} �crira dans une portion diff�rente de
     * ce canal, qui commencera � {@link TimeSeries#base}.
     */
    final FileChannel channel;

    /**
     * {@code true} si {@link #fillSeries} a d�j� �t� appel�e.
     */
    private boolean filled;

    /**
     * Construit un ensemble de s�ries temporelles � partir des donn�es de la couverture sp�cifi�e.
     * Ce constructeur suppose que la dimension temporelle est la derni�re.
     *
     * @param  coverage La couverture � utiliser pour extraire les donn�es.
     * @param  envelope L'enveloppe spatio-temporelle de la zone d'�tude, ou {@code null} pour
     *         prendre la totalit� de l'enveloppe de {@code coverage}.
     * @param  step Le pas selon chacune des dimensions. La longueur de ce tableau doit �tre identique
     *         au nombre de dimensions de l'enveloppe.
     * @throws IOException si une erreur est survenue lors des acc�s disque.
     * @throws TransformException si une erreur est survenue lors d'une transformation de coordonn�es.
     */
    public TimeSeriesTile(final Coverage coverage, final Envelope envelope, final double[] step)
            throws IOException, TransformException
    {
        this(coverage, envelope, step,
             coverage.getCoordinateReferenceSystem().getCoordinateSystem().getDimension()-1);
    }

    /**
     * Construit un ensemble de s�ries temporelles � partir des donn�es de la couverture sp�cifi�e.
     * Un objet {@code TimeSeriesTile} est le r�sultat de l'extraction � partir de la couverture
     * {@code coverage} des valeurs en faisant varier les coordonn�es � la dimension
     * {@code varyingDimension} par le pas d�fini par le tableau {@code step}.
     *
     * @param  coverage La couverture � utiliser pour extraire les donn�es.
     * @param  envelope L'enveloppe spatio-temporelle de la zone d'�tude, ou {@code null} pour
     *         prendre la totalit� de l'enveloppe de {@code coverage}.
     * @param  step Le pas selon chacune des dimensions. La longueur de ce tableau doit �tre identique
     *         au nombre de dimensions de l'enveloppe.
     * @param  varyingDimension La dimension de l'ordonn�e que l'on souhaite faire varier
     *         (habituellement la dimension du temps).
     * @throws IOException si une erreur est survenue lors des acc�s disque.
     * @throws TransformException si une erreur est survenue lors d'une transformation de coordonn�es.
     */
    public TimeSeriesTile(final Coverage coverage, Envelope envelope,
                          double[] step, final int varyingDimension)
            throws IOException, TransformException
    {
        if (envelope == null) {
            envelope = coverage.getEnvelope();
        }
        envelope = this.envelope = new GeneralEnvelope(envelope);
        /*
         * Le nombre de dimensions est d�finit par le syst�me de coordonn�es. On v�rifie
         * que les arguments donn�s par l'utilisateur (notamment 'step') sont compatibles.
         */
        this.coverage         = coverage;
        this.step             = step = (double[]) step.clone();
        this.varyingDimension = varyingDimension;
        final int dimension   = coverage.getCoordinateReferenceSystem().getCoordinateSystem().getDimension();
        if (envelope.getDimension() != dimension) {
            throw new MismatchedDimensionException(Errors.format(ErrorKeys.MISMATCHED_DIMENSION_$3,
                                                   "envelope", envelope.getDimension(), dimension));
        }
        if (step.length != dimension) {
            throw new MismatchedDimensionException(Errors.format(ErrorKeys.MISMATCHED_DIMENSION_$3,
                                                   "step", step.length, dimension));
        }
        if (varyingDimension < 0 || varyingDimension >= dimension) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$2,
                                               "varyingDimension", varyingDimension));
        }
        /*
         * Pour chaque dimension i, "size[i]" contient le nombre d'�l�ments pour cette
         * direction qui se calcule en fonction du pas d�fini pour cette direction. On
         * ajustera l'enveloppe de fa�on � ce qu'elle couvre un nombre entier de ces points.
         *
         * "nSeries" d�finit la taille du tableau "series". C'est un tableau qui contiendra
         * la liste des s�ries temporelles qui nous int�resse, en omettant le nombre de points
         * selon l'axe du temps (puisque ces valeurs seront g�r�es par les objets TimeSeries).
         *
         * Note sur l'usage de Math.floor au lieu de Math.ceil: il ne faut pas compter le nombres
         * de points (cas o� l'on utiliserait Math.ceil), mais plut�t le nombre de cellules (c'est
         * � dire le nombre d'espaces entre les points). Cette nuance vient du fait que la
         * transformation affine 'gridToCRS' fera correspondre les centres des cellules.
         */
        int nSeries = 1;
        size = new int[dimension];
        for (int i=0; i<dimension; i++) {
            final double s   = step[i];
            final int    n   = size[i] = (int) Math.floor(envelope.getLength(i)/s + EPS);
            final double min = envelope.getMinimum(i);
            this.envelope.setRange(i, min, min + n*s);
            if (i != varyingDimension) {
                nSeries *= n;
            }
        }
        series = new TimeSeries[nSeries];
        /*
         * Les �l�ments extraits de la couverture seront stock�s dans un fichier temporaire.
         * Ce dernier sera d�truit lorsque cette instance de TimeSeriesTile sera dispos�e.
         */
        file = File.createTempFile("TimeSeries", ".raw");
        file.deleteOnExit();
        channel = new RandomAccessFile(file, "rw").getChannel();
        /*
         * Obtient la transformation passant des coordonn�es de la grille de destination
         * (� ne pas confondre avec une �ventuelle grille du 'coverage' source) vers les
         * coordonn�es g�ographiques.
         */
        final GeneralGridRange gridRange = new GeneralGridRange(new int[size.length], size);
        final GeneralGridGeometry gridGeometry = new GeneralGridGeometry(gridRange, envelope);
        gridToCRS = gridGeometry.getGridToCoordinateSystem();
        /*
         * Le tableau 'index' sert � simuler un suite de boucles imbriqu�es. Dans le cas o� la
         * dimension est 2, on aurait fait une double boucle imbriqu�e. Dans notre cas, la
         * dimension peut varier et nombre de boucles imbriqu�es ne peut �tre d�fini par avance.
         */
        final int[] index = new int[dimension]; // initialis� � 0
        int count = 0;
        loop:  while (true) {
            // Conversion de l'index en coordonn�es g�ographiques
            final GeneralDirectPosition position = new GeneralDirectPosition(dimension);
            for (int i=0; i<dimension; i++) {
                position.ordinates[i] = index[i];
            }
            final DirectPosition pos = gridToCRS.transform(position, position);
            series[count] = new TimeSeries(this, pos, count);
            count++;
            for (int d=dimension; --d>=0; ) {
                if (d != varyingDimension) {
                    if (++index[d] < size[d]) {
                        continue loop;
                    }
                    index[d] = 0;
                }
            }
            break;
        }
        assert count == nSeries : count;
    }

    /**
     * Evalue une valeur � la position spatio-temporelle sp�cifi�e. Cette m�thode est appel�e
     * automatiquement par les {@linkplain TimeSeries s�ries temporelles} lors de leur remplissage.
     * L'impl�mentation par d�faut d�l�gue l'�valuation � l'objet {@link Coverage} sp�cifi� au
     * constructeur. Red�finissez cette m�thode si les valeurs devraient �tre �valu�es autrement.
     *
     * @param  position La position � laquelle �valuer la couverture.
     * @return La valeur de la couverture � la position sp�cifi�e.
     */
    final double evaluate(final DirectPosition position) {
        try {
            samples = coverage.evaluate(position, samples);
        } catch (CannotEvaluateException exception) {
            Utilities.unexpectedException("net.sicade.observation.coverage",
                                          "TimeSeriesTile", "evaluate", exception);
            return Double.NaN;
        }
        return samples[band];
    }

    /**
     * Remplit le contenu de tous les objets {@link TimeSeries}.
     *
     * @param  listener Objet � utiliser pour informer des progr�s, ou {@code null} si aucun.
     * @throws IOException si une erreur est survenue lors de l'�criture du fichier temporaire.
     */
    private void fillSeries(final ProgressListener listener) throws IOException {
        if (listener != null) {
            listener.setDescription("Chargement des donn�es");
            listener.started();
        }
        final int seriesLength = size[varyingDimension];
        final double timeStep  = step[varyingDimension];
        final double tmin      = envelope.getMinimum(varyingDimension);
        for (int i=0; i<seriesLength; i++) {
            if (listener != null) {
                listener.progress(100f / seriesLength * i);
            }
            final double t = tmin + timeStep*i;
            for (int j=0; j<series.length; j++) {
                series[j].evaluate(t);
            }
        }
        for (int i=0; i<series.length; i++) {
            series[i].flush();
        }
        if (listener != null) {
            listener.complete();
        }
    }

    /**
     * Retourne toutes les s�ries temporelles. Toute modification faites dans ces s�ries (par exemple
     * par un appel � {@link TimeSeries#setData}) seront refl�t�es dans cette collection de s�ries.
     * <p>
     * Cette m�thode proc�dera au chargement des s�ries temporelles la premi�re fois o� elle sera
     * appel�e. Tout appel subs�quent retournera imm�diatement les s�ries temporelles d�j� en m�moire.
     *
     * @param  listener Objet � utiliser pour informer des progr�s, ou {@code null} si aucun.
     * @return Toutes les s�ries temporelles dans cette collection.
     * @throws IOException si une erreur est survenue lors de l'�criture du fichier temporaire.
     */
    public synchronized TimeSeries[] getSeries(final ProgressListener listener) throws IOException {
        if (!filled) {
            fillSeries(listener);
            filled = true;
        }
        return (TimeSeries[]) series.clone();
    }

    /**
     * Retourne le nombre de s�ries temporelles. La valeur retourn�e par cette
     * m�thode doit toujours �tre identique � celle que retournerait
     * <code>{@link #getSeries getSeries}(null).length</code>.
     */
    public final int getSeriesCount() {
        return series.length;
    }

    /**
     * Retourne la longueur de chaque s�ries temporelle, en nombre de donn�es.
     */
    public final int getSeriesLength() {
        return size[varyingDimension];
    }

    /**
     * Retourne la valeur du temps <var>t</var> au pas de temps <var>s</var> sp�cifi�e. L'argument
     * <var>s</var> peut varier de 0 inclusivement jusqu'� {@link #getSeriesLength} exclusivement.
     * Des valeurs de <var>s</var> en dehors de cette plage sont accept�es, mais ne correspondront
     * pas � des donn�es stock�es par {@code TimeSeriesTile}.
     */
    public double getTime(final int s) {
        return envelope.getMinimum(varyingDimension) + step[varyingDimension] * s;
    }

    /**
     * Enregistre une image pour chaque pas de temps. A un pas de temps <var>t</var> donn�,
     * cette m�thode copie les valeurs de toutes les s�ries temporelles dans une image et
     * enregistre cette derni�re.
     *
     * @param  tSubsampling La d�cimation � appliquer selon l'axe du temps. La valeur 1 n'applique aucune d�cimation.
     * @param  xDimension   La dimension � utiliser pour les colonnes de l'image (habituellement 0).
     * @param  yDimension   La dimension � utiliser pour les lignes   de l'image (habituellement 1).
     * @param  listener     Objet � utiliser pour informer des progr�s, ou {@code null} si aucun.
     * @throws IOException si une erreur est survenue lors de la lecture ou de l'�criture sur le disque.
     *
     * @todo Cette m�thode n'est pas encore op�rationnelle.
     */
    public synchronized void writeImages(final int tSubsampling,
                                         final int xDimension,
                                         final int yDimension,
                                         final ProgressListener listener)
            throws IOException
    {
        final TimeSeries[] series = getSeries(listener);
        if (listener != null) {
            listener.setDescription("Cr�ation des images");
            listener.started();
        }
        // le nombre total de positions temporelles = le nombre total d'images pour les series temporelles
        final int seriesLength = size[varyingDimension];
        // le nombre de positions temporelles que l'on va utiliser = le nombre d'images prises en compte.
        final int imageCount   = seriesLength / tSubsampling;
        final WritableRaster raster = RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT,
                                      size[xDimension], size[yDimension], 1, null);
        // remise � z�ro des buffeurs... pour chaque TimeSeries
        for (int i=0; i<series.length; i++) {
            series[i].rewind();
        }
        
        // Pour chaque pas de temps...
        for (int j=0; j<seriesLength; j+=tSubsampling) {
            if (listener != null) {
                listener.progress(100f / imageCount * j);
            }
            // ... pour chaque TimeSeries, on r�cup�re la donn�e correspondant au pas de temps...
            // ... et on �crit celles-ci "dans" le raster
            for (int x=0; x<size[xDimension]; x++) {
                for (int y=0; y<size[yDimension]; y++) {
                    raster.setSample(x, y, 0, series[(x*size[yDimension])+y].next());
                }
            }
            
            final GridSampleDimension[] bands = {((GridSampleDimension) coverage.getSampleDimension(0)).geophysics(true)};
            
            final GridCoverage2D gridCoverage = gridCoverageFactory.create(
                    "immageT" + j, 
                    raster, 
                    coverage.getCoordinateReferenceSystem(), 
                    gridToCRS, 
                    bands);
            
            // cr�ation du png...
            // TODO : trouver un nom plus g�n�rique pour les images... ATTENTION au chemin !!!
            final String fileName = "C:\\Documents and Settings\\Antoine\\Bureau\\TestTimeSeriesTile\\imageT";
            ImageIO.write(
                    gridCoverage.geophysics(false).getRenderedImage(), 
                    "png", 
                    new File(fileName + j + ".png"));
            
        }
        if (listener != null) {
            listener.complete();
        }
    }

    /**
     * Retourne une instance par d�faut de {@link NumberFormat}.
     */
    private static NumberFormat getDefaultFormat() {
        final NumberFormat format;
        format = NumberFormat.getNumberInstance();
        format.setMinimumFractionDigits(6);
        format.setMaximumFractionDigits(6);
        format.setGroupingUsed(false);
        return format;
    }

    /**
     * �crit les coordonn�es de toutes les s�ries temporelles vers le flot sp�cifi�. Cette m�thode
     * est typiquement appel�e avant {@link #writeValues writeValues} pour �crire l'en-t�te.
     * Les colonnes seront s�par�es par des tabulations.
     *
     * @param  out Le flot vers o� �crire les coordonn�es.
     * @param  format Le format � utiliser pour l'�criture des nombres, ou {@code null} pour un
     *         format par d�faut.
     */
    public synchronized void writeCoordinates(final Writer out, NumberFormat format)
            throws IOException
    {
        if (format == null) {
            format = getDefaultFormat();
        }
        // On n'apppele pas 'getSeries(null)' car il n'est pas n�cessaire d'extraire les valeurs.
        final StringBuffer  buffer = new StringBuffer();
        final FieldPosition  dummy = new FieldPosition(0);
        final String lineSeparator = System.getProperty("line.separator", "\n");
        for (int j=0; j<series.length; j++) {
            final DirectPosition position = series[j].position;
            final int dimension = position.getDimension();
            boolean firstColumn = true;
            for (int i=0; i<dimension; i++) {
                if (i != varyingDimension) {
                    if (firstColumn) {
                        firstColumn = false;
                    } else {
                        out.write('\t');
                    }
                    buffer.setLength(0);
                    final String text = format.format(position.getOrdinate(i), buffer, dummy).toString();
                    out.write(text);
                }
            }
            out.write(lineSeparator);
        }
        out.write(lineSeparator);
    }

    /**
     * �crit les valeurs de toutes les s�ries temporelles vers le flot sp�cifi�.
     * La premi�re colonne contiendra les valeurs retourn�es par {@link #getTime}.
     * Les colonnes seront s�par�es par des tabulations.
     *
     * @param  out Le flot vers o� �crire les valeurs.
     * @param  format Le format � utiliser pour l'�criture des nombres, ou {@code null} pour un
     *         format par d�faut.
     * @param  listener Objet � utiliser pour informer des progr�s, ou {@code null} si aucun.
     * @throws IOException si une erreur est survenue lors d'un acc�s disque.
     */
    public synchronized void writeValues(final Writer out, NumberFormat format,
                                         final ProgressListener listener)
            throws IOException
    {
        if (format == null) {
            format = getDefaultFormat();
        }
        final TimeSeries[]  series = getSeries(listener);
        final StringBuffer  buffer = new StringBuffer();
        final FieldPosition  dummy = new FieldPosition(0);
        final String lineSeparator = System.getProperty("line.separator", "\n");
        if (listener != null) {
            listener.setDescription("�criture des valeurs");
            listener.started();
        }
        for (int i=0; i<series.length; i++) {
            series[i].rewind();
        }
        final int seriesLength = getSeriesLength();
        for (int j=0; j<seriesLength; j++) {
            for (int i=-1; i<series.length; i++) {
                final double value;
                if (i < 0) {
                    value = getTime(j);
                } else {
                    value = series[i].next();
                    out.write('\t');
                }
                buffer.setLength(0);
                final String text = format.format(value, buffer, dummy).toString();
                out.write(text);
            }
            if (listener != null) {
                // Note: on affiche les progr�s pour toutes les lignes (plut�t qu'une ligne sur 10
                //       par exemple) parce que chacune de ces lignes peut �tre tr�s longue.
                listener.progress(100f / seriesLength * j);
            }
            out.write(lineSeparator);
        }
        out.flush();
        if (listener != null) {
            listener.complete();
        }
    }
    
    /**
     * Retourne une repr�sentation de cet objet sous forme de cha�ne de caract�res.
     */
    @Override
    public synchronized String toString() {
        if (true) {
            return super.toString();
        }
        // TODO: Il faut n'afficher les donn�es que pour des s�ries courtes.
        //       Il faut aussi faire une m�thode 'write' qui �crit vers un flot.
        final String lineSeparator = System.getProperty("line.separator", "\n");
        final StringBuilder buffer = new StringBuilder();
        for (int i=0; i<series.length; i++) {
            final double[] data;
            try {
                data = series[i].getData(null);
            } catch (IOException e) {
                buffer.append(Utilities.getShortClassName(e));
                final String message = e.getLocalizedMessage();
                if (e != null) {
                    buffer.append(": ");
                    buffer.append(e);
                }
                continue;
            }
            for (int j=0; j<data.length; j++) {
                buffer.append(data[i]);
                buffer.append(' ');
            }
            buffer.append(lineSeparator);
        }
        return buffer.toString();
    }

    /**
     * Lib�re les ressources utilis�es par cette instance. Cette m�thode devrait �tre appel�e
     * lorque cette instance de {@code TimeSeriesTile} et tous les objets {@link TimeSeries}
     * associ�s ne sont plus n�cessaire.
     */
    public void dispose() throws IOException {
        channel.close();
        file.delete();
    }

    /**
     * Lib�re les ressources utilis�es par cette instance.
     */
    @Override
    protected void finalize() throws IOException {
        dispose();
    }
}
