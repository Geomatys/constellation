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

package org.constellation.map.configuration;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import java.awt.*;

import org.apache.sis.storage.DataStoreException;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.dao.LayerRecord;
import org.constellation.configuration.ConfigProcessException;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.Instance;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.Source;
import org.constellation.configuration.TargetNotFoundException;
import org.constellation.dto.*;
import org.constellation.map.featureinfo.FeatureInfoUtilities;
import org.constellation.ogc.configuration.OGCConfigurer;
import org.constellation.process.service.AddLayerToMapServiceDescriptor;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.constellation.provider.StyleProvider;
import org.constellation.provider.StyleProviders;
import org.constellation.provider.configuration.ProviderParameters;
import org.constellation.util.DataReference;
import org.constellation.webservice.map.component.StyleBusiness;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.LayerProviders;
import org.constellation.ws.rs.MapUtilities;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.MutableStyleFactory;
import org.geotoolkit.style.function.InterpolationPoint;
import org.geotoolkit.style.function.Method;
import org.geotoolkit.style.function.Mode;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.style.ChannelSelection;
import org.opengis.style.ColorMap;
import org.opengis.style.ContrastEnhancement;
import org.opengis.style.ContrastMethod;
import org.opengis.style.Description;
import org.opengis.style.OverlapBehavior;
import org.opengis.style.RasterSymbolizer;
import org.opengis.style.ShadedRelief;
import org.opengis.style.Symbolizer;

import javax.inject.Inject;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.xml.namespace.QName;

import static org.geotoolkit.style.StyleConstants.DEFAULT_CATEGORIZE_LOOKUP;
import static org.geotoolkit.style.StyleConstants.DEFAULT_DESCRIPTION;
import static org.geotoolkit.style.StyleConstants.DEFAULT_FALLBACK;
import static org.geotoolkit.style.StyleConstants.DEFAULT_GEOM;
import static org.geotoolkit.style.StyleConstants.LITERAL_ONE_FLOAT;

