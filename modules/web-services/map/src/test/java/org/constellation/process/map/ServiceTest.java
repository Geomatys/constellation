/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.process.map;

import java.io.File;
import java.net.MalformedURLException;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.process.AbstractProcessTest;
import org.geotoolkit.util.FileUtilities;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public abstract class ServiceTest extends AbstractProcessTest {
    
    protected static File configDirectory;
    public ServiceTest (final String str) {
        super(str);
    }
    
     @BeforeClass
    public static void initFolder() throws MalformedURLException {
        
        configDirectory = new File("ProcessMapTest");

        if (configDirectory.exists()) {
            FileUtilities.deleteDirectory(configDirectory);
        }
        
        configDirectory.mkdir();
        final File wms = new File(configDirectory, "WMS");
        final File wmts = new File(configDirectory, "WMTS");
        final File wfs = new File(configDirectory, "WFS");
        
        wms.mkdir();
        wmts.mkdir();
        wfs.mkdir();
        
        ConfigDirectory.setConfigDirectory(configDirectory);
    }
    
    @AfterClass
    public static void destroyFolder() {
        FileUtilities.deleteDirectory(configDirectory);
    }
}
