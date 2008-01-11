/*
 * (C) 2008, Geomatys
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
package org.geotools.image.io.mosaic;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

/**
 * An image writer built from a mosaic of other image readers. The mosaic is specified as a
 * collection of {@link Tile} objects, organized in a {@link TileManager}.
 *
 * @source $URL$
 * @author Cédric Briançon
 */
public class MosaicImageWriter extends ImageWriter {

    /**
     * Constructs an image writer with the specified provider.
     */
    public MosaicImageWriter(final ImageWriterSpi spi) {
        super(spi != null ? spi : Spi.DEFAULT);
    }

    @Override
    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType, ImageWriteParam param) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier imageType, ImageWriteParam param) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Write each tile, generated with {@linkplain TileGenerator#createTiles()}, into its
     * input file defined.
     * These tiles are gotten from the file in parameter.
     *
     * @param file The original raster to tiled.
     * @throws IOException
     * @throws IIOException
     */
    public void writeTiles(final Object originalInput) throws IOException {
        ImageReader reader = null;
        ImageInputStream stream = null;
        reader = findReader(originalInput);
        if (reader == null) {
            stream = ImageIO.createImageInputStream(originalInput);
            reader = findReader(stream);
            reader.setInput(stream);
        } else {
            reader.setInput(originalInput);
        }
        final ImageReadParam params = reader.getDefaultReadParam();
        // Launch the tile creation process.
        final TileManager tileManager = (TileManager) getOutput();
        final Collection<Tile> tiles = tileManager.getTiles();
        for (final Tile tile : tiles) {
            final Object input = tile.getInput();
            final Rectangle region = tile.getAbsoluteRegion();
            final Dimension subSampling = tile.getSubsampling();
            params.setSourceRegion(region);
            params.setSourceSubsampling(subSampling.width, subSampling.height, 0, 0);
            final BufferedImage image = reader.read(0, params);
            final ImageWriter writer = getImageWriter(tile, image);
            writer.write(image);
            final Object output = writer.getOutput();
            if (output != input) {
                // The output is not the File (or whatever object) given by Tile.
                // It is probably an ImageOutputStream created by getImageWriter,
                // so we need to close it.
                if (output instanceof Closeable) {
                    ((Closeable) output).close();
                } else if (output instanceof ImageOutputStream) {
                    ((ImageOutputStream) output).close();
                }
            }
        }
        if (stream != null) {
            stream.close();
        }
    }

    /**
     * Try to find a reader for the specified input file.
     *
     * @param input The whole raster.
     * @return An {@linkplain ImageReader} for the specified raster, or null if no reader
     *         seems to be convenient.
     */
    private ImageReader findReader(final Object input) {
        ImageReader reader = null;
        Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
        while (readers.hasNext()) {
            reader = readers.next();
            // TODO: do more check here in order to detect if it is a suitable reader.
            break;
        }
        return reader;
    }

    /**
     * Get an {@linkplain ImageWriter} that can encode the selected image.
     *
     * @param tile The tile to write.
     * @param image The image associated to this tile.
     * @return The image writer that seems to be the most appropriated, or null if
     *         no image writer can be applied.
     * @throws IOException
     */
    private static ImageWriter getImageWriter(final Tile tile, final BufferedImage image)
            throws IOException
    {
        final Object output = tile.getInput(); // We call that "output" because we want to create it.
        final Class<?> outputType = output.getClass();
        final IIORegistry registry = IIORegistry.getDefaultInstance();
        final String[] spiNames = tile.getImageReaderSpi().getImageWriterSpiNames();
        final ImageWriterSpi[] providers = new ImageWriterSpi[spiNames.length];
        int count = 0;
        for (final String name : spiNames) {
            final Class<?> spiType;
            try {
                spiType = Class.forName(name);
            } catch (ClassNotFoundException e) {
                // May be normal.
                continue;
            }
            final Object candidate = registry.getServiceProviderByClass(spiType);
            if (candidate instanceof ImageWriterSpi) {
                final ImageWriterSpi spi = (ImageWriterSpi) candidate;
                if (spi.canEncodeImage(image)) {
                    providers[count++] = spi;
                    for (final Class legalType : spi.getOutputTypes()) {
                        if (legalType.isAssignableFrom(outputType)) {
                            final ImageWriter writer = spi.createWriterInstance();
                            writer.setOutput(output);
                            return writer;
                        }
                    }
                }
            }
        }
        if (count != 0) {
            final ImageOutputStream stream = ImageIO.createImageOutputStream(output);
            final Class<? extends ImageOutputStream> streamType = stream.getClass();
            for (int i=0; i<count; i++) {
                final ImageWriterSpi spi = providers[i];
                for (final Class legalType : spi.getOutputTypes()) {
                    if (legalType.isAssignableFrom(streamType)) {
                        final ImageWriter writer = spi.createWriterInstance();
                        writer.setOutput(stream);
                        return writer;
                    }
                }
            }
            stream.close();
        }
        throw new IIOException("Unable to get a writer for the tiles to write");
    }

    /**
     * Service provider for {@link MosaicImageWriter}.
     *
     * @source $URL$
     * @author Cédric Briançon
     */
    public static class Spi extends ImageWriterSpi {
        /**
         * The format names.
         */
        private static final String[] NAMES = new String[] {
            "mosaic"
        };

        /**
         * The input types.
         */
        private static final Class<?>[] INPUT_TYPES = new Class[] {
            TileManager[].class,
            TileManager.class,
            Tile[].class,
            Collection.class
        };

        /**
         * The default instance.
         */
        public static final Spi DEFAULT = new Spi();

        /**
         * Creates a default provider.
         */
        public Spi() {
            vendorName      = "Geomatys";
            version         = "1.0";
            names           = NAMES;
            outputTypes     = INPUT_TYPES;
            pluginClassName = "org.geotools.image.io.mosaic.MosaicImageWriter";
        }

        @Override
        public boolean canEncodeImage(ImageTypeSpecifier type) {
            return true;
        }

        @Override
        public ImageWriter createWriterInstance(Object extension) throws IOException {
            return new MosaicImageWriter(this);
        }

        @Override
        public String getDescription(Locale locale) {
            return "Mosaic Image Writer";
        }

    }
}
