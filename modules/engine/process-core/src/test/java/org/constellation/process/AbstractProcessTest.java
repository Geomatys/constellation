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
package org.constellation.process;

import java.util.logging.Logger;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessFinder;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.opengis.util.NoSuchIdentifierException;
/**
 * Abstract test base for all engine process tests. 
 * 
 * @author Quentin Boileau (Geomatys)
 */
public abstract class AbstractProcessTest {

    protected static final Logger LOGGER = Logging.getLogger(AbstractProcessTest.class);
    private static final String factory = ConstellationProcessFactory.NAME;
    private final String process;


    protected AbstractProcessTest(final String process){
        this.process = process;
    }

    @Test
    public void findProcessTest() throws NoSuchIdentifierException{
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(factory, process);
        assertNotNull(desc);
    }
    
}
