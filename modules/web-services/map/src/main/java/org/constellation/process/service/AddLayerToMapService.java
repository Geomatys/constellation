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

import org.constellation.configuration.*;
import org.geotoolkit.process.AbstractProcess;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;
import static org.geotoolkit.parameter.Parameters.*;
import static org.constellation.process.service.AddLayerToMapServiceDescriptor.*;
import static org.constellation.process.service.WSProcessUtils.*;

import java.util.*;
import javax.xml.namespace.QName;

import org.constellation.process.ConstellationProcessFactory;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.constellation.util.DataReference;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.parameter.Parameters;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.sld.xml.StyleXmlIO;
import org.geotoolkit.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.util.NoSuchIdentifierException;

/**
 * Process that add a new layer layerContext from a webMapService configuration.
 *
 * @author Quentin Boileau (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public class AddLayerToMapService extends AbstractProcess {

    AddLayerToMapService(final ProcessDescriptor desc, final ParameterValueGroup input) {
        super(desc, input);
    }

    @Override
    protected void execute() throws ProcessException {

        final ProcessDescriptor getServiceConfProcess;
        final ProcessDescriptor setServiceConfProcess;
        final ProcessDescriptor restartServiceProcess;
        try {
            getServiceConfProcess = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateServiceDescriptor.NAME);
            setServiceConfProcess = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, SetConfigServiceDescriptor.NAME);
            restartServiceProcess = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartServiceDescriptor.NAME);
        } catch (NoSuchIdentifierException ex) {
            throw new ProcessException("Can't find GetConfig or SetConfig process.", this, ex);
        }

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
            throw new ProcessException("Layer Refrence must be a from a layer provider.", this, null);
        }

        //check style from a style provider
        if (layerStyleRef != null && !layerStyleRef.getDataType().equals(DataReference.PROVIDER_STYLE_TYPE)) {
            throw new ProcessException("Layer Style reference must be a from a style provider.", this, null);
        }

        //test alias
        if (layerAlias != null && layerAlias.isEmpty()) {
            throw new ProcessException("Layer alias can't be empty string.", this, null);
        }

        final LayerContext layerContext = getServiceConfig(serviceType, serviceInstance, getServiceConfProcess);

        //extract provider identifier and layer name
        final String providerID = layerRef.getProviderOrServiceId();
        final Date dataVersion = layerRef.getDataVersion();
        final Name layerName = layerRef.getLayerId();
        final QName layerQName = new QName(layerName.getNamespaceURI(), layerName.getLocalPart());

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


        final List<Source> sourceList = layerContext.getLayers();
        Source source = null;
        if (sourceList != null) {
            for (final Source src : sourceList) {
                if (src.getId().equals(providerID)) {
                    source = src;
                    break;
                }
            }
        }


        if (source != null) {

            if (source.getLoadAll()) {
                source.setLoadAll(false);

                //remove layer from excluded layers
                if (source.isExcludedLayer(layerQName)) {

                    Layer oldLayer = null;
                    for (final Layer excludeLayer : source.getExclude()) {
                        if (excludeLayer.getName().equals(layerQName)) {
                            oldLayer = excludeLayer;
                            break;
                        }
                    }
                    source.getExclude().remove(oldLayer);
                }

                final DataProvider provider = findProvider(providerID);
                if (provider != null) {
                    final Set<Name> avaibleLayers = provider.getKeys();
                    if (avaibleLayers != null) {
                        final Iterator<Name> ite = avaibleLayers.iterator();

                        while (ite.hasNext()) {
                            final Name name = ite.next();
                            final QName qname = new QName(name.getNamespaceURI(), name.getLocalPart());

                            //add all layers from provider except exclude ones.
                            if (!source.isExcludedLayer(qname)) {
                                final Layer layerToAdd = (layerQName.getLocalPart().equals(name.getLocalPart())) ?
                                        newLayer : new Layer(qname);
                                source.getInclude().add(layerToAdd);
                            }
                        }
                    }
                }

            } else {

                //remove layer from include or exclude list
                Layer oldLayer = null;
                if (source.isIncludedLayer(layerQName) != null) {
                    oldLayer = source.isIncludedLayer(layerQName);
                    source.getInclude().remove(oldLayer);
                }
                if (source.isExcludedLayer(layerQName)) {

                    for (final Layer excludeLayer : source.getExclude()) {
                        if (excludeLayer.getName().equals(layerQName)) {
                            oldLayer = excludeLayer;
                            break;
                        }
                    }
                    source.getExclude().remove(oldLayer);
                }

                //add layer to include list.
                source.getInclude().add(newLayer);
            }

        } else {
            //create source with layer.
            final List<Layer> includes = new ArrayList<>();
            includes.add(newLayer);
            final Source newSource = new Source(providerID, false, includes, null);
            layerContext.getLayers().add(newSource);

        }

        //Save configuration
        setServiceConfig(serviceType, serviceInstance, setServiceConfProcess, layerContext);

        //Restart service
        restartService(serviceType, serviceInstance, restartServiceProcess);

        //output
        getOrCreate(OUT_LAYER_CTX, outputParameters).setValue(layerContext);

    }

    /**
     * Find provider from his identifier in registered layer providers.
     *
     * @param providerID
     * @return LayerProvider found or null.
     */
    private DataProvider findProvider(final String providerID) {

        DataProvider provider = null;

        final Collection<DataProvider> layerProviders = DataProviders.getInstance().getProviders();
        for (final DataProvider layerProvider : layerProviders) {
            if (layerProvider.getId().equals(providerID)) {
               provider = layerProvider;
               break;
            }
        }
        return provider;
    }

    /**
     * Call GetConfigMapService process to get the service LayerContext.
     *
     * @param serviceType
     * @param serviceInstance
     * @param processDesc
     * @return service layer context
     * @throws ProcessException if serviceType is not handled or if instance is not found.
     */
    private LayerContext getServiceConfig(final String serviceType, final String serviceInstance, final ProcessDescriptor processDesc) throws ProcessException {
        if (SUPPORTED_SERVICE_TYPE.contains(serviceType)) {
            final ParameterValueGroup in = processDesc.getInputDescriptor().createValue();
            in.parameter(CreateServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceType);
            in.parameter(CreateServiceDescriptor.IDENTIFIER_NAME).setValue(serviceInstance);
            in.parameter(CreateServiceDescriptor.CONFIGURATION_CLASS_NAME).setValue(LayerContext.class);
            final ParameterValueGroup out = processDesc.createProcess(in).call();
            return Parameters.value(CreateServiceDescriptor.OUT_CONFIGURATION, out);
        } else {
            throw new ProcessException("Service name can't be null or empty but one of these (\"WMS\", \"WMTS\", \"WFS\", \"WCS\").", this, null);
        }
    }

    /**
     * Update service configuration.
     * @param serviceType
     * @param serviceInstance
     * @param processDesc
     * @param context
     * @throws ProcessException if serviceType is not handled or if instance is not found.
     */
    private void setServiceConfig(final String serviceType, final String serviceInstance, final ProcessDescriptor processDesc, final LayerContext context) throws ProcessException {

        if (SUPPORTED_SERVICE_TYPE.contains(serviceType)) {
            final ParameterValueGroup in = processDesc.getInputDescriptor().createValue();
            in.parameter(SetConfigServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceType);
            in.parameter(SetConfigServiceDescriptor.IDENTIFIER_NAME).setValue(serviceInstance);
            in.parameter(SetConfigServiceDescriptor.CONFIG_NAME).setValue(context);
            in.parameter(SetConfigServiceDescriptor.CONFIGURATION_CLASS_NAME).setValue(LayerContext.class);
            processDesc.createProcess(in).call();
        } else {
            throw new ProcessException("Service name can't be null or empty but one of these (\"WMS\", \"WMTS\", \"WFS\", \"WCS\").", this, null);
        }
    }

    /**
     * Restart service.
     * @param serviceType
     * @param serviceInstance
     * @param processDesc
     * @throws ProcessException if serviceType is not handled or if instance is not found.
     */
    private void restartService(final String serviceType, final String serviceInstance, final ProcessDescriptor processDesc) throws ProcessException {

        if (SUPPORTED_SERVICE_TYPE.contains(serviceType)) {
            final ParameterValueGroup in = processDesc.getInputDescriptor().createValue();
            in.parameter(RestartServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceType);
            in.parameter(RestartServiceDescriptor.IDENTIFIER_NAME).setValue(serviceInstance);
            in.parameter(RestartServiceDescriptor.CLOSE_NAME).setValue(true);
            processDesc.createProcess(in).call();
        } else {
            throw new ProcessException("Service name can't be null or empty but one of these (\"WMS\", \"WMTS\", \"WFS\", \"WCS\").", this, null);
        }
    }
}
