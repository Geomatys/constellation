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
package org.constellation.process;

import java.io.File;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.process.service.GetOrCreateMapServiceTest;
import org.constellation.wmts.ws.DefaultWMTSWorker;
import org.geotoolkit.util.FileUtilities;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author Quentin Boileau (Geomatys)
 */
public class GetOrCreateWMTSServiceTest extends GetOrCreateMapServiceTest {

    @BeforeClass
    public static void createConfig () {
        final File configDir = new File("WMTSConfigTest");
        configDir.mkdir();
        final File wmts = new File(configDir, "WMTS");
        wmts.mkdir();

        ConfigDirectory.setConfigDirectory(configDir);
    }

    @AfterClass
    public static void deleteConfig () {
        FileUtilities.deleteDirectory(configDirectory);
    }

    public GetOrCreateWMTSServiceTest() {
        super("WMTS", DefaultWMTSWorker.class);
    }

}
