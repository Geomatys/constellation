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
package org.constellation.process.provider.layer;

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
 * @author Quentin Boileau (Geomatys).
 */
public class DeleteProviderLayerStyleDescriptor extends AbstractProcessDescriptor {


    public static final String NAME = "provider.delete_layer_style";
    public static final InternationalString ABSTRACT = new SimpleInternationalString("Delete layer style of a specified provider.");

    /*
     * Provider idenifier
     */
    public static final String PROVIDER_ID_NAME = "provider_id";
    private static final String PROVIDER_ID_REMARKS = "Input provider identifier.";
    public static final ParameterDescriptor<String> PROVIDER_ID =
            new DefaultParameterDescriptor(PROVIDER_ID_NAME, PROVIDER_ID_REMARKS, String.class, null, true);

    /*
     * Layer name to delete
     */
    public static final String LAYER_NAME_NAME = "layerName";
    private static final String LAYER_NAME_REMARKS = "Input layer identifier.";
    public static final ParameterDescriptor<String> LAYER_NAME =
            new DefaultParameterDescriptor(LAYER_NAME_NAME, LAYER_NAME_REMARKS, String.class, null, true);


    /**Input parameters */
    public static final ParameterDescriptorGroup INPUT_DESC =
            new DefaultParameterDescriptorGroup("InputParameters", new GeneralParameterDescriptor[]{PROVIDER_ID, LAYER_NAME});


    /**Output parameters */
    public static final ParameterDescriptorGroup OUTPUT_DESC = new DefaultParameterDescriptorGroup("OutputParameters");


    /**
     * Public constructor use by the ServiceRegistry to find and instantiate all ProcessDescriptor.
     */
    public DeleteProviderLayerStyleDescriptor() {
        super(NAME, ConstellationProcessFactory.IDENTIFICATION, ABSTRACT, INPUT_DESC, OUTPUT_DESC);
    }

    @Override
    public org.geotoolkit.process.Process createProcess(ParameterValueGroup input) {
        return new DeleteProviderLayerStyle(this, input);
    }

}
