/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.process.service;

import org.apache.sis.util.iso.SimpleInternationalString;
import org.constellation.configuration.LayerContext;
import org.constellation.dto.Details;
import org.constellation.process.AbstractCstlProcess;
import org.constellation.process.AbstractCstlProcessDescriptor;
import org.constellation.process.ConstellationProcessFactory;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.utility.parameter.ExtendedParameterDescriptor;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.InternationalString;

import static org.constellation.process.service.ServiceProcessCommon.SUPPORTED_SERVICE_TYPE;

/**
 * ProcessDescriptor for create a new Map service like WMS, WMTS, WCS or WFS.
 * @author Quentin Boileau (Geomatys).
 * @author Benjamin Garcia (Geomatys).
 *
 */
public class CreateServiceDescriptor extends AbstractCstlProcessDescriptor {


    public static final String NAME = "service.create";
    public static final InternationalString ABSTRACT = new SimpleInternationalString("Get an existing or create a new ogc service in constellation.");


    public static final String SERVICE_TYPE_NAME = "service_type";
    private static final String SERVICE_TYPE_REMARKS = "The type of the service WMS, WFS, WMTS, WCS.";
    private static final String[] SERVICE_TYPE_VALID_VALUES = SUPPORTED_SERVICE_TYPE.toArray(new String[SUPPORTED_SERVICE_TYPE.size()]);
    public static final ParameterDescriptor<String> SERVICE_TYPE = 
            new ExtendedParameterDescriptor<>(SERVICE_TYPE_NAME, SERVICE_TYPE_REMARKS, String.class, SERVICE_TYPE_VALID_VALUES, null, null, null, null, true, null);



    public static final String IDENTIFIER_NAME = "identifier";
    private static final String IDENTIFIER_REMARKS = "Identifier of the new service instance.";
    public static final ParameterDescriptor<String> IDENTIFIER =
            new DefaultParameterDescriptor(IDENTIFIER_NAME, IDENTIFIER_REMARKS, String.class, "default", true);


    public static final String CONFIG_NAME = "configuration";
    private static final String CONFIG_REMARKS = "LayerContext object use to configure the instance. If not specified the instance will be configured from default LayerContext.";
    public static final ParameterDescriptor<Object> CONFIGURATION =
            new DefaultParameterDescriptor(CONFIG_NAME, CONFIG_REMARKS, Object.class, null, false);

    public static final String SERVICE_METADATA_NAME = "serviceMetadata";
    private static final String SERVICE_METADATA_REMARKS = "The service metadata for new instance.";
    public static final ParameterDescriptor<Details> SERVICE_METADATA =
            new DefaultParameterDescriptor(SERVICE_METADATA_NAME, SERVICE_METADATA_REMARKS, Details.class, null, false);

    /**Input parameters */
    public static final ParameterDescriptorGroup INPUT_DESC =
            new DefaultParameterDescriptorGroup("InputParameters",
            new GeneralParameterDescriptor[]{SERVICE_TYPE, IDENTIFIER, CONFIGURATION, SERVICE_METADATA});


    public static final String OUT_CONFIG_NAME = "out_configuration";
    private static final String OUT_CONFIG_REMARKS = "The configuration object for the new instance.";
    public static final ParameterDescriptor<LayerContext> OUT_CONFIGURATION =
            new DefaultParameterDescriptor(OUT_CONFIG_NAME, OUT_CONFIG_REMARKS, Object.class, null, false);
    /**Output parameters */
    public static final ParameterDescriptorGroup OUTPUT_DESC = new DefaultParameterDescriptorGroup("OutputParameters",
            new GeneralParameterDescriptor[]{OUT_CONFIGURATION});

    /**
     * Public constructor use by the ServiceRegistry to find and instantiate all ProcessDescriptor.
     */
    public CreateServiceDescriptor() {
        super(NAME, ConstellationProcessFactory.IDENTIFICATION, ABSTRACT, INPUT_DESC, OUTPUT_DESC);
    }

    @Override
    public AbstractCstlProcess buildProcess(ParameterValueGroup input) {
        return new CreateService(this, input);
    }

}
