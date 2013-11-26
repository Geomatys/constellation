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
package org.constellation.process.service;

import java.util.ArrayList;
import java.util.List;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.Layers;
import org.constellation.configuration.Source;
import org.constellation.process.ConstellationProcessFactory;
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
public abstract class SetConfigMapServiceTest  extends AbstractMapServiceTest {

    public SetConfigMapServiceTest (final String serviceName, final Class workerClass) {
        super(SetConfigServiceDescriptor.NAME,serviceName, workerClass);
    }

    @Test
    public void testUpdate() throws ProcessException, NoSuchIdentifierException {

        createInstance("updateInstance4", null);

        try {
            final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, SetConfigServiceDescriptor.NAME);

            final List<Source> sources = new ArrayList<>();
            sources.add(new Source("source1", Boolean.TRUE, new ArrayList<Layer>(), null));
            final Layers layers = new Layers(sources);
            final LayerContext conf = new LayerContext(layers);

            //WMS
            ParameterValueGroup in = desc.getInputDescriptor().createValue();
            in.parameter(SetConfigServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
            in.parameter(SetConfigServiceDescriptor.IDENTIFIER_NAME).setValue("updateInstance4");
            in.parameter(SetConfigServiceDescriptor.CONFIG_NAME).setValue(conf);
            in.parameter(SetConfigServiceDescriptor.CONFIGURATION_CLASS_NAME).setValue(LayerContext.class);


            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();

            assertEquals(conf, getConfig("updateInstance4"));
        } finally {
            deleteInstance("updateInstance4");
        }
    }

    @Test
    public void testUpdateNoInstance() throws ProcessException, NoSuchIdentifierException {


        try {
            final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, SetConfigServiceDescriptor.NAME);

            final List<Source> sources = new ArrayList<>();
            sources.add(new Source("source1", Boolean.TRUE, null, null));
            final Layers layers = new Layers(sources);
            final LayerContext conf = new LayerContext(layers);

            final ParameterValueGroup in = desc.getInputDescriptor().createValue();
            in.parameter(SetConfigServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceName);
            in.parameter(SetConfigServiceDescriptor.IDENTIFIER_NAME).setValue("instance10");
            in.parameter(SetConfigServiceDescriptor.CONFIG_NAME).setValue(conf);
            in.parameter(SetConfigServiceDescriptor.CONFIGURATION_CLASS_NAME).setValue(LayerContext.class);


            org.geotoolkit.process.Process proc = desc.createProcess(in);
            proc.call();

            assertEquals(conf, getConfig("instance10"));
        } finally {
            deleteInstance("instance10");
        }
    }
}
