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

package org.constellation.map.configuration;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import java.awt.*;
import java.io.FileNotFoundException;

import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.logging.Logging;
import org.constellation.ServiceDef.Specification;
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
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.provider.style.SetStyleToStyleProviderDescriptor;
import org.constellation.process.service.AddLayerToMapServiceDescriptor;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.StyleProvider;
import org.constellation.provider.StyleProviderProxy;
import org.constellation.provider.configuration.ProviderParameters;
import org.constellation.util.DataReference;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.LayerProviders;
import org.constellation.ws.rs.MapUtilities;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
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

import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
 * @version 0.9
 * @since 0.9
 */
public class MapConfigurer extends OGCConfigurer {

    private static final Logger LOGGER = Logging.getLogger(MapConfigurer.class);

    /**
     * Create a new {@link MapConfigurer} instance.
     *
     * @param specification  the target service specification
     */
    public MapConfigurer(final Specification specification) {
        super(specification, LayerContext.class, "layerContext.xml");
    }

    /**
     * Adds a new layer to a "map" service instance.
     *
     * @param addLayerData the layer to be added
     * @throws TargetNotFoundException if the service with specified identifier does not exist
     * @throws ConfigurationException if the operation has failed for any reason
     */
    public void addLayer(final AddLayer addLayerData) throws ConfigurationException {
        this.ensureExistingInstance(addLayerData.getServiceId());

        final LayerProvider provider = LayerProviderProxy.getInstance().getProvider(addLayerData.getProviderId());
        final String namespace = ProviderParameters.getNamespace(provider);
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
        try {
            final QName layerID = new QName(layerProviderReference.getLayerId().getNamespaceURI(), layerProviderReference.getLayerId().getLocalPart());
            StyleProviderConfig.linkToData(
                    styleProviderReference.getProviderId(),
                    styleProviderReference.getLayerId().getLocalPart(),
                    layerProviderReference.getProviderId(),
                    layerID);
        } catch (ConfigurationException ex) {
            LOGGER.log(Level.WARNING, "Error when associating layer default style to the layer source data.", ex);
        }

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


        final StyleProvider provider = StyleProviderProxy.getInstance().getProvider("sld");
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
    public List<Layer> getLayers(final String identifier) throws ConfigurationException {
        this.ensureExistingInstance(identifier);

        // Extracts the layer list from service configuration.
        final LayerContext layerContext = (LayerContext) this.getInstanceConfiguration(identifier);
        List<Layer> layers = MapUtilities.getConfigurationLayers(layerContext, null, null);

        for (Layer layer : layers) {
            final LayerRecord record = ConfigurationEngine.getLayer(identifier, this.specification, layer.getName());
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
    public Instance getInstance(String identifier) throws ConfigurationException {
        final Instance instance = super.getInstance(identifier);
        instance.setLayersNumber(getLayers(identifier).size());
        return instance;
    }
    /**
     * {@inheritDoc}
     * Add default FeatureInfoFormat for LayerContext configuration.
     */
    @Override
    public void createInstance(String identifier, Service metadata, Object configuration) throws ConfigurationException {

        if (configuration == null) {
            configuration = new LayerContext();
            ((LayerContext)configuration).setGetFeatureInfoCfgs(FeatureInfoUtilities.createGenericConfiguration());
        }

        super.createInstance(identifier, metadata, configuration);
    }

    public void removeLayer(final String serviceId, final QName layerid) throws JAXBException {
        try {
            final LayerContext layerContext = (LayerContext) ConfigurationEngine.getConfiguration(specification.name(), serviceId);
            final List<Source> sources = layerContext.getLayers();
            QName name = null;
            boolean found = false;

            for (Source source : sources) {
                List<Layer> layers = source.getInclude();
                for (Layer layer : layers) {
                    if(layer.getName().equals(layerid)){
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
                ConfigurationEngine.storeConfiguration(specification.name(), serviceId, layerContext);
                ConfigurationEngine.deleteLayer(serviceId, specification, name);
            }

        } catch (FileNotFoundException e) {
            LOGGER.log(Level.WARNING, "", e);
        }
    }
}
