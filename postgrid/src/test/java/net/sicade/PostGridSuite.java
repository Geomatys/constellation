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
        addTestSuite(net.sicade.catalog.             SpatialFunctionsTest         .class);
        addTestSuite(net.sicade.catalog.             TimeStampTest                .class);
        addTestSuite(net.sicade.catalog.             QueryTest                    .class);
        addTestSuite(net.sicade.coverage.catalog.sql.SchemaTest                   .class);
        addTestSuite(net.sicade.coverage.catalog.sql.CategoryTableTest            .class);
        addTestSuite(net.sicade.coverage.catalog.sql.SampleDimensionTableTest     .class);
        addTestSuite(net.sicade.coverage.catalog.sql.FormatTableTest              .class);
        addTestSuite(net.sicade.coverage.catalog.sql.RegionOfInterestTableTest    .class);
        addTestSuite(net.sicade.coverage.catalog.sql.OperationTableTest           .class);
        addTestSuite(net.sicade.coverage.catalog.sql.DistributionTableTest        .class);
        addTestSuite(net.sicade.coverage.catalog.sql.DescriptorTableTest          .class);
        addTestSuite(net.sicade.coverage.catalog.sql.ThematicTableTest            .class);
        addTestSuite(net.sicade.coverage.catalog.sql.SeriesTableTest              .class);
        addTestSuite(net.sicade.coverage.catalog.sql.LayerTableTest               .class);
        addTestSuite(net.sicade.coverage.catalog.sql.GridGeometryTableTest        .class);
        addTestSuite(net.sicade.coverage.catalog.sql.GridCoverageTableTest        .class);
        addTestSuite(net.sicade.coverage.catalog.sql.WritableGridCoverageTableTest.class);
    }

    /**
     * Returns the test suite.
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
