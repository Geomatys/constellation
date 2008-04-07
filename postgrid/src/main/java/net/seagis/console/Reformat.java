/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2008, Institut de Recherche pour le Développement
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

import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.imageio.spi.*;
import javax.imageio.stream.*;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;

import org.geotools.console.Option;
import org.geotools.console.CommandLine;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.image.ImageUtilities;


/**
 * Loads all images in a directory and rewrite them in a different format.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Reformat extends CommandLine implements FileFilter {
    /**
     * The target format.
     */
    @Option(description="The target format.")
    private String format;

    /**
     * The color model as one of RGB or ARGB constants.
     */
    @Option(description="The color model. One of RGB or ARGB.")
    private String model;

    /**
     * {@code true} for scanning recursively into directories.
     */
    @Option(description="Scan recursively into directories.")
    private boolean recursive;

    /**
     * The image writer.
     */
    private ImageWriter writer;

    /**
     * The {@link BufferedImage} destination type, or {@code null} if none.
     */
    private int destinationType;

    /**
     * The filename suffix.
     */
    private String suffix;

    /**
     * Creates an instance of {@code Reformat} for the given arguments.
     */
    private Reformat(final String[] args) {
        super(args, Integer.MAX_VALUE);
    }

    /**
     * Executes the command for every files given on the command-line.
     */
    private void run() throws IOException {
        if (model != null) {
            if (model.equalsIgnoreCase("ARGB")) {
                destinationType = BufferedImage.TYPE_INT_ARGB;
            } else if (model.equalsIgnoreCase("RGB")) {
                destinationType = BufferedImage.TYPE_INT_RGB;
            } else {
                err.println(Errors.format(ErrorKeys.UNKNOW_PARAMETER_$1, model));
                System.exit(ILLEGAL_ARGUMENT_EXIT_CODE);
            }
        }
        if (format != null) {
            ImageUtilities.allowNativeCodec(format, ImageWriterSpi.class, false);
            final Iterator<ImageWriter> it = ImageIO.getImageWritersByFormatName(format);
            if (!it.hasNext()) {
                err.println(Errors.format(ErrorKeys.UNKNOW_IMAGE_FORMAT_$1, format));
                System.exit(ILLEGAL_ARGUMENT_EXIT_CODE);
            }
            writer = it.next();
            final String[] suffixes = writer.getOriginatingProvider().getFileSuffixes();
            suffix = (suffixes != null && suffixes.length != 0) ? suffixes[0] : format;
        }
        for (final String filename : arguments) {
            run(new File(filename));
        }
        if (writer != null) {
            writer.dispose();
            writer = null;
        }
    }

    /**
     * Executes the command for the given file.
     */
    private void run(final File file) throws IOException {
        if (file.isDirectory()) {
            for (final File child : file.listFiles(this)) {
                run(child);
            }
        } else {
            BufferedImage image = ImageIO.read(file);
            out.print(file);
            out.print(": ");
            out.print(image.getWidth());
            out.print(" x ");
            out.print(image.getHeight());
            switch (image.getColorModel().getTransparency()) {
                case Transparency.OPAQUE:      out.print(" opaque");       break;
                case Transparency.BITMASK:     out.print(" bitmask");      break;
                case Transparency.TRANSLUCENT: out.print(" transluscent"); break;
            }
            out.println();
            if (writer == null) {
                return;
            }
            if (destinationType != 0 && image.getType() != destinationType) {
                final BufferedImage buffer = new BufferedImage(image.getWidth(), image.getHeight(), destinationType);
                final Graphics2D graphics = buffer.createGraphics();
                graphics.drawRenderedImage(image, new AffineTransform());
                graphics.dispose();
                image = buffer;
            }
            String name = file.getName();
            int sep = name.lastIndexOf('.');
            if (sep > 0) {
                name = name.substring(0, sep);
            }
            name = name + '.' + suffix;
            final ImageOutputStream out = ImageIO.createImageOutputStream(new File(file.getParentFile(), name));
            writer.setOutput(out);
            writer.write(image);
            writer.reset();
            out.close();
        }
    }

    /**
     * Returns {@code true} if the following file should be accepted.
     * This is invoked only when scanning a directory content.
     */
    public boolean accept(final File pathname) {
        if (pathname.isDirectory()) {
            return recursive;
        }
        return true;
    }

    /**
     * Runs from the command line.
     */
    public static void main(String[] args) {
        final Reformat worker = new Reformat(args);
        try {
            worker.run();
        } catch (IOException e) {
            worker.err.println(e);
            System.exit(IOEXCEPTION_EXIT_CODE);
        }
    }
}
