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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sicade.image.io;

// J2SE dependencies
import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Arrays;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.BufferedOutputStream;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.IIOException;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

// Geotools dependencies
import org.geotools.resources.Arguments;
import org.geotools.resources.Utilities;


/**
 * Utilitaires de lignes de commandes.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Console {
    /**
     * Do not allows instantiation of this class.
     */
    private Console() {
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
        final String filename = file.getName();
        final int dot = filename.lastIndexOf('.');
        if (dot < 0) {
            return ImageIO.read(file);
        }
        final String extension = filename.substring(dot+1);
        final Iterator<ImageReader> it = ImageIO.getImageReadersBySuffix(extension);
        if (it!=null) while (it.hasNext()) {
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
            final RenderedImage image = reader.readAsRenderedImage(0, null);
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
            extension = filename.substring(dot+1);
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
     * Liste les décodeurs et encodeurs disponibles.
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
            for (final String name : secondPass ? writers : readers) {
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
            out.print(Utilities.spaces(length - name.length()));
            out.print(" (");
            out.print(format.getValue());
            out.println(')');
        }
    }

    /**
     * Utilitaires de ligne de commande.
     * Les options suivantes sont autorisées:
     * <p>
     * <ul>
     *   <li>{@code -formats}<br>
     *       Liste les formats disponibles.</li>
     *   <li>{@code -mimes}<br>
     *       Liste les types mimes disponibles.</li>
     *   <li>{@code -show} <var>filename</var><br>
     *       Lit l'image spécifié et affiche.</li>
     * </ul>
     */
    public static void main(String[] args) throws IOException {
        final Arguments arguments = new Arguments(args);
        final boolean formats = arguments.getFlag("-formats");
        final boolean mimes   = arguments.getFlag("-mimes");
        final boolean show    = arguments.getFlag("-show");
        final boolean props   = arguments.getFlag("-properties");
        final PrintWriter out = arguments.out;
        args = arguments.getRemainingArguments(Integer.MAX_VALUE);
        if (formats) {
            out.println("Formats d'images:");
            list(out, ImageIO.getReaderFormatNames(), ImageIO.getWriterFormatNames());
        }
        if (mimes) {
            out.println("Types MIMES:");
            list(out, ImageIO.getReaderMIMETypes(), ImageIO.getWriterMIMETypes());
        }
        for (final String filename : args) {
            final RenderedImage image = read(new File(filename));
            if (props) {
                out.println(filename);
                out.print("  X-min  : "); out.println(image.getMinX());
                out.print("  Y-min  : "); out.println(image.getMinY());
                out.print("  Largeur: "); out.println(image.getWidth());
                out.print("  Hauteur: "); out.println(image.getHeight());
            }
            if (show) {
                net.sicade.image.Utilities.show(image, filename);
            }
        }
    }
}
