/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Geomatys
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
package net.seagis.resources.i18n;

import java.io.File;
import org.geotools.resources.IndexedResourceCompiler;


/**
 * Resource compiler.
 * 
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Compiler {
    /**
     * The base directory for {@code "java"} {@code "resources"} sub-directories.
     * The directory structure must be consistent with Maven conventions.
     */
    private static final File SOURCE_DIRECTORY = new File("postgrid/src/main");

    /**
     * The resources to process.
     */
    private static final Class[] RESOURCES_TO_PROCESS = {
        Resources.class
    };

    /**
     * Do not allows instantiation of this class.
     */
    private Compiler() {
    }

    /**
     * Run the resource compiler.
     */
    public static void main(final String[] args) {
        IndexedResourceCompiler.main(args, SOURCE_DIRECTORY, RESOURCES_TO_PROCESS);
    }
}
