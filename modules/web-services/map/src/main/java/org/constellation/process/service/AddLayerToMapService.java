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
import org.constellation.configuration.DimensionDefinition;
import org.constellation.configuration.GetFeatureInfoCfg;
import org.constellation.configuration.Layer;
import org.constellation.process.AbstractCstlProcess;
import org.constellation.util.DataReference;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.sld.xml.StyleXmlIO;
import org.opengis.filter.Filter;
import org.opengis.parameter.ParameterValueGroup;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.constellation.process.service.AddLayerToMapServiceDescriptor.*;
import org.geotoolkit.util.NamesExt;
import static org.geotoolkit.parameter.Parameters.getOrCreate;
import static org.geotoolkit.parameter.Parameters.value;
import org.opengis.util.GenericName;

/**
 * Process that add a new layer layerContext from a webMapService configuration.
 *
 * @author Quentin Boileau (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public class AddLayerToMapService extends AbstractCstlProcess {
	
    @Autowired
    protected ILayerBusiness layerBusiness;
    
    AddLayerToMapService(final ProcessDescriptor desc, final ParameterValueGroup input) {
        super(desc, input);
    }

    public AddLayerToMapService (final String serviceType, final String serviceInstance,
                                 final DataReference layerRef, final String layerAlias,
                                 final DataReference layerStyleRef, final Filter layerFilter,
                                 final String layerDimension, final GetFeatureInfoCfg[] customGFI) {
        this(INSTANCE, toParameters(serviceType, serviceInstance, layerRef, layerAlias, layerStyleRef, layerFilter, layerDimension, customGFI));
    }

    public AddLayerToMapService (final String serviceType, final String serviceInstance,
                                 final DataReference layerRef, final DataReference layerStyleRef) {
        this(INSTANCE, toParameters(serviceType, serviceInstance, layerRef, null, layerStyleRef, null, null, null));
    }

    private static ParameterValueGroup toParameters(final String serviceType, final String serviceInstance,
                                                    final DataReference layerRef, final String layerAlias,
                                                    final DataReference layerStyleRef, final Filter layerFilter,
                                                    final String layerDimension, final GetFeatureInfoCfg[] customGFI){
        final ParameterValueGroup params = INSTANCE.getInputDescriptor().createValue();
        getOrCreate(LAYER_REF, params).setValue(layerRef);
        getOrCreate(LAYER_ALIAS, params).setValue(layerAlias);
        getOrCreate(LAYER_STYLE, params).setValue(layerStyleRef);
        getOrCreate(LAYER_FILTER, params).setValue(layerFilter);
        getOrCreate(LAYER_DIMENSION, params).setValue(layerDimension);
        getOrCreate(LAYER_CUSTOM_GFI, params).setValue(customGFI);
        getOrCreate(SERVICE_TYPE, params).setValue(serviceType);
        getOrCreate(SERVICE_INSTANCE, params).setValue(serviceInstance);
        return params;
    }

    @Override
    protected void execute() throws ProcessException {

        final DataReference layerRef        = value(LAYER_REF, inputParameters);
        final String layerAlias             = value(LAYER_ALIAS, inputParameters);
        final DataReference layerStyleRef   = value(LAYER_STYLE, inputParameters);
        final Filter layerFilter            = value(LAYER_FILTER, inputParameters);
        final String layerDimension         = value(LAYER_DIMENSION, inputParameters);
        final GetFeatureInfoCfg[] customGFI = value(LAYER_CUSTOM_GFI, inputParameters);
        final String serviceType            = value(SERVICE_TYPE, inputParameters);
        final String serviceInstance        = value(SERVICE_INSTANCE, inputParameters);

        //check layer reference
        final String dataType = layerRef.getDataType();
        if (dataType.equals(DataReference.PROVIDER_STYLE_TYPE) || dataType.equals(DataReference.SERVICE_TYPE)) {
            throw new ProcessException("Layer Reference must be a from a layer provider.", this, null);
        }

        //check style from a style provider
        if (layerStyleRef != null && !layerStyleRef.getDataType().equals(DataReference.PROVIDER_STYLE_TYPE)) {
            throw new ProcessException("Layer Style reference must be a from a style provider.", this, null);
        }

        //test alias
        if (layerAlias != null && layerAlias.isEmpty()) {
            throw new ProcessException("Layer alias can't be empty string.", this, null);
        }

        //extract provider identifier and layer name
        final String providerID = layerRef.getProviderOrServiceId();
        final Date dataVersion = layerRef.getDataVersion();
        final GenericName layerName = layerRef.getLayerId();
        final QName layerQName = new QName(NamesExt.getNamespace(layerName), layerName.tip().toString());

        //create future new layer
        final Layer newLayer = new Layer(layerQName);

        //add filter if exist
        if (layerFilter != null) {
            //convert filter int an FilterType
            final StyleXmlIO xmlUtilities = new StyleXmlIO();
            final FilterType filterType = xmlUtilities.getTransformerXMLv110().visit(layerFilter);
            newLayer.setFilter(filterType);
        }


        //add extra dimension
        if (layerDimension != null && !layerDimension.isEmpty()) {
            final DimensionDefinition dimensionDef = new DimensionDefinition();
            dimensionDef.setCrs(layerDimension);
            dimensionDef.setLower(layerDimension);
            dimensionDef.setUpper(layerDimension);
            newLayer.setDimensions(Collections.singletonList(dimensionDef));
        }

        //add style if exist
        if (layerStyleRef != null) {
            final List<DataReference> styles = new ArrayList<>();
            styles.add(layerStyleRef);
            newLayer.setStyles(styles);
        }

        //add alias if exist
        if (layerAlias != null) {
            newLayer.setAlias(layerAlias);
        }

        //forward data version if defined.
        if (dataVersion != null) {
            newLayer.setVersion(dataVersion.getTime());
        }

        //custom GetFeatureInfo
        if (customGFI != null) {
            newLayer.setGetFeatureInfoCfgs(Arrays.asList(customGFI));
        }

        try {
            layerBusiness.add(layerName.tip().toString(), NamesExt.getNamespace(layerName), providerID, layerAlias, serviceInstance, serviceType, newLayer);
        } catch (ConfigurationException ex) {
            throw new ProcessException("Error while saving layer", this, ex);
        }

        //output
        getOrCreate(OUT_LAYER, outputParameters).setValue(newLayer);

    }
}
