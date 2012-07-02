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
package org.constellation.process.service.create;

import org.constellation.process.service.create.CreateMapServiceDescriptor;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.Layers;
import org.constellation.configuration.Source;
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
public class CreateMapServiceTest extends AbstractMapServiceTest {

    public CreateMapServiceTest() {
        super(CreateMapServiceDescriptor.NAME);
    }

    @Test
    public void testCreateWMS() throws ProcessException, NoSuchIdentifierException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateMapServiceDescriptor.NAME);

        //WMS
        ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(CreateMapServiceDescriptor.SERVICE_NAME_NAME).setValue("wms");
        in.parameter(CreateMapServiceDescriptor.IDENTIFIER_NAME).setValue("instance1");

        org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        assertTrue(checkInsanceExist("WMS", "instance1"));
        deleteInstance("WMS", "instance1");
    }

    @Test
    public void testCreateWMSWithContext() throws ProcessException, NoSuchIdentifierException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateMapServiceDescriptor.NAME);

        final List<Source> sources = new ArrayList<Source>();
        sources.add(new Source("source1", Boolean.TRUE, null, null));
        final Layers layers = new Layers(sources);
        final LayerContext conf = new LayerContext(layers);

        //WMS
        ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(CreateMapServiceDescriptor.SERVICE_NAME_NAME).setValue("wms");
        in.parameter(CreateMapServiceDescriptor.IDENTIFIER_NAME).setValue("instance11");
        in.parameter(CreateMapServiceDescriptor.CONFIG_NAME).setValue(conf);

        org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        assertTrue(checkInsanceExist("WMS", "instance11"));
        assertEquals(conf, getConfig("instance11"));
        
        deleteInstance("WMS", "instance11");
    }

    @Test
    public void testCreateWMTS() throws ProcessException, NoSuchIdentifierException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateMapServiceDescriptor.NAME);

        //WMTS
        ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(CreateMapServiceDescriptor.SERVICE_NAME_NAME).setValue("wmTs");
        in.parameter(CreateMapServiceDescriptor.IDENTIFIER_NAME).setValue("instance2");

        org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        assertTrue(checkInsanceExist("WMTS", "instance2"));
        deleteInstance("WMTS", "instance2");
    }

    @Test
    public void testCreateNoConfiguration() throws ProcessException, NoSuchIdentifierException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateMapServiceDescriptor.NAME);

        // instance directory created but no configuration file
        final File instance3Dir = new File(configDirectory.getAbsolutePath() + "/WMS/instance3");
        instance3Dir.mkdir();

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(CreateMapServiceDescriptor.SERVICE_NAME_NAME).setValue("WMS");
        in.parameter(CreateMapServiceDescriptor.IDENTIFIER_NAME).setValue("instance3");

        final org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        assertTrue(checkInsanceExist("WMS", "instance3"));
        deleteInstance("WMS", "instance3");
    }

    @Test
    public void testCreateUnknowService() throws ProcessException, NoSuchIdentifierException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateMapServiceDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(CreateMapServiceDescriptor.SERVICE_NAME_NAME).setValue("foo");
        in.parameter(CreateMapServiceDescriptor.IDENTIFIER_NAME).setValue("instance2");

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

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateMapServiceDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(CreateMapServiceDescriptor.SERVICE_NAME_NAME).setValue("SOS");
        in.parameter(CreateMapServiceDescriptor.IDENTIFIER_NAME).setValue("instance1");

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

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateMapServiceDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(CreateMapServiceDescriptor.SERVICE_NAME_NAME).setValue("WMS");
        in.parameter(CreateMapServiceDescriptor.IDENTIFIER_NAME).setValue("");

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

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateMapServiceDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(CreateMapServiceDescriptor.SERVICE_NAME_NAME).setValue("WMS");
        in.parameter(CreateMapServiceDescriptor.IDENTIFIER_NAME).setValue("instance3");

        try {
            final org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex) {
            //do nothing
        }
    }
}
