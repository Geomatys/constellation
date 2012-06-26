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
package org.constellation.process.service.delete;

import org.constellation.process.service.delete.DeleteMapServiceDescriptor;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.service.AbstractMapServiceTest;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.junit.Test;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;
import static org.junit.Assert.*;

/**
 *
 * @author Quentin Boileau (Geometys).
 */
public class DeleteMapServiceTest extends AbstractMapServiceTest {

    public DeleteMapServiceTest() {
        super(DeleteMapServiceDescriptor.NAME);
    }
    
    @Test
    public void testDeleteWMS() throws ProcessException, NoSuchIdentifierException {

        createDefaultInstance("WMS","instance1");
        createDefaultInstance("WMS","instance2");
        
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteMapServiceDescriptor.NAME);

        //WMS
        ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(DeleteMapServiceDescriptor.SERVICE_NAME_NAME).setValue("wms");
        in.parameter(DeleteMapServiceDescriptor.IDENTIFIER_NAME).setValue("instance1");

        org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        assertFalse(checkInsanceExist("WMS", "instance1"));
        assertTrue(checkInsanceExist("WMS", "instance2"));
        
        deleteInstance("WMS", "instance2");
    }
 
    @Test
    public void testFailDeleteWMS1() throws ProcessException, NoSuchIdentifierException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteMapServiceDescriptor.NAME);

        //WMS
        ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(DeleteMapServiceDescriptor.SERVICE_NAME_NAME).setValue("FOO");
        in.parameter(DeleteMapServiceDescriptor.IDENTIFIER_NAME).setValue("instance1");

        try {
            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
        
    }
    
    @Test
    public void testFailDeleteWMS2() throws ProcessException, NoSuchIdentifierException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteMapServiceDescriptor.NAME);

        //WMS
        ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(DeleteMapServiceDescriptor.SERVICE_NAME_NAME).setValue("SOS");
        in.parameter(DeleteMapServiceDescriptor.IDENTIFIER_NAME).setValue("instance1");

        try {
            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
        
    }
    
    @Test
    public void testFailDeleteWMS3() throws ProcessException, NoSuchIdentifierException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteMapServiceDescriptor.NAME);

        //WMS
        ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(DeleteMapServiceDescriptor.SERVICE_NAME_NAME).setValue("wms");
        in.parameter(DeleteMapServiceDescriptor.IDENTIFIER_NAME).setValue("unknowInstance");

        try {
            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
        
    }
}
