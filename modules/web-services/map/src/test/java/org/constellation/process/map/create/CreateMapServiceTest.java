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

/**
 *
 * @author Quentin Boileau (Geometys).
 */
public class CreateMapServiceTest extends ServiceTest {

    public CreateMapServiceTest() {
        super(CreateMapServiceDesciptor.NAME);
    }

    @Test
    public void testCreateWMS() throws ProcessException, NoSuchIdentifierException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor("constellation", CreateMapServiceDesciptor.NAME);

        //WMS
        ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter("service_Name").setValue("wms");
        in.parameter("identifier").setValue("instance1");

        org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        assertTrue(checkInsanceCreated("WMS", "instance1"));

    }

    @Test
    public void testCreateWMTS() throws ProcessException, NoSuchIdentifierException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor("constellation", CreateMapServiceDesciptor.NAME);

        //WMTS
        ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter("service_Name").setValue("wmTs");
        in.parameter("identifier").setValue("instance2");

        org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        assertTrue(checkInsanceCreated("WMTS", "instance2"));
    }

    @Test
    public void testCreateNoConfiguration() throws ProcessException, NoSuchIdentifierException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor("constellation", CreateMapServiceDesciptor.NAME);

        // instance directory created but no configuration file
        final File instance3Dir = new File(configDirectory.getAbsolutePath() + "/WMS/instance3");
        instance3Dir.mkdir();

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter("service_Name").setValue("WMS");
        in.parameter("identifier").setValue("instance3");

        final org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        assertTrue(checkInsanceCreated("WMS", "instance3"));
    }

    @Test
    public void testCreateUnknowService() throws ProcessException, NoSuchIdentifierException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor("constellation", CreateMapServiceDesciptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter("service_Name").setValue("foo");
        in.parameter("identifier").setValue("instance2");

        try {
            final org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
    }

    @Test
    public void testCreateOtherService() throws ProcessException, NoSuchIdentifierException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor("constellation", CreateMapServiceDesciptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter("service_Name").setValue("SOS");
        in.parameter("identifier").setValue("instance1");

        try {
            final org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
    }

    @Test
    public void testCreateEmptyIdentifier() throws ProcessException, NoSuchIdentifierException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor("constellation", CreateMapServiceDesciptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter("service_Name").setValue("WMS");
        in.parameter("identifier").setValue("");

        try {
            final org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
    }

    @Test
    public void testCreateAleardyExist() throws ProcessException, NoSuchIdentifierException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor("constellation", CreateMapServiceDesciptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter("service_Name").setValue("WMS");
        in.parameter("identifier").setValue("instance3");

        try {
            final org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
    }

    private boolean checkInsanceCreated(final String serviceName, final String identifier) {

        final File instanceDir = new File(configDirectory.getAbsolutePath() + "/" + serviceName, identifier);
        if (instanceDir.exists() && instanceDir.isDirectory()) {
            final File configFile = new File(instanceDir, "layerContext.xml");
            return configFile.exists();
        } else {
            return false;
        }
    }
}
