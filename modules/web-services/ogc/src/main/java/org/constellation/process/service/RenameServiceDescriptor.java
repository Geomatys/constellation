/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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
import org.apache.sis.util.iso.SimpleInternationalString;
import org.constellation.process.ConstellationProcessFactory;
import static org.constellation.process.service.WSProcessUtils.*;
import org.geotoolkit.process.Process;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.parameter.ExtendedParameterDescriptor;
import org.geotoolkit.process.AbstractProcessDescriptor;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.InternationalString;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class RenameServiceDescriptor extends AbstractProcessDescriptor {

    public static final String NAME = "service.rename";
    public static final InternationalString ABSTRACT = new SimpleInternationalString("rename a new ogc service in constellation.");

    public static final String SERVICE_TYPE_NAME = "service_type";
    private static final String SERVICE_TYPE_REMARKS = "The type of the service WMS, WFS, WMTS, WCS.";
    private static final String[] SERVICE_TYPE_VALID_VALUES = SUPPORTED_SERVICE_TYPE.toArray(new String[SUPPORTED_SERVICE_TYPE.size()]);
    public static final ParameterDescriptor<String> SERVICE_TYPE =
            new ExtendedParameterDescriptor<String>(SERVICE_TYPE_NAME, SERVICE_TYPE_REMARKS, String.class, SERVICE_TYPE_VALID_VALUES, null, null, null, null, true, null);

    public static final String IDENTIFIER_NAME = "identifier";
    private static final String IDENTIFIER_REMARKS = "Identifier of the new service instance.";
    public static final ParameterDescriptor<String> IDENTIFIER =
            new DefaultParameterDescriptor(IDENTIFIER_NAME, IDENTIFIER_REMARKS, String.class, "default", true);

    public static final String NEW_NAME_NAME = "newName";
    private static final String NEW_NAME_REMARKS = "new name of the service instance.";
    public static final ParameterDescriptor<String> NEW_NAME =
            new DefaultParameterDescriptor(NEW_NAME_NAME, NEW_NAME_REMARKS, String.class, null, true);

    public static final String SERVICE_DIRECTORY_NAME = "serviceDirectory";
    private static final String SERVICE_DIRECTORY_REMARKS = "Service directory. Use default constellation config directory if not set.";
    public static final ParameterDescriptor<File> SERVICE_DIRECTORY =
            new DefaultParameterDescriptor(SERVICE_DIRECTORY_NAME, SERVICE_DIRECTORY_REMARKS, File.class, null, false);

    /**Input parameters */
    public static final ParameterDescriptorGroup INPUT_DESC =
            new DefaultParameterDescriptorGroup("InputParameters",
            new GeneralParameterDescriptor[]{SERVICE_TYPE, IDENTIFIER, NEW_NAME, SERVICE_DIRECTORY});

    /**Output parameters */
    public static final ParameterDescriptorGroup OUTPUT_DESC = new DefaultParameterDescriptorGroup("OutputParameters");

    /**
     * Public constructor use by the ServiceRegistry to find and instantiate all ProcessDescriptor.
     */
    public RenameServiceDescriptor() {
        super(NAME, ConstellationProcessFactory.IDENTIFICATION, ABSTRACT, INPUT_DESC, OUTPUT_DESC);
    }

    @Override
    public Process createProcess(ParameterValueGroup input) {
        return new RenameService(this, input);
    }
}
