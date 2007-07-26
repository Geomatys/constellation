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
        addTestSuite(net.sicade.observation.sql.         SpatialFunctionsTest         .class);
        addTestSuite(net.sicade.observation.sql.         TimeStampTest                .class);
        addTestSuite(net.sicade.observation.sql.         QueryTest                    .class);
        addTestSuite(net.sicade.observation.coverage.sql.SchemaTest                   .class);
        addTestSuite(net.sicade.observation.coverage.sql.CategoryTableTest            .class);
        addTestSuite(net.sicade.observation.coverage.sql.SampleDimensionTableTest     .class);
        addTestSuite(net.sicade.observation.coverage.sql.FormatTableTest              .class);
        addTestSuite(net.sicade.observation.coverage.sql.RegionOfInterestTableTest    .class);
        addTestSuite(net.sicade.observation.coverage.sql.OperationTableTest           .class);
        addTestSuite(net.sicade.observation.coverage.sql.DistributionTableTest        .class);
        addTestSuite(net.sicade.observation.coverage.sql.DescriptorTableTest          .class);
        addTestSuite(net.sicade.observation.coverage.sql.ThematicTableTest            .class);
        addTestSuite(net.sicade.observation.coverage.sql.SeriesTableTest              .class);
        addTestSuite(net.sicade.observation.coverage.sql.LayerTableTest               .class);
        addTestSuite(net.sicade.observation.coverage.sql.GridGeometryTableTest        .class);
        addTestSuite(net.sicade.observation.coverage.sql.GridCoverageTableTest        .class);
        addTestSuite(net.sicade.observation.coverage.sql.WritableGridCoverageTableTest.class);
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
