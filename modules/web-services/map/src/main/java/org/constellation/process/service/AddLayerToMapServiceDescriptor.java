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

import org.constellation.configuration.LayerContext;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.util.DataReference;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import static org.constellation.process.service.MapProcessUtils.*;
import org.geotoolkit.parameter.ExtendedParameterDescriptor;
import org.geotoolkit.process.AbstractProcessDescriptor;
import org.geotoolkit.process.Process;
import org.geotoolkit.util.ResourceInternationalString;
import org.opengis.filter.Filter;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.InternationalString;

/**
 * Add a layer to a map service. If service instance doesn't exist, process will create it.
 * Inputs :
 * - layer_reference : reference to data
 * - layer_alias : alias used for this layer in service
 * - layer_style_reference : reference to style used by this layer
 * - layer_filter : CQL filter for the layer
 * - service_type : service type where layer will be published like (WMS, WFS, WMTS, WCS)
 * - service_instance : service instance name where layer will be published.
 * 
 * Outputs : 
 * - layer_context : result service configuration.
 * 
 * @author Quentin Boileau (Geomatys)
 */
public class AddLayerToMapServiceDescriptor extends AbstractProcessDescriptor {

    /*
     * Bundle path and keys
     */
    private static final String BUNDLE = "org/constellation/process/service/bundle";
    private static final String ADD_SFLAYER_ABSTRACT_KEY            = "service.add_layer_Abstract";
    private static final String LAYER_REF_PARAM_REMARKS_KEY         = "service.add_layer.layerReference";
    private static final String LAYER_ALIAS_PARAM_REMARKS_KEY       = "service.add_layer.layerAlias";
    private static final String LAYER_STYLE_PARAM_REMARKS_KEY       = "service.add_layer.layerStyleRef";
    private static final String LAYER_FILTER_PARAM_REMARKS_KEY      = "service.add_layer.layerFilter";
    private static final String SERVICE_TYPE_PARAM_REMARKS_KEY      = "service.add_layer.serviceType";
    private static final String SERVICE_INSTANCE_PARAM_REMARKS_KEY  = "service.add_layer.serviceInstance";
    private static final String OUT_LAYER_CTX_PARAM_REMARKS_KEY     = "service.add_layer.outLayerContext";
    
    /*
     * Name and description
     */
    public static final String NAME = "service.add_layer";
    public static final InternationalString ABSTRACT = new ResourceInternationalString(BUNDLE, ADD_SFLAYER_ABSTRACT_KEY);

    /*
     * Layer reference
     */
    public static final String LAYER_REF_PARAM_NAME = "layer_reference";
    public static final InternationalString LAYER_REF_PARAM_REMARKS = new ResourceInternationalString(BUNDLE, LAYER_REF_PARAM_REMARKS_KEY);
    public static final ParameterDescriptor<DataReference> LAYER_REF =
            new DefaultParameterDescriptor(LAYER_REF_PARAM_NAME, LAYER_REF_PARAM_REMARKS, DataReference.class, null, true);

    /*
     * Layer alias
     */
    public static final String LAYER_ALIAS_PARAM_NAME = "layer_alias";
    public static final InternationalString LAYER_ALIAS_PARAM_REMARKS = new ResourceInternationalString(BUNDLE, LAYER_ALIAS_PARAM_REMARKS_KEY);
    public static final ParameterDescriptor<String> LAYER_ALIAS =
            new DefaultParameterDescriptor(LAYER_ALIAS_PARAM_NAME, LAYER_ALIAS_PARAM_REMARKS, String.class, null, false);

    /*
     * Layer Style
     */
    public static final String LAYER_STYLE_PARAM_NAME = "layer_style_reference";
    public static final InternationalString LAYER_STYLE_PARAM_REMARKS = new ResourceInternationalString(BUNDLE, LAYER_STYLE_PARAM_REMARKS_KEY);
    public static final ParameterDescriptor<DataReference> LAYER_STYLE =
            new DefaultParameterDescriptor(LAYER_STYLE_PARAM_NAME, LAYER_STYLE_PARAM_REMARKS, DataReference.class, null, false);

    /*
     * Layer filter
     */
    public static final String LAYER_FILTER_PARAM_NAME = "layer_filter";
    public static final InternationalString LAYER_FILTER_PARAM_REMARKS = new ResourceInternationalString(BUNDLE, LAYER_FILTER_PARAM_REMARKS_KEY);
    public static final ParameterDescriptor<Filter> LAYER_FILTER =
            new DefaultParameterDescriptor(LAYER_FILTER_PARAM_NAME, LAYER_FILTER_PARAM_REMARKS, Filter.class, null, false);

    /*
     * Service Type
     */
    public static final String SERVICE_TYPE_PARAM_NAME = "service_type";
    public static final InternationalString SERVICE_TYPE_PARAM_REMARKS = new ResourceInternationalString(BUNDLE, SERVICE_TYPE_PARAM_REMARKS_KEY);
    private static final String[] SERVICE_TYPE_VALID_VALUES = SUPPORTED_SERVICE_TYPE.toArray(new String[SUPPORTED_SERVICE_TYPE.size()]);
    public static final ParameterDescriptor<String> SERVICE_TYPE =
            new ExtendedParameterDescriptor<String>(SERVICE_TYPE_PARAM_NAME, SERVICE_TYPE_PARAM_REMARKS, String.class, SERVICE_TYPE_VALID_VALUES, "WMS", null, null, null, true, null);
    
    /*
     * Service insance name
     */
    public static final String SERVICE_INSTANCE_PARAM_NAME = "service_instance";
    public static final InternationalString SERVICE_INSTANCE_PARAM_REMARKS = new ResourceInternationalString(BUNDLE, SERVICE_INSTANCE_PARAM_REMARKS_KEY);
    public static final ParameterDescriptor<String> SERVICE_INSTANCE =
            new DefaultParameterDescriptor(SERVICE_INSTANCE_PARAM_NAME, SERVICE_INSTANCE_PARAM_REMARKS, String.class, null, true);

     /**Input parameters */
    public static final ParameterDescriptorGroup INPUT_DESC =
            new DefaultParameterDescriptorGroup("InputParameters",
            new GeneralParameterDescriptor[]{LAYER_REF, SERVICE_TYPE, SERVICE_INSTANCE, LAYER_ALIAS, LAYER_STYLE, LAYER_FILTER});

    /*
     * Output Layer context
     */
    public static final String OUT_LAYER_CTX_PARAM_NAME = "layer_context";
    public static final InternationalString OUT_LAYER_CTX_PARAM_REMARKS = new ResourceInternationalString(BUNDLE, OUT_LAYER_CTX_PARAM_REMARKS_KEY);
    public static final ParameterDescriptor<LayerContext> OUT_LAYER_CTX =
            new DefaultParameterDescriptor(OUT_LAYER_CTX_PARAM_NAME, OUT_LAYER_CTX_PARAM_REMARKS, LayerContext.class, null, true);

    /**Output parameters */
    public static final ParameterDescriptorGroup OUTPUT_DESC = new DefaultParameterDescriptorGroup("OutputParameters",
            new GeneralParameterDescriptor[]{OUT_LAYER_CTX});


    public AddLayerToMapServiceDescriptor() {
        super(NAME, ConstellationProcessFactory.IDENTIFICATION, ABSTRACT, INPUT_DESC, OUTPUT_DESC);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Process createProcess(ParameterValueGroup input) {
        return new AddLayerToMapService(this, input);
    }


}
