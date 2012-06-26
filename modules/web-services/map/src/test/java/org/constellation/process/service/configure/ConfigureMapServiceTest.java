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
package org.constellation.process.service.configure;

import org.constellation.process.service.configure.ConfigureMapServiceDescriptor;
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
import static org.junit.Assert.*;
import org.junit.Test;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;
/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public class ConfigureMapServiceTest  extends AbstractMapServiceTest {
    
    public ConfigureMapServiceTest () {
        super(ConfigureMapServiceDescriptor.NAME);
    }
    
    @Test
    public void testUpdateWMS() throws ProcessException, NoSuchIdentifierException {

        createDefaultInstance("WMS","instance4");
        
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, ConfigureMapServiceDescriptor.NAME);

        final List<Source> sources = new ArrayList<Source>();
        sources.add(new Source("source1", Boolean.TRUE, null, null));
        final Layers layers = new Layers(sources);
        final LayerContext conf = new LayerContext(layers);
        
        //WMS
        ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(ConfigureMapServiceDescriptor.SERVICE_NAME_NAME).setValue("wms");
        in.parameter(ConfigureMapServiceDescriptor.IDENTIFIER_NAME).setValue("instance4");
        in.parameter(ConfigureMapServiceDescriptor.CONFIG_NAME).setValue(conf);
        
        org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        assertEquals(conf, getConfig("instance4"));

        deleteInstance("WMS", "instance4");
    }
    
    @Test
    public void testUpdateNoInstanceWMS() throws ProcessException, NoSuchIdentifierException {

        
        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, ConfigureMapServiceDescriptor.NAME);

        final List<Source> sources = new ArrayList<Source>();
        sources.add(new Source("source1", Boolean.TRUE, null, null));
        final Layers layers = new Layers(sources);
        final LayerContext conf = new LayerContext(layers);
        
        //WMS
        ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(ConfigureMapServiceDescriptor.SERVICE_NAME_NAME).setValue("wms");
        in.parameter(ConfigureMapServiceDescriptor.IDENTIFIER_NAME).setValue("instance10");
        in.parameter(ConfigureMapServiceDescriptor.CONFIG_NAME).setValue(conf);
        
        try {
            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();
            fail();
        } catch (ProcessException ex ) {
            //do nohing
        }

    }
}
