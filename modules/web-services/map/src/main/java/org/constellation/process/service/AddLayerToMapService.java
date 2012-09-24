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

import org.geotoolkit.process.AbstractProcess;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;
import static org.geotoolkit.parameter.Parameters.*;
import static org.constellation.process.service.AddLayerToMapServiceDescriptor.*;
import static org.constellation.process.service.MapProcessUtils.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.Source;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.util.DataReference;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.parameter.Parameters;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.sld.xml.XMLUtilities;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.util.NoSuchIdentifierException;

/**
 * Process that add a new layer layerContext from a webMapService configuration.
 *
 * @author Quentin Boileau (Geomatys)
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
            getServiceConfProcess = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, CreateMapServiceDescriptor.NAME);
            setServiceConfProcess = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, SetConfigMapServiceDescriptor.NAME);
            restartServiceProcess = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartServiceDescriptor.NAME);
        } catch (NoSuchIdentifierException ex) {
            throw new ProcessException("Can't find GetConfig or SetConfig process.", this, ex);
        }
        
        final DataReference layerRef        = value(LAYER_REF, inputParameters);
        final String layerAlias             = value(LAYER_ALIAS, inputParameters);
        final DataReference layerStyleRef   = value(LAYER_STYLE, inputParameters);
        final Filter layerFilter            = value(LAYER_FILTER, inputParameters);
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
        final String providerID = layerRef.getServiceId();
        final Name layerName = layerRef.getLayerId();
        final QName layerQName = new QName(layerName.getNamespaceURI(), layerName.getLocalPart());

        //create futur new layer
        final Layer newLayer = new Layer(layerQName);

        //add filter if exist
        if (layerFilter != null) {
            //convert filter int an FilterType
            final XMLUtilities xmlUtilities = new XMLUtilities();
            final FilterType filterType = xmlUtilities.getTransformerXMLv110().visit(layerFilter);
            newLayer.setFilter(filterType);
        }

        //add style if exist
        if (layerStyleRef != null) {
            final List<String> styles = new ArrayList<String>();
            styles.add(layerStyleRef.getReference());
            newLayer.setStyles(styles);
        }

        //add alias if exist
        if (layerAlias != null) {
            newLayer.setAlias(layerAlias);
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

                final LayerProvider provider = findProvider(providerID);
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
            final List<Layer> includes = new ArrayList<Layer>();
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
     * @return LaerProvider found or null.
     */
    private LayerProvider findProvider(final String providerID) {

        LayerProvider provider = null;

        final Collection<LayerProvider> layerProviders = LayerProviderProxy.getInstance().getProviders();
        for (final LayerProvider layerProvider : layerProviders) {
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
            in.parameter(CreateMapServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceType);
            in.parameter(CreateMapServiceDescriptor.IDENTIFIER_NAME).setValue(serviceInstance);
            final ParameterValueGroup out = processDesc.createProcess(in).call();
            return Parameters.value(CreateMapServiceDescriptor.OUT_CONFIGURATION, out);
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
            in.parameter(SetConfigMapServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceType);
            in.parameter(SetConfigMapServiceDescriptor.IDENTIFIER_NAME).setValue(serviceInstance);
            in.parameter(SetConfigMapServiceDescriptor.CONFIG_NAME).setValue(context);
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
