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
package org.constellation.admin.process;

import org.constellation.configuration.ConfigDirectory;
import org.constellation.process.AbstractProcessTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public class RestartTest extends AbstractProcessTest {

    @BeforeClass
    public static void initTestDir() {
        ConfigDirectory.setupTestEnvironement("RestartTest");
    }

    public RestartTest() {
        super(RestartDescriptor.NAME);
    }
    
    @AfterClass
    public static void shutDown() {
        ConfigDirectory.shutdownTestEnvironement("RestartTest");
    }
}
