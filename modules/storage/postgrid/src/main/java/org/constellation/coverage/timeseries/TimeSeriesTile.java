/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.coverage.timeseries;

import java.awt.image.DataBuffer;
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

import org.opengis.util.ProgressListener;
import org.opengis.coverage.Coverage;
import org.opengis.coverage.CannotEvaluateException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.MismatchedDimensionException;

import org.geotools.util.logging.Logging;
import org.geotools.util.SimpleInternationalString;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.GeneralGridGeometry;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * Un ensemble de {@linkplain TimeSeries séries temporelles} ayant des propriétés communes.
 * Toutes les séries temporelles sont construite à partir du même descripteur du paysage océanique.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class TimeSeriesTile {
    /**
     * Petite valeur pour éviter des erreurs d'arrondissement.
     */
    private static final double EPS = 1e-6;

    /**
     * La source de données tri-dimensionnelle.
     */
    private final Coverage coverage;

    /**
     * La transforamtion mathématique qui permet de passer des coordonnées image aux coordonnées
     * réelles.
     */
    private final MathTransform gridToCRS;

    /**
     * La fabrique à utiliser pour la création d'objets {@link GridCoverage2D}.
     */
    private final GridCoverageFactory gridCoverageFactory = CoverageFactoryFinder.getGridCoverageFactory(null);

    /**
     * L'enveloppe de la grille de destination. Ce n'est pas nécessairement l'enveloppe de
     * {@link #coverage}, car l'utilisateur peut ne souhaiter extraire que les séries temporelles
     * d'une sous-région. En outre, cette enveloppe sera corrigée pour correspondre à la géométrie
     * de la grille de destination. En d'autres termes, le nombre de cellules de la grille de
     * destination sera un nombre entier selon chaque dimension.
     */
    private final GeneralEnvelope envelope;

    /**
     * La bande à prendre en compte dans {@link #coverage}.
     */
    private final int band = 0;

    /**
     * Un tableau contenant toutes les valeurs d'une bande à une position données.
     * N'est conservé ici qu'afin d'éviter de le recréer trop souvent.
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
     * de chaque série temporelle, en nombre de valeurs.
     */
    private final int[] size;

    /**
     * Le pas selon chaque dimension.
     */
    private final double[] step;

    /**
     * Le tableau de séries temporelles.
     */
    private final TimeSeries[] layer;

    /**
     * Le fichier temporaire qui correspond au canal {@link #channel}.
     * Ce fichier devrait être détruit à la fin du programme.
     */
    private final File file;

    /**
     * Le canal dans lequel lire et écrire les valeurs des séries temporelles.
     * Chaque objet {@link TimeSeries} écrira dans une portion différente de
     * ce canal, qui commencera à {@link TimeSeries#base}.
     */
    final FileChannel channel;

    /**
     * {@code true} si {@link #fillSeries} a déjà été appelée.
     */
    private boolean filled;

    /**
     * Construit un ensemble de séries temporelles à partir des données de la couverture spécifiée.
     * Ce constructeur suppose que la dimension temporelle est la dernière.
     *
     * @param  coverage La couverture à utiliser pour extraire les données.
     * @param  envelope L'enveloppe spatio-temporelle de la zone d'étude, ou {@code null} pour
     *         prendre la totalité de l'enveloppe de {@code coverage}.
     * @param  step Le pas selon chacune des dimensions. La longueur de ce tableau doit être identique
     *         au nombre de dimensions de l'enveloppe.
     * @throws IOException si une erreur est survenue lors des accès disque.
     * @throws TransformException si une erreur est survenue lors d'une transformation de coordonnées.
     */
    public TimeSeriesTile(final Coverage coverage, final Envelope envelope, final double[] step)
            throws IOException, TransformException
    {
        this(coverage, envelope, step,
             coverage.getCoordinateReferenceSystem().getCoordinateSystem().getDimension()-1);
    }

    /**
     * Construit un ensemble de séries temporelles à partir des données de la couverture spécifiée.
     * Un objet {@code TimeSeriesTile} est le résultat de l'extraction à partir de la couverture
     * {@code coverage} des valeurs en faisant varier les coordonnées à la dimension
     * {@code varyingDimension} par le pas défini par le tableau {@code step}.
     *
     * @param  coverage La couverture à utiliser pour extraire les données.
     * @param  envelope L'enveloppe spatio-temporelle de la zone d'étude, ou {@code null} pour
     *         prendre la totalité de l'enveloppe de {@code coverage}.
     * @param  step Le pas selon chacune des dimensions. La longueur de ce tableau doit être identique
     *         au nombre de dimensions de l'enveloppe.
     * @param  varyingDimension La dimension de l'ordonnée que l'on souhaite faire varier
     *         (habituellement la dimension du temps).
     * @throws IOException si une erreur est survenue lors des accès disque.
     * @throws TransformException si une erreur est survenue lors d'une transformation de coordonnées.
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
         * Le nombre de dimensions est définit par le système de coordonnées. On vérifie
         * que les arguments donnés par l'utilisateur (notamment 'step') sont compatibles.
         */
        this.coverage         = coverage;
        this.step             = step = step.clone();
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
         * Pour chaque dimension i, "size[i]" contient le nombre d'éléments pour cette
         * direction qui se calcule en fonction du pas défini pour cette direction. On
         * ajustera l'enveloppe de façon à ce qu'elle couvre un nombre entier de ces points.
         *
         * "nSeries" définit la taille du tableau "layer". C'est un tableau qui contiendra
         * la liste des séries temporelles qui nous intéressent, en omettant le nombre de points
         * selon l'axe du temps (puisque ces valeurs seront gérées par les objets TimeSeries).
         *
         * Note sur l'usage de Math.floor au lieu de Math.ceil: il ne faut pas compter le nombres
         * de points (cas où l'on utiliserait Math.ceil), mais plutôt le nombre de cellules (c'est
         * à dire le nombre d'espaces entre les points). Cette nuance vient du fait que la
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
        layer = new TimeSeries[nSeries];
        /*
         * Les éléments extraits de la couverture seront stockés dans un fichier temporaire.
         * Ce dernier sera détruit lorsque cette instance de TimeSeriesTile sera disposée.
         */
        file = File.createTempFile("TimeSeries", ".raw");
        file.deleteOnExit();
        channel = new RandomAccessFile(file, "rw").getChannel();
        /*
         * Obtient la transformation passant des coordonnées de la grille de destination
         * (à ne pas confondre avec une éventuelle grille du 'coverage' source) vers les
         * coordonnées géographiques.
         */
        final GeneralGridRange gridRange = new GeneralGridRange(new int[size.length], size);
        final GeneralGridGeometry gridGeometry = new GeneralGridGeometry(gridRange, envelope);
        gridToCRS = gridGeometry.getGridToCRS();
        /*
         * Le tableau 'index' sert à simuler un suite de boucles imbriquées. Dans le cas où la
         * dimension est 2, on aurait fait une double boucle imbriquée. Dans notre cas, la
         * dimension peut varier et nombre de boucles imbriquées ne peut être défini par avance.
         */
        final int[] index = new int[dimension]; // initialisé à 0
        int count = 0;
        loop:  while (true) {
            // Conversion de l'index en coordonnées géographiques
            final GeneralDirectPosition position = new GeneralDirectPosition(dimension);
            for (int i=0; i<dimension; i++) {
                position.ordinates[i] = index[i];
            }
            final DirectPosition pos = gridToCRS.transform(position, position);
            layer[count] = new TimeSeries(this, pos, count);
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
     * Evalue une valeur à la position spatio-temporelle spécifiée. Cette méthode est appelée
     * automatiquement par les {@linkplain TimeSeries séries temporelles} lors de leur remplissage.
     * L'implémentation par défaut délègue l'évaluation à l'objet {@link Coverage} spécifié au
     * constructeur. Redéfinissez cette méthode si les valeurs devraient être évaluées autrement.
     *
     * @param  position La position à laquelle évaluer la couverture.
     * @return La valeur de la couverture à la position spécifiée.
     */
    final double evaluate(final DirectPosition position) {
        try {
            samples = coverage.evaluate(position, samples);
        } catch (CannotEvaluateException exception) {
            Logging.unexpectedException(TimeSeriesTile.class, "evaluate", exception);
            return Double.NaN;
        }
        return samples[band];
    }

    /**
     * Remplit le contenu de tous les objets {@link TimeSeries}.
     *
     * @param  listener Objet à utiliser pour informer des progrès, ou {@code null} si aucun.
     * @throws IOException si une erreur est survenue lors de l'écriture du fichier temporaire.
     */
    private void fillSeries(final ProgressListener listener) throws IOException {
        if (listener != null) {
            listener.setTask(new SimpleInternationalString("Chargement des données"));
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
            for (int j=0; j<layer.length; j++) {
                layer[j].evaluate(t);
            }
        }
        for (int i=0; i<layer.length; i++) {
            layer[i].flush();
        }
        if (listener != null) {
            listener.complete();
        }
    }

    /**
     * Retourne toutes les séries temporelles. Toute modification faites dans ces séries (par exemple
     * par un appel à {@link TimeSeries#setData}) seront reflètées dans cette collection de séries.
     * <p>
     * Cette méthode procèdera au chargement des séries temporelles la première fois où elle sera
     * appelée. Tout appel subséquent retournera immédiatement les séries temporelles déjà en mémoire.
     *
     * @param  listener Objet à utiliser pour informer des progrès, ou {@code null} si aucun.
     * @return Toutes les séries temporelles dans cette collection.
     * @throws IOException si une erreur est survenue lors de l'écriture du fichier temporaire.
     */
    public synchronized TimeSeries[] getLayer(final ProgressListener listener) throws IOException {
        if (!filled) {
            fillSeries(listener);
            filled = true;
        }
        return layer.clone();
    }

    /**
     * Retourne le nombre de séries temporelles. La valeur retournée par cette
     * méthode doit toujours être identique à celle que retournerait
     * <code>{@link #getSeries getSeries}(null).length</code>.
     */
    public final int getSeriesCount() {
        return layer.length;
    }

    /**
     * Retourne la longueur de chaque série temporelle, en nombre de données.
     */
    public final int getSeriesLength() {
        return size[varyingDimension];
    }

    /**
     * Retourne la valeur du temps <var>t</var> au pas de temps <var>s</var> spécifiée. L'argument
     * <var>s</var> peut varier de 0 inclusivement jusqu'à {@link #getSeriesLength} exclusivement.
     * Des valeurs de <var>s</var> en dehors de cette plage sont acceptées, mais ne correspondront
     * pas à des données stockées par {@code TimeSeriesTile}.
     */
    public double getTime(final int s) {
        return envelope.getMinimum(varyingDimension) + step[varyingDimension] * s;
    }

    /**
     * Enregistre une image pour chaque pas de temps. A un pas de temps <var>t</var> donné,
     * cette méthode copie les valeurs de toutes les séries temporelles dans une image et
     * enregistre cette dernière.
     *
     * @param  tSubsampling La décimation à appliquer selon l'axe du temps. La valeur 1 n'applique aucune décimation.
     * @param  xDimension   La dimension à utiliser pour les colonnes de l'image (habituellement 0).
     * @param  yDimension   La dimension à utiliser pour les lignes   de l'image (habituellement 1).
     * @param  listener     Objet à utiliser pour informer des progrès, ou {@code null} si aucun.
     * @throws IOException si une erreur est survenue lors de la lecture ou de l'écriture sur le disque.
     *
     * @todo Cette méthode n'est pas encore opérationnelle.
     */
    public synchronized void writeImages(final int tSubsampling,
                                         final int xDimension,
                                         final int yDimension,
                                         final ProgressListener listener)
            throws IOException
    {
        final TimeSeries[] layer = getLayer(listener);
        if (listener != null) {
            listener.setTask(new SimpleInternationalString("Création des images"));
            listener.started();
        }
        // le nombre total de positions temporelles = le nombre total d'images pour les couche temporelles
        final int seriesLength = size[varyingDimension];
        // le nombre de positions temporelles que l'on va utiliser = le nombre d'images prises en compte.
        final int imageCount   = seriesLength / tSubsampling;
        final WritableRaster raster = RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT,
                                      size[xDimension], size[yDimension], 1, null);
        // remise à zéro des buffeurs... pour chaque TimeSeries
        for (int i=0; i<layer.length; i++) {
            layer[i].rewind();
        }

        // Pour chaque pas de temps...
        for (int j=0; j<seriesLength; j+=tSubsampling) {
            if (listener != null) {
                listener.progress(100f / imageCount * j);
            }
            // ... pour chaque TimeSeries, on récupère la donnée correspondant au pas de temps...
            // ... et on écrit celles-ci "dans" le raster
            for (int x=0; x<size[xDimension]; x++) {
                for (int y=0; y<size[yDimension]; y++) {
                    raster.setSample(x, y, 0, layer[(x*size[yDimension])+y].next());
                }
            }

            final GridSampleDimension[] bands = {((GridSampleDimension) coverage.getSampleDimension(0)).geophysics(true)};

            final GridCoverage2D gridCoverage = gridCoverageFactory.create(
                    "immageT" + j,
                    raster,
                    coverage.getCoordinateReferenceSystem(),
                    gridToCRS,
                    bands);

            // création du png...
            // TODO : trouver un nom plus générique pour les images... ATTENTION au chemin !!!
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
     * Retourne une instance par défaut de {@link NumberFormat}.
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
     * Écrit les coordonnées de toutes les séries temporelles vers le flot spécifié. Cette méthode
     * est typiquement appelée avant {@link #writeValues writeValues} pour écrire l'en-tête.
     * Les colonnes seront séparées par des tabulations.
     *
     * @param  out Le flot vers où écrire les coordonnées.
     * @param  format Le format à utiliser pour l'écriture des nombres, ou {@code null} pour un
     *         format par défaut.
     */
    public synchronized void writeCoordinates(final Writer out, NumberFormat format)
            throws IOException
    {
        if (format == null) {
            format = getDefaultFormat();
        }
        // On n'apppele pas 'getSeries(null)' car il n'est pas nécessaire d'extraire les valeurs.
        final StringBuffer  buffer = new StringBuffer();
        final FieldPosition  dummy = new FieldPosition(0);
        final String lineSeparator = System.getProperty("line.separator", "\n");
        for (int j=0; j<layer.length; j++) {
            final DirectPosition position = layer[j].position;
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
     * Écrit les valeurs de toutes les séries temporelles vers le flot spécifié.
     * La première colonne contiendra les valeurs retournées par {@link #getTime}.
     * Les colonnes seront séparées par des tabulations.
     *
     * @param  out Le flot vers où écrire les valeurs.
     * @param  format Le format à utiliser pour l'écriture des nombres, ou {@code null} pour un
     *         format par défaut.
     * @param  listener Objet à utiliser pour informer des progrès, ou {@code null} si aucun.
     * @throws IOException si une erreur est survenue lors d'un accès disque.
     */
    public synchronized void writeValues(final Writer out, NumberFormat format,
                                         final ProgressListener listener)
            throws IOException
    {
        if (format == null) {
            format = getDefaultFormat();
        }
        final TimeSeries[]  layer  = getLayer(listener);
        final StringBuffer  buffer = new StringBuffer();
        final FieldPosition dummy  = new FieldPosition(0);
        final String lineSeparator = System.getProperty("line.separator", "\n");
        if (listener != null) {
            listener.setTask(new SimpleInternationalString("Écriture des valeurs"));
            listener.started();
        }
        for (int i=0; i<layer.length; i++) {
            layer[i].rewind();
        }
        final int seriesLength = getSeriesLength();
        for (int j=0; j<seriesLength; j++) {
            for (int i=-1; i<layer.length; i++) {
                final double value;
                if (i < 0) {
                    value = getTime(j);
                } else {
                    value = layer[i].next();
                    out.write('\t');
                }
                buffer.setLength(0);
                final String text = format.format(value, buffer, dummy).toString();
                out.write(text);
            }
            if (listener != null) {
                // Note: on affiche les progrès pour toutes les lignes (plutôt qu'une ligne sur 10
                //       par exemple) parce que chacune de ces lignes peut être très longue.
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
     * Retourne une représentation de cet objet sous forme de chaîne de caractères.
     */
    @Override
    public synchronized String toString() {
        if (true) {
            return super.toString();
        }
        // TODO: Il faut n'afficher les données que pour des séries courtes.
        //       Il faut aussi faire une méthode 'write' qui écrit vers un flot.
        final String lineSeparator = System.getProperty("line.separator", "\n");
        final StringBuilder buffer = new StringBuilder();
        for (int i=0; i<layer.length; i++) {
            final double[] data;
            try {
                data = layer[i].getData(null);
            } catch (IOException e) {
                buffer.append(e);
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
     * Libère les ressources utilisées par cette instance. Cette méthode devrait être appelée
     * lorque cette instance de {@code TimeSeriesTile} et tous les objets {@link TimeSeries}
     * associés ne sont plus nécessaire.
     */
    public void dispose() throws IOException {
        channel.close();
        file.delete();
    }

    /**
     * Libère les ressources utilisées par cette instance.
     */
    @Override
    protected void finalize() throws IOException {
        dispose();
    }
}
