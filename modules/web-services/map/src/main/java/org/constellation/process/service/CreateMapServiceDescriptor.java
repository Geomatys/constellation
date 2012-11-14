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
import java.util.HashMap;
import java.util.Map;
import org.constellation.configuration.LayerContext;
import org.constellation.process.ConstellationProcessFactory;
import static org.constellation.process.service.MapProcessUtils.*;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.parameter.ExtendedParameterDescriptor;
import org.geotoolkit.process.AbstractProcessDescriptor;
import org.geotoolkit.process.Process;
import org.geotoolkit.util.SimpleInternationalString;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.util.InternationalString;

/**
 * ProcessDescriptor for create a new Map service like WMS, WMTS, WCS or WFS.
 * @author Quentin Boileau (Geomatys).
 */
public class CreateMapServiceDescriptor extends AbstractProcessDescriptor {


    public static final String NAME = "service.create";
    public static final InternationalString ABSTRACT = new SimpleInternationalString("Get an existing or create a new map service (WMS, WMTS, WFS, WCS) in constellation.");


    public static final String SERVICE_TYPE_NAME = "service_type";
    private static final String SERVICE_TYPE_REMARKS = "The type of the service WMS, WFS, WMTS, WCS.";
    private static final String[] SERVICE_TYPE_VALID_VALUES = SUPPORTED_SERVICE_TYPE.toArray(new String[SUPPORTED_SERVICE_TYPE.size()]);
    public static final ParameterDescriptor<String> SERVICE_TYPE = 
            new ExtendedParameterDescriptor<String>(SERVICE_TYPE_NAME, SERVICE_TYPE_REMARKS, String.class, SERVICE_TYPE_VALID_VALUES, null, null, null, null, true, null);



    public static final String IDENTIFIER_NAME = "identifier";
    private static final String IDENTIFIER_REMARKS = "Identifier of the new service instance.";
    public static final ParameterDescriptor<String> IDENTIFIER =
            new DefaultParameterDescriptor(IDENTIFIER_NAME, IDENTIFIER_REMARKS, String.class, "default", true);


    public static final String CONFIG_NAME = "configuration";
    private static final String CONFIG_REMARKS = "LayerContext object use to configure the instance. If not specified the instance will be configured from default LayerContext.";
    public static final ParameterDescriptor<LayerContext> CONFIGURATION =
            new DefaultParameterDescriptor(CONFIG_NAME, CONFIG_REMARKS, LayerContext.class, null, false);

    public static final String INSTANCE_DIRECTORY_NAME = "instanceDirectory";
    private static final String INSTANCE_DIRECTORY_REMARKS = "Configuration directory. Use default constellation config directory if not set.";
    public static final ParameterDescriptor<File> INSTANCE_DIRECTORY =
            new DefaultParameterDescriptor(INSTANCE_DIRECTORY_NAME, INSTANCE_DIRECTORY_REMARKS, File.class, null, false);

    /**Input parameters */
    public static final ParameterDescriptorGroup INPUT_DESC =
            new DefaultParameterDescriptorGroup("InputParameters",
            new GeneralParameterDescriptor[]{SERVICE_TYPE, IDENTIFIER, CONFIGURATION, INSTANCE_DIRECTORY});


    public static final String OUT_CONFIG_NAME = "out_configuration";
    private static final String OUT_CONFIG_REMARKS = "The LayerContext configuration for the new instance.";
    public static final ParameterDescriptor<LayerContext> OUT_CONFIGURATION =
            new DefaultParameterDescriptor(OUT_CONFIG_NAME, OUT_CONFIG_REMARKS, LayerContext.class, null, false);
    /**Output parameters */
    public static final ParameterDescriptorGroup OUTPUT_DESC = new DefaultParameterDescriptorGroup("OutputParameters",
            new GeneralParameterDescriptor[]{OUT_CONFIGURATION});


    /**
     * Public constructor use by the ServiceRegistry to find and instantiate all ProcessDescriptor.
     */
    public CreateMapServiceDescriptor() {
        super(NAME, ConstellationProcessFactory.IDENTIFICATION, ABSTRACT, INPUT_DESC, OUTPUT_DESC);
    }

    @Override
    public Process createProcess(ParameterValueGroup input) {
        return new CreateMapService(this, input);
    }

}
