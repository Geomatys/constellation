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
package org.constellation.process.map.create;

import java.io.File;
import org.constellation.process.map.ServiceTest;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.junit.Test;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;
import static org.junit.Assert.*;
import static org.constellation.process.map.create.CreateMapServiceDesciptor.*;

/**
 *
 * @author Quentin Boileau (Geometys).
 */
public class CreateMapServiceTest extends ServiceTest {
    
    public CreateMapServiceTest () {
        super(CreateMapServiceDesciptor.NAME);
    }
    
    @Test
    public void testCreateProvider() throws ProcessException, NoSuchIdentifierException {
        
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor("constellation", CreateMapServiceDesciptor.NAME);
        
        //WMS
        ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter("service_Name").setValue("wms");
        in.parameter("identifier").setValue("instance1");

        org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();
        
        assertTrue(checkInsanceCreated("WMS", "instance1"));
        
        //WMTS
        in = desc.getInputDescriptor().createValue();
        in.parameter("service_Name").setValue("wmTs");
        in.parameter("identifier").setValue("instance2");

        proc = desc.createProcess(in);
        proc.call();
           
        assertTrue(checkInsanceCreated("WMTS", "instance2"));
        
        //unknow service
        in = desc.getInputDescriptor().createValue();
        in.parameter("service_Name").setValue("foo");
        in.parameter("identifier").setValue("instance2");

        try {
            proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
        
        //empty identifier
        in = desc.getInputDescriptor().createValue();
        in.parameter("service_Name").setValue("foo");
        in.parameter("identifier").setValue("");

        try {
            proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
    }
    
    private boolean checkInsanceCreated(final String serviceName, final String identifier) {
        
        final File instanceDir = new File(configDirectory.getAbsolutePath()+"/"+serviceName, identifier);
        if (instanceDir.exists() && instanceDir.isDirectory()) {
            final File configFile = new File(instanceDir, "layerContext.xml");
            return configFile.exists();
        } else {
            return false;
        }
    }
}
