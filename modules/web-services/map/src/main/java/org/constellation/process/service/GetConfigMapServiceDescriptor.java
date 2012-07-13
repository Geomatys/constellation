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

import java.io.File;
import org.constellation.configuration.LayerContext;
import org.constellation.process.ConstellationProcessFactory;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.process.AbstractProcessDescriptor;
import org.geotoolkit.util.SimpleInternationalString;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.InternationalString;

/**
 *
 * @author Quentin Boileau (Geoamtys)
 */
public class GetConfigMapServiceDescriptor extends AbstractProcessDescriptor {


    public static final String NAME = "map_service.getConfig";
    public static final InternationalString ABSTRACT = new SimpleInternationalString("Get configuration of an existing map service (WMS, WMTS, WFS) in constellation.");


    public static final String SERVICE_NAME_NAME = "service_Name";
    private static final String SERVICE_NAME_REMARKS = "The name of the service (WMS, WMTS, WFS).";
    public static final ParameterDescriptor<String> SERVICE_NAME =
            new DefaultParameterDescriptor(SERVICE_NAME_NAME, SERVICE_NAME_REMARKS, String.class, null, true);


    public static final String IDENTIFIER_NAME = "identifier";
    private static final String IDENTIFIER_REMARKS = "Identifier of the service instance.";
    public static final ParameterDescriptor<String> IDENTIFIER =
            new DefaultParameterDescriptor(IDENTIFIER_NAME, IDENTIFIER_REMARKS, String.class, "default", true);

    public static final String INSTANCE_DIRECTORY_NAME = "instanceDirectory";
    private static final String INSTANCE_DIRECTORY_REMARKS = "Configuration directory. Use default constellation config directory if not set.";
    public static final ParameterDescriptor<File> INSTANCE_DIRECTORY =
            new DefaultParameterDescriptor(INSTANCE_DIRECTORY_NAME, INSTANCE_DIRECTORY_REMARKS, File.class, null, false);

    /**Input parameters */
    public static final ParameterDescriptorGroup INPUT_DESC =
            new DefaultParameterDescriptorGroup("InputParameters",
            new GeneralParameterDescriptor[]{SERVICE_NAME, IDENTIFIER, INSTANCE_DIRECTORY});


    public static final String CONFIG_NAME = "configuration";
    private static final String CONFIG_REMARKS = "The LayerContext configuration for the specified service instance.";
    public static final ParameterDescriptor<LayerContext> CONFIGURATION =
            new DefaultParameterDescriptor(CONFIG_NAME, CONFIG_REMARKS, LayerContext.class, null, false);

    /**Output parameters */
    public static final ParameterDescriptorGroup OUTPUT_DESC = new DefaultParameterDescriptorGroup("OutputParameters", new GeneralParameterDescriptor[] {CONFIGURATION});

    /**
     * Public constructor use by the ServiceRegistry to find and instantiate all ProcessDescriptor.
     */
    public GetConfigMapServiceDescriptor() {
        super(NAME, ConstellationProcessFactory.IDENTIFICATION, ABSTRACT, INPUT_DESC, OUTPUT_DESC);
    }

    @Override
    public org.geotoolkit.process.Process createProcess(ParameterValueGroup input) {
        return new GetConfigMapService(this, input);
    }

}
