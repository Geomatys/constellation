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
package org.constellation.process.service.getconfig;

import java.util.ArrayList;
import java.util.List;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.Layers;
import org.constellation.configuration.Source;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.service.AbstractMapServiceTest;
import org.constellation.process.service.configure.ConfigureMapServiceDescriptor;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.junit.Test;
import static org.junit.Assert.*;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;

/**
 *
 * @author Quentin Boileau (Geoamtys)
 */
public class GetConfigMapServiceTest extends AbstractMapServiceTest {

    public GetConfigMapServiceTest() {
        super(GetConfigMapServiceDescriptor.NAME);
    }

     @Test
    public void testGetConfigWMS() throws ProcessException, NoSuchIdentifierException {

        final List<Source> sources = new ArrayList<Source>();
        sources.add(new Source("source1", Boolean.TRUE, null, null));
        final Layers layers = new Layers(sources);
        final LayerContext conf = new LayerContext(layers);

        createInstance("WMS","instance5", conf);

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, GetConfigMapServiceDescriptor.NAME);

        //WMS
        ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(GetConfigMapServiceDescriptor.SERVICE_NAME_NAME).setValue("wms");
        in.parameter(GetConfigMapServiceDescriptor.IDENTIFIER_NAME).setValue("instance5");

        org.geotoolkit.process.Process proc = desc.createProcess(in);
        ParameterValueGroup ouptuts = proc.call();

        assertEquals(conf, ouptuts.parameter(GetConfigMapServiceDescriptor.CONFIG_NAME).getValue());

        deleteInstance("WMS", "instance5");
    }

    @Test
    public void testGetConfigNoInstanceWMS() throws ProcessException, NoSuchIdentifierException {

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, GetConfigMapServiceDescriptor.NAME);

        //WMS
        ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(GetConfigMapServiceDescriptor.SERVICE_NAME_NAME).setValue("wms");
        in.parameter(GetConfigMapServiceDescriptor.IDENTIFIER_NAME).setValue("instance10");

        try {
            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex ) {
            //do nohing
        }
    }

}
