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

import org.constellation.business.ILayerBusiness;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.Layer;
import org.constellation.process.AbstractCstlProcess;
import org.constellation.security.SecurityManagerHolder;
import org.constellation.util.DataReference;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;
import org.springframework.beans.factory.annotation.Autowired;

import static org.constellation.process.service.RemoveLayerFromMapServiceDescriptor.*;
import org.geotoolkit.feature.type.NamesExt;
import static org.geotoolkit.parameter.Parameters.getOrCreate;
import static org.geotoolkit.parameter.Parameters.value;
import org.opengis.util.GenericName;

/**
 * Process that remove a layer from a webMapService configuration.
 *
 * @author Quentin Boileau (Geomatys)
 */
public class RemoveLayerFromMapService extends AbstractCstlProcess {
	
    @Autowired
    protected ILayerBusiness layerBusiness;
    
    RemoveLayerFromMapService(final ProcessDescriptor desc, final ParameterValueGroup input) {
        super(desc, input);
    }

    public RemoveLayerFromMapService(final String serviceType, final String serviceInstance,
                                     final DataReference layerRef) {
        this(INSTANCE, toParameters(serviceType, serviceInstance, layerRef));
    }

    private static ParameterValueGroup toParameters(final String serviceType, final String serviceInstance,
                                                    final DataReference layerRef){
        final ParameterValueGroup params = INSTANCE.getInputDescriptor().createValue();
        getOrCreate(LAYER_REF, params).setValue(layerRef);
        getOrCreate(SERVICE_TYPE, params).setValue(serviceType);
        getOrCreate(SERVICE_INSTANCE, params).setValue(serviceInstance);
        return params;
    }

    @Override
    protected void execute() throws ProcessException {

        final DataReference layerRef        = value(LAYER_REF, inputParameters);
        final String serviceType            = value(SERVICE_TYPE, inputParameters);
        final String serviceInstance        = value(SERVICE_INSTANCE, inputParameters);

        //check layer reference
        final String dataType = layerRef.getDataType();
        if (dataType.equals(DataReference.PROVIDER_STYLE_TYPE) || dataType.equals(DataReference.SERVICE_TYPE)) {
            throw new ProcessException("Layer Reference must be a from a layer provider.", this, null);
        }
        final GenericName layerName = layerRef.getLayerId();

        Layer oldLayer = null;
        try {
            String login = null;
            try {
                login = SecurityManagerHolder.getInstance().getCurrentUserLogin();
            } catch (RuntimeException ex) {
               //do nothing
            }
            oldLayer = layerBusiness.getLayer(serviceType, serviceInstance, layerName.tip().toString(), NamesExt.getNamespace(layerName), login);
            layerBusiness.remove(serviceType, serviceInstance, layerName.tip().toString(), NamesExt.getNamespace(layerName));
        } catch (ConfigurationException ex) {
            throw new ProcessException("Error while saving layer", this, ex);
        }

        //output
        getOrCreate(OLD_LAYER, outputParameters).setValue(oldLayer);
    }
}
