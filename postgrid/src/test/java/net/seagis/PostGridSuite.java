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
package net.seagis;

import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * All PostGrid-related tests. They are grouped in this suite in order to open
 * the database connection only once.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class PostGridSuite extends TestSuite {
    /**
     * Creates the test suite. The tests are added in an approximative dependency order.
     */
    public PostGridSuite() {
        addTestSuite(net.seagis.catalog.DatabaseTest.Open.class); // Must be first

        addTestSuite(org.geotools.referencing.factory.wkt.PostgisAuthorityFactoryTest.class);

        addTestSuite(net.seagis.catalog.         SpatialFunctionsTest         .class);
        addTestSuite(net.seagis.catalog.         TimeStampTest                .class);
        addTestSuite(net.seagis.catalog.         QueryTest                    .class);
        addTestSuite(net.seagis.coverage.catalog.MetadataParserTest           .class);
        addTestSuite(net.seagis.coverage.catalog.CategoryTableTest            .class);
        addTestSuite(net.seagis.coverage.catalog.SampleDimensionTableTest     .class);
        addTestSuite(net.seagis.coverage.catalog.FormatTableTest              .class);
        addTestSuite(net.seagis.coverage.catalog.SeriesEntryTest              .class);
        addTestSuite(net.seagis.coverage.catalog.SeriesTableTest              .class);
        addTestSuite(net.seagis.coverage.catalog.LayerTableTest               .class);
        addTestSuite(net.seagis.coverage.catalog.GridGeometryTableTest        .class);
        addTestSuite(net.seagis.coverage.catalog.GridCoverageTableTest        .class);
        addTestSuite(net.seagis.coverage.catalog.WritableGridCoverageTableTest.class);
        addTestSuite(net.seagis.coverage.catalog.MosaicTest                   .class);
        addTestSuite(net.seagis.coverage.model  .RegionOfInterestTableTest    .class);
        addTestSuite(net.seagis.coverage.model  .OperationTableTest           .class);
        addTestSuite(net.seagis.coverage.model  .DistributionTableTest        .class);
        addTestSuite(net.seagis.coverage.model  .DescriptorTableTest          .class);
        addTestSuite(net.seagis.coverage.model  .IntegrationTest              .class);
        addTestSuite(net.seagis.coverage.web    .TimeParserTest               .class);
        addTestSuite(net.seagis.coverage.web    .WebServiceWorkerTest         .class);

        addTestSuite(net.seagis.catalog.DatabaseTest.Close.class); // Must be last
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
