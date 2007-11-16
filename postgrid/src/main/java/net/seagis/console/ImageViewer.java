/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2006, GeoTools Project Managment Committee (PMC)
 * (C) 2006, Geomatys
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
package net.seagis.console;

import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Iterator;
import java.io.File;
import java.io.Writer;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.BufferedOutputStream;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import javax.swing.JFrame;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.IIOException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.resources.Arguments;

import net.seagis.catalog.Database;
import net.seagis.coverage.catalog.CoverageReference;
import net.seagis.coverage.catalog.GridCoverageTable;
import org.geotools.image.io.metadata.GeographicMetadata;


/**
 * Displays the image specified on the command line. This is utility is mostly for testing
 * purpose.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class ImageViewer {
    /**
     * Do not allows instantiation of this class.
     */
    private ImageViewer() {
    }

    /**
     * Display the specified image. This method is used mostly for debugging purpose.
     */
    @SuppressWarnings("deprecation")
    public static void show(final RenderedImage image, final String title) {
        final JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(new javax.media.jai.widget.ScrollingImagePanel(image, 400, 400));
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Returns {@code true} if the specified array contains {@code File.class}.
     */
    private static boolean acceptFile(final Class[] types) {
        for (int i=types.length; --i>=0;) {
            if (File.class.equals(types[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reads an image from a file of the given name. This method is different from
     * {@link ImageIO#read(File)} in that the file format will be infered from the
     * filename extension.
     *
     * @param  file The source filename.
     * @return The image.
     * @throws IOException if an error occured during I/O.
     */
    public static RenderedImage read(final File file) throws IOException {
        return read(file, 0, null);
    }

    /**
     * Reads an image from a file of the given name. This method is different from
     * {@link ImageIO#read(File)} in that the file format will be infered from the
     * filename extension.
     *
     * @param  file The source filename.
     * @param  imageIndex Index of the image to be read.
     * @param  out If non-null, the stream where to write metadata.
     * @return The image.
     * @throws IOException if an error occured during I/O.
     */
    private static RenderedImage read(final File file, final int imageIndex, final Writer out)
            throws IOException
    {
        final String filename = file.getName();
        final int dot = filename.lastIndexOf('.');
        if (dot < 0) {
            return ImageIO.read(file);
        }
        final String extension = filename.substring(dot+1);
        final Iterator<ImageReader> it = ImageIO.getImageReadersBySuffix(extension);
        if (it != null) while (it.hasNext()) {
            final ImageReader reader = it.next();
            final ImageReaderSpi spi = reader.getOriginatingProvider();
            final ImageInputStream input;
            if (spi!=null && acceptFile(spi.getInputTypes())) {
                reader.setInput(file);
                input = null;
            } else {
                input = ImageIO.createImageInputStream(file);
                reader.setInput(input);
            }
            final RenderedImage image = reader.readAsRenderedImage(imageIndex, null);
            if (out != null) {
                final IIOMetadata metadata = reader.getImageMetadata(imageIndex);
                if (metadata instanceof GeographicMetadata) {
                    out.write(metadata.toString());
                }
            }
            reader.dispose();
            if (input != null) {
                input.close();
            }
            return image;
        }
        throw new IIOException("Can't find a decoder.");
    }

    /**
     * Saves an image in a file of the given name. The file format will be infered from
     * the filename extension. If no extension were provided, default to PNG.
     *
     * @param  image The image to save.
     * @param  file The destination file.
     * @throws IOException if an error occured during I/O.
     */
    public static void save(final RenderedImage image, File file) throws IOException {
        String filename = file.getName();
        int dot = filename.lastIndexOf('.');
        String extension;
        if (dot >= 0) {
            extension = filename.substring(dot + 1);
        } else {
            dot       = filename.length();
            extension = "png";
            filename += ".png";
            file      = new File(file.getParentFile(), filename);
        }
        final Iterator<ImageWriter> it = ImageIO.getImageWritersBySuffix(extension);
        if (it!=null && it.hasNext()) {
            final ImageWriter writer = it.next();
            final ImageWriterSpi spi = writer.getOriginatingProvider();
            final ImageOutputStream output;
            if (spi!=null && acceptFile(spi.getOutputTypes())) {
                writer.setOutput(file);
                output = null;
            } else {
                output = ImageIO.createImageOutputStream(file);
                writer.setOutput(output);
            }
            writer.write(image);
            writer.dispose();
            if (output != null) {
                output.close();
            }
            return;
        }
        throw new IIOException("Can't find an encoder.");
    }

    /**
     * Save the first band from the specified raster in binary format,
     * using IEEE 754 {@code float} format on 32 bits.
     *
     * @param  image The image to save.
     * @param  filename The destination filename.
     * @throws IOException if an error occured during I/O.
     */
    public static void saveBinary(final Raster raster, final String filename) throws IOException {
        final int xmin = raster.getMinX();
        final int ymin = raster.getMinY();
        final int xmax = raster.getWidth()  + xmin;
        final int ymax = raster.getHeight() + ymin;
        final DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
        for (int y=ymin; y<ymax; y++) {
            for (int x=xmin; x<xmax; x++) {
                out.writeFloat(raster.getSampleFloat(x, y, 0));
            }
        }
        out.close();
    }

    /**
     * List all available image readers and writers.
     */
    private static void list(final PrintWriter out, final String[] readers, final String[] writers) {
        final String READ       = "R  ";
        final String WRITE      = "  W";
        final String READ_WRITE = "R/W";
        final Map<String,String> formats = new TreeMap<String,String>();
        final Map<String,String> names   = new HashMap<String,String>();
        int length = 0;
        boolean secondPass = false;
        do {
            final String label = secondPass ? WRITE : READ;
            final String[] codecs = secondPass ? writers : readers;
            for (int i=0; i<codecs.length; i++) {
                final String name = codecs[i];
                final String identifier = name.toLowerCase();
                String old = names.put(identifier, name);
                if (old!=null && old.compareTo(name) > 0) {
                    names.put(identifier, old);
                }
                old = formats.put(identifier, label);
                if (old!=null && old!=label) {
                    formats.put(identifier, READ_WRITE);
                }
                final int lg = name.length();
                if (lg > length) {
                    length = lg;
                }
            }
        } while ((secondPass = !secondPass) == true);
        for (final Map.Entry<String,String> format : formats.entrySet()) {
            final String name = names.get(format.getKey());
            out.print("  ");
            out.print(name);
            out.print(org.geotools.resources.Utilities.spaces(length - name.length()));
            out.print(" (");
            out.print(format.getValue());
            out.println(')');
        }
    }

    /**
     * Prints some properties about the given image.
     */
    private static void printProperties(final RenderedImage image, final PrintWriter out) {
        out.println("Image properties");
        out.print("  min x  : "); out.println(image.getMinX());
        out.print("  min y  : "); out.println(image.getMinY());
        out.print("  width  : "); out.println(image.getWidth());
        out.print("  height : "); out.println(image.getHeight());
    }

    /**
     * Command line tool. Options are
     * <p>
     * <ul>
     *   <li>{@code -formats}<br>
     *       Lists available formats.</li>
     *   <li>{@code -mimes}<br>
     *       Lists available mime types.</li>
     *   <li>{@code -show}<br>
     *       Show the image instead of just printing metadata.</li>
     *   <li>{@code -layer} <var>name</var><br>
     *       If specified, get the image from the specified layer
     *       (otherwise the image is get from an ordinary file).</li>
     * </ul>
     */
    public static void main(String[] args) throws IOException {
        final Arguments arguments = new Arguments(args);
        final boolean formats = arguments.getFlag("-formats");
        final boolean mimes   = arguments.getFlag("-mimes");
        final boolean show    = arguments.getFlag("-show");
        final String  layer   = arguments.getOptionalString("-layer");
        final PrintWriter out = arguments.out;
        args = arguments.getRemainingArguments(Integer.MAX_VALUE);
        if (formats) {
            out.println("Images formats:");
            list(out, ImageIO.getReaderFormatNames(), ImageIO.getWriterFormatNames());
        }
        if (mimes) {
            out.println("MIMES types:");
            list(out, ImageIO.getReaderMIMETypes(), ImageIO.getWriterMIMETypes());
        }
        if (layer == null) {
            for (int i=0; i<args.length; i++) {
                final String filename = args[i];
                out.print("Filename: "); out.println(filename);
                final RenderedImage image = read(new File(filename), 0, out);
                printProperties(image, out);
                if (show) {
                    show(image, filename);
                }
            }
        } else try {
            final Database database = new Database();
            final GridCoverageTable coverages = new GridCoverageTable(database.getTable(GridCoverageTable.class));
            coverages.setLayer(layer);
            for (final String file : args) {
                final CoverageReference ref = coverages.getEntry(file);
                final GridCoverage2D coverage = ref.getCoverage(null);
                final RenderedImage image = coverage.geophysics(false).getRenderedImage();
                printProperties(image, out);
                if (show) {
                    show(image, file);
                }
            }
            database.close();
        } catch (Exception e) {
            e.printStackTrace(arguments.err);
        }
    }
}
