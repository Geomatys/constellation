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
import org.constellation.process.ConstellationProcessFactory;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.parameter.ExtendedParameterDescriptor;
import org.geotoolkit.process.AbstractProcessDescriptor;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.InternationalString;
import static org.constellation.process.service.WSProcessUtils.*;
/**
 *
 * @author Quentin Boileau (Geoamtys)
 */
public class GetConfigServiceDescriptor extends AbstractProcessDescriptor {


    public static final String NAME = "service.get_config";
    public static final InternationalString ABSTRACT = new SimpleInternationalString("Get configuration of an existing map service (WMS, WMTS, WFS, WCS) in constellation.");


    public static final String SERVICE_TYPE_NAME = "service_type";
    private static final String SERVICE_TYPE_REMARKS = "The type of the service WMS, WFS, WMTS, WCS.";
    private static final String[] SERVICE_TYPE_VALID_VALUES = SUPPORTED_SERVICE_TYPE.toArray(new String[SUPPORTED_SERVICE_TYPE.size()]);
    public static final ParameterDescriptor<String> SERVICE_TYPE = 
            new ExtendedParameterDescriptor<>(SERVICE_TYPE_NAME, SERVICE_TYPE_REMARKS, String.class, SERVICE_TYPE_VALID_VALUES, null, null, null, null, true, null);


    public static final String IDENTIFIER_NAME = "identifier";
    private static final String IDENTIFIER_REMARKS = "Identifier of the service instance.";
    public static final ParameterDescriptor<String> IDENTIFIER =
            new DefaultParameterDescriptor(IDENTIFIER_NAME, IDENTIFIER_REMARKS, String.class, "default", true);

    public static final String INSTANCE_DIRECTORY_NAME = "instanceDirectory";
    private static final String INSTANCE_DIRECTORY_REMARKS = "Configuration directory. Use default constellation config directory if not set.";
    public static final ParameterDescriptor<File> INSTANCE_DIRECTORY =
            new DefaultParameterDescriptor(INSTANCE_DIRECTORY_NAME, INSTANCE_DIRECTORY_REMARKS, File.class, null, false);

    public static final String CONFIGURATION_CLASS_NAME = "configurationClass";
    private static final String CONFIGURATION_CLASS_REMARKS = "Configuration class of the object.";
    public static final ParameterDescriptor<Class> CONFIGURATION_CLASS =
            new DefaultParameterDescriptor(CONFIGURATION_CLASS_NAME, CONFIGURATION_CLASS_REMARKS, Class.class, null, true);

    public static final String FILENAME_NAME = "fileName";
    private static final String FILENAME_REMARKS = "name of the configuration file.";
    public static final ParameterDescriptor<String> FILENAME =
            new DefaultParameterDescriptor(FILENAME_NAME, FILENAME_REMARKS, String.class, null, true);

    /**Input parameters */
    public static final ParameterDescriptorGroup INPUT_DESC =
            new DefaultParameterDescriptorGroup("InputParameters",
            new GeneralParameterDescriptor[]{SERVICE_TYPE, IDENTIFIER, INSTANCE_DIRECTORY, CONFIGURATION_CLASS, FILENAME});


    public static final String CONFIG_NAME = "configuration";
    private static final String CONFIG_REMARKS = "The configuration object for the specified service instance.";
    public static final ParameterDescriptor<Object> CONFIGURATION =
            new DefaultParameterDescriptor(CONFIG_NAME, CONFIG_REMARKS, Object.class, null, false);

    /**Output parameters */
    public static final ParameterDescriptorGroup OUTPUT_DESC = new DefaultParameterDescriptorGroup("OutputParameters", new GeneralParameterDescriptor[] {CONFIGURATION});

    /**
     * Public constructor use by the ServiceRegistry to find and instantiate all ProcessDescriptor.
     */
    public GetConfigServiceDescriptor() {
        super(NAME, ConstellationProcessFactory.IDENTIFICATION, ABSTRACT, INPUT_DESC, OUTPUT_DESC);
    }

    @Override
    public org.geotoolkit.process.Process createProcess(ParameterValueGroup input) {
        return new GetConfigService(this, input);
    }

}
