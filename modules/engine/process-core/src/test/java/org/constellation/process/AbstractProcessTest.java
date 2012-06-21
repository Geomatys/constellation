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
    
    private static final String factory = ConstellationProcessFactory.NAME;
    private String process;


    protected AbstractProcessTest(final String process){
        this.process = process;
    }

    @Test
    public void findProcessTest() throws NoSuchIdentifierException{
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(factory, process);
        assertNotNull(desc);
    }
    
}
