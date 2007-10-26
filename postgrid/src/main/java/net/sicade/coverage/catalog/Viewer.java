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
package net.sicade.coverage.catalog;

import java.io.IOException;
import java.sql.SQLException;
import java.awt.image.RenderedImage;

import org.geotools.resources.Arguments;
import org.geotools.coverage.grid.GridCoverage2D;

import net.sicade.catalog.CatalogException;
import net.sicade.catalog.Database;
import net.sicade.image.Utilities;


/**
 * Display the image specified on the command line. This is utility is mostly for testing
 * purpose.
 *
 * @author Martin Desruisseaux
 */
public final class Viewer {
    /**
     * Do not allows instantiation of this class.
     */
    private Viewer() {
    }

    /**
     * Run from the command line.
     */
    public static void main(String[] args) throws SQLException, IOException, CatalogException {
        final Arguments arguments = new Arguments(args);
        final String layer = arguments.getRequiredString("-layer");
        args = arguments.getRemainingArguments(Integer.MAX_VALUE);
        final Database database = new Database();
        final GridCoverageTable coverages = new GridCoverageTable(database.getTable(GridCoverageTable.class));
        coverages.setLayer(layer);
        for (final String file : args) {
            final CoverageReference ref = coverages.getEntry(file);
            final GridCoverage2D coverage = ref.getCoverage(null);
            final RenderedImage image = coverage.geophysics(false).getRenderedImage();
            Utilities.show(image, file);
        }
        database.close();
    }
}
