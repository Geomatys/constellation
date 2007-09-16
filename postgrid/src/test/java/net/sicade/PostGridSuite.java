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
package net.sicade;

import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * All PostGrid-related tests.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class PostGridSuite extends TestSuite {
    /**
     * Creates the test suite. The tests are added in an approximative dependency order.
     */
    public PostGridSuite() {
        // No need for database connection for those first few tests.
        addTestSuite(net.sicade.coverage.io.MetadataParserTest.class);

        addTestSuite(net.sicade.catalog.DatabaseTest.Open.class); // Must be first
        addTestSuite(net.sicade.catalog.         SpatialFunctionsTest         .class);
        addTestSuite(net.sicade.catalog.         TimeStampTest                .class);
        addTestSuite(net.sicade.catalog.         QueryTest                    .class);
        addTestSuite(net.sicade.coverage.catalog.SchemaTest                   .class);
        addTestSuite(net.sicade.coverage.catalog.CategoryTableTest            .class);
        addTestSuite(net.sicade.coverage.catalog.SampleDimensionTableTest     .class);
        addTestSuite(net.sicade.coverage.catalog.FormatTableTest              .class);
        addTestSuite(net.sicade.coverage.catalog.GridGeometryTableTest        .class);
        addTestSuite(net.sicade.coverage.catalog.GridCoverageTableTest        .class);
        addTestSuite(net.sicade.coverage.catalog.WritableGridCoverageTableTest.class);
        addTestSuite(net.sicade.coverage.catalog.ThematicTableTest            .class);
        addTestSuite(net.sicade.coverage.catalog.SeriesTableTest              .class);
        addTestSuite(net.sicade.coverage.catalog.LayerTableTest               .class);
        addTestSuite(net.sicade.coverage.model  .RegionOfInterestTableTest    .class);
        addTestSuite(net.sicade.coverage.model  .OperationTableTest           .class);
        addTestSuite(net.sicade.coverage.model  .DistributionTableTest        .class);
        addTestSuite(net.sicade.coverage.model  .DescriptorTableTest          .class);
        addTestSuite(net.sicade.catalog.DatabaseTest.Close.class); // Must be last
    }

    /**
     * Returns the test suite, for JUnit 3 compatibility.
     */
    public static TestSuite suite() {
        return new PostGridSuite();
    }

    /**
     * Run the suite from the command line.
     */
    public static void main(final String[] args) {
        TestRunner.run(suite());
    }
}
