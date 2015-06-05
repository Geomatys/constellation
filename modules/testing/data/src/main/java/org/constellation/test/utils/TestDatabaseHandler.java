/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.test.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.ConfigDirectory;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class TestDatabaseHandler {
    
    private static final Logger LOGGER = Logging.getLogger(TestDatabaseHandler.class);
    
    public static Properties testProperties = new Properties();
    
    static {
        // load the properties
        hasLocalDatabase();
    }
    
    public static boolean hasLocalDatabase() {
        final File home = ConfigDirectory.getUserHomeDirectory();
        if (home != null && home.isDirectory()) {
            final File cstlTestPropFile = new File(home, "cstl-test.properties");
            if (cstlTestPropFile.exists()) {
                try {
                    testProperties.load(new FileReader(cstlTestPropFile));
                    return true;
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
            }
        }
        return false;
    }
}
