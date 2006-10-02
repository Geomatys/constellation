/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 2006, Institut de Recherche pour le D�veloppement
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
package net.sicade.image.io.netcdf;

// JS2E dependencies
import java.util.List;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;

// NetCDF dependencies
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import static org.geotools.resources.i18n.ErrorKeys.*;

// Sicade dependencies
import net.sicade.image.io.FileBasedReader;
import net.sicade.image.io.FileBasedReaderSpi;


/**
 * Impl�mentation par d�faut des d�codeurs d'images au format NetCDF. Dans la plupart des
 * cas, il ne sera pas n�cessaire de cr�er des classes d�riv�es. Des classes d�riv�es
 * de {@link AbstractReaderSpi} suffisent.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
final class DefaultReader extends FileBasedReader {
    /**
     * Dimension correspondant aux colonnes. Doit �tre constant, car il y aura des appels
     * � {@link Array#set0} cod�s en dur.
     */
    private static final int X_DIMENSION = 0;

    /**
     * Dimension correspondant aux lignes. Doit �tre constant, car il y aura des appels
     * � {@link Array#set1} cod�s en dur.
     */
    private static final int Y_DIMENSION = 1;

    /**
     * Le fichier NetCDF, ou {@code null} s'il n'a pas encore �t� ouvert.
     */
    private NetcdfFile file;

    /**
     * L'ensemble des donn�es du ph�nom�ne �tudi�.
     */
    private Variable variable;

    /** 
     * Construit un nouveau d�codeur HDF.
     *
     * @param spi Une description du service fournit par ce d�codeur.
     */
    public DefaultReader(final AbstractReaderSpi spi) {
        super(spi);
    }

    /**
     * Sp�cifie la source des donn�es � utiliser en entr�e. Cette source doit �tre un objet de
     * type {@link File} ou {@link URL}.
     */
    @Override
    public void setInput(final Object input, final boolean seekForwardOnly, final boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        if (file != null) {
            try {
                file.close();
            } catch (IOException e) {
                LOGGER.warning("Echec lors de la fermeture du fichier pr�c�dent.");
                /*
                 * On continue. Ce n'est qu'un avertissement car de toute fa�on on
                 * n'utilisera plus ce fichier.
                 */
            }
            file = null;
            variable = null;
        }
    }

    /**
     * Retourne la largeur de l'image.
     */
    public int getWidth(final int imageIndex) throws IOException {
        prepareVariable(imageIndex);
        return variable.getDimension(X_DIMENSION).getLength();
    }

    /**
     * Retourne la hauteur de l'image.
     */
    public int getHeight(final int imageIndex) throws IOException {
        prepareVariable(imageIndex);
        return variable.getDimension(Y_DIMENSION).getLength();
    }

    /**
     * V�rifie que les donn�es ont bien �t� charg�e dans {@link #variable} pour l'image sp�cifi�e.
     * Si les donn�es ont d�j� �t� charg�e lors d'un appel pr�c�dent, alors cette m�thode ne fait
     * rien.
     * 
     * @param   imageIndex L'index de l'image � traiter.
     * @throws  IndexOutOfBoundsException Si {@code indexImage} est diff�rent de 0,
     *          car on consid�re qu'il n'y a qu'une image par fichier HDF.
     * @throws  IllegalStateException Si le champ {@link #input} n'a pas �t� initialis� via
     *          {@link #setInput setInput(...)}.
     * @throws  IIOException Si le fichier NetCDF ne semble pas correct.
     * @throws  IOException Si la lecture a �chou�e pour une autre raison.
     */
    private void prepareVariable(final int imageIndex) throws IOException {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(Errors.format(ILLEGAL_ARGUMENT_$2, "imageIndex", imageIndex));
        }
        if (variable == null) {
            final String variableName = ((AbstractReaderSpi) originatingProvider).variable;
            final File inputFile = getInputFile();
            file = new NetcdfFile(inputFile.getPath()); // TODO: consider using NetcdfFileCache.acquire(...)
            @SuppressWarnings("unchecked")
            final List<Variable> variables = (List<Variable>) file.getVariables();
            for (final Variable v : variables) {
                if (variableName.equalsIgnoreCase(v.getName().trim())) {
                    variable = v;
                    return;
                }
            }
            file.close();
            file = null;
            throw new IIOException("La variable \"" + variableName + "\" n'a pas �t� trouv�e.");
        }
    }

    /**
     * Construit une image � partir des param�tre de lecture sp�cifi�s.
     *
     * @throws  IOException Si la lecture de l'image a �chou�e.
     */
    public BufferedImage read(final int imageIndex, final ImageReadParam param) throws IOException {
        clearAbortRequest();
        checkReadParamBandSettings(param, 1, 1);
        prepareVariable(imageIndex);
        final int            width  = variable.getDimension(X_DIMENSION).getLength();
        final int            height = variable.getDimension(Y_DIMENSION).getLength();
        final BufferedImage  image  = getDestination(param, getImageTypes(imageIndex), width, height);
        final WritableRaster raster = image.getRaster();
        final Rectangle   srcRegion = new Rectangle();
        final Rectangle  destRegion = new Rectangle();
        final int strideX, strideY;
        if (param != null) {
            strideX = param.getSourceXSubsampling();
            strideY = param.getSourceYSubsampling();
        } else {
            strideX = 1;
            strideY = 1;
        }
        computeRegions(param, width, height, image, srcRegion, destRegion);
        processImageStarted(imageIndex);
        /*
         * Proc�de � la lecture de la sous-r�gion demand�e par l'utilisateur.
         */
        final int[] shape  = variable.getShape();
        final int[] origin = new int[shape.length];
        origin [X_DIMENSION] = srcRegion.x;
        origin [Y_DIMENSION] = srcRegion.y;
        shape  [X_DIMENSION] = srcRegion.width;
        shape  [Y_DIMENSION] = srcRegion.height;
        final Array array;
        try {
            array = variable.read(origin, shape);
        } catch (InvalidRangeException e) {
            throw netcdfFailure(e);
        }
        final Index index = array.getIndex();
        final float toPercent = 100f / height;
        final int xmax = destRegion.x + destRegion.width;
        final int ymax = destRegion.y + destRegion.height;
        for (int yi=0,y=destRegion.y; y<ymax; y++) {
            index.set1(yi);
            yi += strideY;
            for (int xi=0,x=destRegion.x; x<xmax; x++) {
                raster.setSample(x, y, 0, array.getFloat(index.set0(xi)));
                xi += strideX;
            }
            processImageProgress(yi * toPercent);
            if (abortRequested()) {
                processReadAborted();
                return image;
            }
        }
        processImageComplete();
        return image;
    }

    /**
     * Lance une exception un peu plus explicite lorsqu'une erreur est survenue
     * lors de la lecture d'un fichier NetCDF.
     */
    private static IIOException netcdfFailure(final Exception e) {
        return new IIOException("Echec lors de la lecture du fichier NetCDF", e);
    }
}
