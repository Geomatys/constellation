/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
package org.constellation.coverage;

import java.net.MalformedURLException;

import junit.framework.Assert;

import org.apache.sis.storage.DataStoreException;
import org.junit.Test;

public class PyramidCoverageHelperTestCase {

    @Test
    public void testSimple() throws MalformedURLException, DataStoreException {
        PyramidCoverageHelper helper = PyramidCoverageHelper.builder("name")
                .inputFormat("PNG").fromImage("path/to/a/geo.tiff")
                .toMemoryStore().build();
        Assert.assertNotNull(helper);
        PyramidCoverageHelper.builder("zozo").fromImage("");

    }

}