/**
 * {@link org.constellation.configuration.ServiceConfigurer} base for "map" services.
 *
 * @author Fabien Bernard (Geomatys).
 * @author Benjamin Garcia (Geomatys).
 * @author Cédric Briançon (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public class MapConfigurer extends OGCConfigurer {
    
    @Inject
    StyleBusiness styleBusiness;

    /**
     * Create a new {@link MapConfigurer} instance.
     *
     */
    public MapConfigurer() {
        super(LayerContext.class);
    }

    /**
     * Adds a new layer to a "map" service instance.
     *
     * @param addLayerData the layer to be added
     * @throws TargetNotFoundException if the service with specified identifier does not exist
     * @throws ConfigurationException if the operation has failed for any reason
     */
    public void addLayer(final AddLayer addLayerData) throws ConfigurationException {
        this.ensureExistingInstance(addLayerData.getServiceType(), addLayerData.getServiceId());

        final DataProvider provider = DataProviders.getInstance().getProvider(addLayerData.getProviderId());
        final String namespace;
        if (addLayerData.getLayerNamespace() != null) {
            namespace = addLayerData.getLayerNamespace();
        } else {
            namespace = ProviderParameters.getNamespace(provider);
        }
        final String layerId = (namespace != null && !namespace.isEmpty()) ? "{" + namespace + "}" + addLayerData.getLayerId() : addLayerData.getLayerId();

        // Set layer provider reference.
        final DataReference layerProviderReference = DataReference.createProviderDataReference(DataReference.PROVIDER_LAYER_TYPE, addLayerData.getProviderId(), layerId);

        // Set style provider reference.
        DataReference styleProviderReference;
        try {
            final DataDescription dataDescription = LayerProviders.getDataDescription(addLayerData.getProviderId(), addLayerData.getLayerId());
            if (dataDescription instanceof FeatureDataDescription) {
                final PropertyDescription geometryProp = ((FeatureDataDescription) dataDescription).getGeometryProperty();
                if (Polygon.class.isAssignableFrom(geometryProp.getType()) || MultiPolygon.class.isAssignableFrom(geometryProp.getType())) {
                    styleProviderReference = DataReference.createProviderDataReference(DataReference.PROVIDER_STYLE_TYPE, "sld", "default-polygon");
                } else if (LineString.class.isAssignableFrom(geometryProp.getType()) || MultiLineString.class.isAssignableFrom(geometryProp.getType()) || LinearRing.class.isAssignableFrom(geometryProp.getType())) {
                    styleProviderReference = DataReference.createProviderDataReference(DataReference.PROVIDER_STYLE_TYPE, "sld", "default-line");
                } else {
                    styleProviderReference = DataReference.createProviderDataReference(DataReference.PROVIDER_STYLE_TYPE, "sld", "default-point");
                }
            } else {
                styleProviderReference = generateDefaultStyleProviderReference((CoverageDataDescription)dataDescription, addLayerData.getLayerId());
            }
        } catch (IOException | DataStoreException | CstlServiceException ex) {
            LOGGER.log(Level.INFO, "Error when trying to find an appropriate default style. Fallback to a raster style.");
            styleProviderReference = DataReference.createProviderDataReference(DataReference.PROVIDER_STYLE_TYPE, "sld", "default-raster");
        }

        // Declare the style as "applicable" for the layer data.
//        try {
//            final QName layerID = new QName(layerProviderReference.getLayerId().getNamespaceURI(), layerProviderReference.getLayerId().getLocalPart());
//            styleBusiness.linkToData(
//                    styleProviderReference.getProviderId(),
//                    styleProviderReference.getLayerId().getLocalPart(),
//                    layerProviderReference.getProviderId(),
//                    layerID);
//        } catch (ConfigurationException ex) {
//            LOGGER.log(Level.WARNING, "Error when associating layer default style to the layer source data.", ex);
//        }

        // Build descriptor.
        final ProcessDescriptor desc = getProcessDescriptor("service.add_layer");
        final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
        inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_REF_PARAM_NAME).setValue(layerProviderReference);
        inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_ALIAS_PARAM_NAME).setValue(addLayerData.getLayerAlias());
        inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_STYLE_PARAM_NAME).setValue(styleProviderReference);
        inputs.parameter(AddLayerToMapServiceDescriptor.SERVICE_TYPE_PARAM_NAME).setValue(addLayerData.getServiceType());
        inputs.parameter(AddLayerToMapServiceDescriptor.SERVICE_INSTANCE_PARAM_NAME).setValue(addLayerData.getServiceId());

        // Call process.
        try {
            desc.createProcess(inputs).call();
        } catch (ProcessException ex) {
            throw new ConfigProcessException("Process to add a layer has reported an error.", ex);
        }
    }

    private DataReference generateDefaultStyleProviderReference(final CoverageDataDescription covDesc, final String layerName) throws IOException, DataStoreException, CstlServiceException {
    	
    	// Determine if we should apply this palette (should be applied only for geophysics data!)
        // HACK: normally we should test if the view types set contains photographic, but this is not working here
        // because all coverage readers seems to have it ... so just test the number of sample dimensions.
        // It won't work for all cases ...
        // TODO: fix netcdf reader, should not add photographic in the view types possibilities
        if (covDesc.getBands().isEmpty() || covDesc.getBands().size() == 3 || covDesc.getBands().size()  == 4) {
        //if (coverage.getViewTypes().contains(ViewType.PHOTOGRAPHIC)) {
            // should be RGB, no need to apply a palette, let the renderer display this image unchanged
        	 return DataReference.createProviderDataReference(DataReference.PROVIDER_STYLE_TYPE, "sld", "default-raster");
        }
    	
        double min = covDesc.getBands().get(0).getMinValue();
        double max = covDesc.getBands().get(0).getMaxValue();

        // Style.
        final MutableStyleFactory SF = (MutableStyleFactory) FactoryFinder.getStyleFactory(
                new Hints(Hints.STYLE_FACTORY, MutableStyleFactory.class));

        double average = (max + min) / 2;
        final List<InterpolationPoint> values = new ArrayList<>();
        values.add(SF.interpolationPoint(Float.NaN, SF.literal(new Color(0, 0, 0, 0))));
        values.add(SF.interpolationPoint(min, SF.literal(new Color(0, 54, 204, 255))));
        values.add(SF.interpolationPoint(average, SF.literal(new Color(255, 254, 162, 255))));
        values.add(SF.interpolationPoint(max, SF.literal(new Color(199, 8, 30, 255))));
        final Expression lookup = DEFAULT_CATEGORIZE_LOOKUP;
        final Literal fallback = DEFAULT_FALLBACK;
        final Function function = SF.interpolateFunction(
                lookup, values, Method.COLOR, Mode.LINEAR, fallback);

        final ChannelSelection selection = SF.channelSelection(SF.selectedChannelType("0", (ContrastEnhancement) null));
        final Expression opacity = LITERAL_ONE_FLOAT;
        final OverlapBehavior overlap = OverlapBehavior.LATEST_ON_TOP;
        final ColorMap colorMap = SF.colorMap(function);
        final ContrastEnhancement enhance = SF.contrastEnhancement(LITERAL_ONE_FLOAT, ContrastMethod.NONE);
        final ShadedRelief relief = SF.shadedRelief(LITERAL_ONE_FLOAT);
        final Symbolizer outline = null;
        final Unit uom = NonSI.PIXEL;
        final String geom = DEFAULT_GEOM;
        final String symbName = "raster symbol name";
        final Description desc = DEFAULT_DESCRIPTION;

        final RasterSymbolizer symbol = SF.rasterSymbolizer(
                symbName, geom, desc, uom, opacity, selection, overlap, colorMap, enhance, relief, outline);
        final MutableStyle style = SF.style(symbol);

        style.setDefaultSpecification(symbol);


        final StyleProvider provider = StyleProviders.getInstance().getProvider("sld");
        // Add style into provider.
        final String styleName = "default_"+ layerName;
        provider.set(styleName, style);

        return DataReference.createProviderDataReference(DataReference.PROVIDER_STYLE_TYPE, "sld", styleName);
    }

    /**
     * Extracts and returns the list of {@link Layer}s available on a "map" service.
     *
     * @param identifier the service identifier
     * @return the {@link Layer} list
     * @throws TargetNotFoundException if the service with specified identifier does not exist
     * @throws ConfigurationException if the operation has failed for any reason
     */
    public List<Layer> getLayers(final String spec, final String identifier) throws ConfigurationException {
        this.ensureExistingInstance(spec, identifier);

        // Extracts the layer list from service configuration.
        final LayerContext layerContext = (LayerContext) this.getInstanceConfiguration(spec, identifier);
        List<Layer> layers = MapUtilities.getConfigurationLayers(layerContext, null, null);

        for (Layer layer : layers) {
            final LayerRecord record = ConfigurationEngine.getLayer(identifier, spec, layer.getName());
            if (record != null) {
                layer.setDate(record.getDate());
                layer.setOwner(record.getOwnerLogin());
            }
         }

        return layers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Instance getInstance(String spec, String identifier) throws ConfigurationException {
        final Instance instance = super.getInstance(spec, identifier);
        instance.setLayersNumber(getLayers(spec, identifier).size());
        return instance;
    }

    /**
     * Remove a layer from a service.
     *
     * @param spec service type.
     * @param serviceId the service identifier
     * @param layerId the layer to remove
     * @throws ConfigurationException
     */
    public void removeLayer(final String spec, final String serviceId, final QName layerId) throws ConfigurationException {
        try {
            final LayerContext layerContext = (LayerContext) ConfigurationEngine.getConfiguration(spec, serviceId);
            final List<Source> sources = layerContext.getLayers();
            QName name = null;
            boolean found = false;

            for (Source source : sources) {
                List<Layer> layers = source.getInclude();
                for (Layer layer : layers) {
                    if(layer.getName().equals(layerId)){
                        name = layer.getName();
                        layers.remove(layer);
                        found = true;
                        break;
                    }
                }
                if(found){
                    break;
                }
            }

            if(found){
                ConfigurationEngine.storeConfiguration(spec, serviceId, layerContext);
                ConfigurationEngine.deleteLayer(serviceId, spec, name);
                restartInstance(spec, serviceId, true);
            }

        } catch (Exception e) {
            throw new ConfigurationException("Error when trying to remove a layer from the service "+ serviceId, e);
        }
    }

//    /**
//     * Update layer style for the given service.
//     * @param serviceId
//     * @param layerId
//     * @param spId
//     * @param styleName
//     * @throws ConfigurationException
//     */
//    public void updateLayerStyle(final String spec, final String serviceId, final String layerId, final String spId, final String styleName) throws ConfigurationException {
//        try {
//            final LayerContext layerContext = (LayerContext) ConfigurationEngine.getConfiguration(spec, serviceId);
//            final List<Source> sources = layerContext.getLayers();
//            boolean found = false;
//
//            for (Source source : sources) {
//                List<Layer> layers = source.getInclude();
//                for (Layer layer : layers) {
//                    if (layer.getName().getLocalPart().equals(layerId)) {
//                        layer.setStyles(Collections.singletonList(DataReference.createProviderDataReference(DataReference.PROVIDER_STYLE_TYPE, spId, styleName)));
//                        found = true;
//                        break;
//                    }
//                }
//                if(found){
//                    break;
//                }
//            }
//
//            if(found){
//                ConfigurationEngine.storeConfiguration(spec, serviceId, layerContext);
//                restartInstance(spec, serviceId, true);
//            }
//
//        } catch (Exception e) {
//            throw new ConfigurationException("Error when trying to remove a layer from the service "+ serviceId, e);
//        }
//    }

//    /**
//     * Remove layer style for the given service.
//     * @param serviceId
//     * @param layerId
//     * @param spId
//     * @param styleName
//     * @throws ConfigurationException
//     */
//    public void removeLayerStyle(final String spec, final String serviceId, final String layerId, final String spId, final String styleName) throws ConfigurationException {
//        try {
//            final LayerContext layerContext = (LayerContext) ConfigurationEngine.getConfiguration(spec, serviceId);
//            final List<Source> sources = layerContext.getLayers();
//            boolean found = false;
//
//            for (Source source : sources) {
//                List<Layer> layers = source.getInclude();
//                for (Layer layer : layers) {
//                    if (layer.getName().getLocalPart().equals(layerId)) {
//                        layer.setStyles(new ArrayList<DataReference>());
//                        found = true;
//                        break;
//                    }
//                }
//                if(found){
//                    break;
//                }
//            }
//
//            if(found){
//                ConfigurationEngine.storeConfiguration(spec, serviceId, layerContext);
//                restartInstance(spec, serviceId, true);
//            }
//
//        } catch (Exception e) {
//            throw new ConfigurationException("Error when trying to remove a layer from the service "+ serviceId, e);
//        }
//    }
}
