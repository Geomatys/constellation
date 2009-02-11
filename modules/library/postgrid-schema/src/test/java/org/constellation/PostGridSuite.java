/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation;

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
        addTestSuite(org.constellation.catalog.DatabaseTest.Open.class); // Must be first

        addTestSuite(org.constellation.catalog.         SpatialFunctionsTest         .class);
        addTestSuite(org.constellation.catalog.         TimeStampTest                .class);
        addTestSuite(org.constellation.catalog.         QueryTest                    .class);

        addTestSuite(org.constellation.catalog.DatabaseTest.Close.class); // Must be last
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
