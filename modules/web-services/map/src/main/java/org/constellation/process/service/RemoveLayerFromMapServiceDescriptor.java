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

import org.apache.sis.util.iso.ResourceInternationalString;
import org.constellation.configuration.Layer;
import org.constellation.process.AbstractCstlProcess;
import org.constellation.process.AbstractCstlProcessDescriptor;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.util.DataReference;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.parameter.ExtendedParameterDescriptor;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.InternationalString;

import static org.constellation.process.service.WSProcessUtils.SUPPORTED_SERVICE_TYPE;

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
 * @author Cédric Briançon (Geomatys)
 */
public class RemoveLayerFromMapServiceDescriptor extends AbstractCstlProcessDescriptor {

    /*
     * Bundle path and keys
     */
    private static final String BUNDLE = "org/constellation/process/service/bundle";
    private static final String ADD_SFLAYER_ABSTRACT_KEY            = "service.remove_layer_Abstract";
    private static final String LAYER_REF_PARAM_REMARKS_KEY         = "service.remove_layer.layerReference";
    private static final String SERVICE_TYPE_PARAM_REMARKS_KEY      = "service.remove_layer.serviceType";
    private static final String SERVICE_INSTANCE_PARAM_REMARKS_KEY  = "service.remove_layer.serviceInstance";
    private static final String OLD_LAYER_PARAM_REMARKS_KEY         = "service.remove_layer.oldLayer";

    /*
     * Name and description
     */
    public static final String NAME = "service.remove_layer";
    public static final InternationalString ABSTRACT = new ResourceInternationalString(BUNDLE, ADD_SFLAYER_ABSTRACT_KEY);

    /*
     * Layer reference
     */
    public static final String LAYER_REF_PARAM_NAME = "layer_reference";
    public static final InternationalString LAYER_REF_PARAM_REMARKS = new ResourceInternationalString(BUNDLE, LAYER_REF_PARAM_REMARKS_KEY);
    public static final ParameterDescriptor<DataReference> LAYER_REF =
            new DefaultParameterDescriptor(LAYER_REF_PARAM_NAME, LAYER_REF_PARAM_REMARKS, DataReference.class, null, true);

    /*
     * Service Type
     */
    public static final String SERVICE_TYPE_PARAM_NAME = "service_type";
    public static final InternationalString SERVICE_TYPE_PARAM_REMARKS = new ResourceInternationalString(BUNDLE, SERVICE_TYPE_PARAM_REMARKS_KEY);
    private static final String[] SERVICE_TYPE_VALID_VALUES = SUPPORTED_SERVICE_TYPE.toArray(new String[SUPPORTED_SERVICE_TYPE.size()]);
    public static final ParameterDescriptor<String> SERVICE_TYPE =
            new ExtendedParameterDescriptor<String>(SERVICE_TYPE_PARAM_NAME, SERVICE_TYPE_PARAM_REMARKS, String.class, SERVICE_TYPE_VALID_VALUES, "WMS", null, null, null, true, null);

    /*
     * Service instance name
     */
    public static final String SERVICE_INSTANCE_PARAM_NAME = "service_instance";
    public static final InternationalString SERVICE_INSTANCE_PARAM_REMARKS = new ResourceInternationalString(BUNDLE, SERVICE_INSTANCE_PARAM_REMARKS_KEY);
    public static final ParameterDescriptor<String> SERVICE_INSTANCE =
            new DefaultParameterDescriptor(SERVICE_INSTANCE_PARAM_NAME, SERVICE_INSTANCE_PARAM_REMARKS, String.class, null, true);

     /**Input parameters */
    public static final ParameterDescriptorGroup INPUT_DESC =
            new DefaultParameterDescriptorGroup("InputParameters",
            new GeneralParameterDescriptor[]{LAYER_REF, SERVICE_TYPE, SERVICE_INSTANCE});

    /*
     * Output Layer context
     */
    public static final String OLD_LAYER_PARAM_NAME = "old_layer";
    public static final InternationalString OLD_LAYER_PARAM_REMARKS = new ResourceInternationalString(BUNDLE, OLD_LAYER_PARAM_REMARKS_KEY);
    public static final ParameterDescriptor<Layer> OLD_LAYER =
            new DefaultParameterDescriptor(OLD_LAYER_PARAM_NAME, OLD_LAYER_PARAM_REMARKS, Layer.class, null, true);

    /**Output parameters */
    public static final ParameterDescriptorGroup OUTPUT_DESC = new DefaultParameterDescriptorGroup("OutputParameters",
            new GeneralParameterDescriptor[]{OLD_LAYER});


    public RemoveLayerFromMapServiceDescriptor() {
        super(NAME, ConstellationProcessFactory.IDENTIFICATION, ABSTRACT, INPUT_DESC, OUTPUT_DESC);
    }

    public static final RemoveLayerFromMapServiceDescriptor INSTANCE = new RemoveLayerFromMapServiceDescriptor();

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractCstlProcess buildProcess(final ParameterValueGroup input) {
        return new RemoveLayerFromMapService(this, input);
    }

}
