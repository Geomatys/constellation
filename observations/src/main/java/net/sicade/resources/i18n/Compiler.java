/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2001, Institut de Recherche pour le Développement
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
package net.sicade.resources.i18n;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import org.geotools.resources.Arguments;
import org.geotools.resources.ResourceCompiler;


/**
 * Resource compiler.
 * 
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Compiler extends ResourceCompiler {
    /**
     * The base directory for {@code "java"} {@code "resources"} sub-directories.
     * The directory structure must be consistent with Maven conventions.
     */
    private static final File SOURCE_DIRECTORY = new File("observations/src/main");

    /**
     * The resources to process.
     */
    private static final Class[] RESOURCES_TO_PROCESS = {
        Resources.class
    };

    /**
     * Constructs a new compiler.
     */
    protected Compiler(final File sourceDirectory, final Class bundleClass, final PrintWriter out)
            throws IOException
    {
        super(sourceDirectory, bundleClass, out);
    }

    /**
     * Run the resource compiler.
     */
    public static void main(String[] args) {
        final Arguments arguments = new Arguments(args);
        final PrintWriter out = arguments.out;
        args = arguments.getRemainingArguments(0);
        if (!SOURCE_DIRECTORY.isDirectory()) {
            out.print(SOURCE_DIRECTORY);
            out.println(" not found or is not a directory.");
            return;
        }
        for (int i=0; i<RESOURCES_TO_PROCESS.length; i++) {
            try {
                scanForResources(SOURCE_DIRECTORY, RESOURCES_TO_PROCESS[i], out);
            } catch (IOException exception) {
                out.println(exception.getLocalizedMessage());
            }
        }
        out.flush();
    }
}
