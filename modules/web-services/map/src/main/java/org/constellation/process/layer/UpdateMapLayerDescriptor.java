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
package org.constellation.process.layer;

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
 * Update a provider layer.
 * @author Quentin Boileau (Geomatys).
 */
public class UpdateMapLayerDescriptor extends AbstractProcessDescriptor {


    public static final String NAME = "updateMapLayer";
    public static final InternationalString ABSTRACT = new SimpleInternationalString("Update a map layer for a specified provider.");

    /*
     * Provider idenifier
     */
    public static final String PROVIDER_ID_NAME = "provider_id";
    private static final String PROVIDER_ID_REMARKS = "Identifier of the provider to update layer.";
    public static final ParameterDescriptor<String> PROVIDER_ID =
            new DefaultParameterDescriptor(PROVIDER_ID_NAME, PROVIDER_ID_REMARKS, String.class, null, true);

    /*
     * Layer name to update
     */
    public static final String LAYER_NAME_NAME = "layer";
    private static final String LAYER_NAME_REMARKS = "The name of he layer to update.";
    public static final ParameterDescriptor<String> LAYER_NAME =
            new DefaultParameterDescriptor(LAYER_NAME_NAME, LAYER_NAME_REMARKS, String.class, null, true);

     /*
     * Updated layer
     */
    public static final String UPDATE_LAYER_NAME = "updateLayer";
    private static final String UPDATE_LAYER_REMARKS = "ParameterValueGroup of the updated layer.";
    public static final ParameterDescriptor<ParameterValueGroup> UPDATE_LAYER =
            new DefaultParameterDescriptor(UPDATE_LAYER_NAME, UPDATE_LAYER_REMARKS, ParameterValueGroup.class, null, true);


    /**Input parameters */
    public static final ParameterDescriptorGroup INPUT_DESC =
            new DefaultParameterDescriptorGroup("InputParameters", new GeneralParameterDescriptor[]{PROVIDER_ID, LAYER_NAME, UPDATE_LAYER});


    /**Output parameters */
    public static final ParameterDescriptorGroup OUTPUT_DESC = new DefaultParameterDescriptorGroup("OutputParameters");


    /**
     * Public constructor use by the ServiceRegistry to find and instantiate all ProcessDescriptor.
     */
    public UpdateMapLayerDescriptor() {
        super(NAME, ConstellationProcessFactory.IDENTIFICATION, ABSTRACT, INPUT_DESC, OUTPUT_DESC);
    }

    @Override
    public org.geotoolkit.process.Process createProcess(ParameterValueGroup input) {
        return new UpdateMapLayer(this, input);
    }

}
