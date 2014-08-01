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

package org.constellation.ws.rs;

import com.vividsolutions.jts.geom.Geometry;
import org.apache.sis.geometry.GeneralEnvelope;
import org.apache.sis.storage.DataStoreException;
import org.constellation.admin.StyleBusiness;
import org.constellation.configuration.TargetNotFoundException;
import org.constellation.dto.BandDescription;
import org.constellation.dto.CoverageDataDescription;
import org.constellation.dto.DataDescription;
import org.constellation.dto.FeatureDataDescription;
import org.constellation.dto.PortrayalContext;
import org.constellation.dto.PropertyDescription;
import org.constellation.portrayal.internal.PortrayalResponse;
import org.constellation.provider.Data;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.constellation.provider.FeatureData;
import org.constellation.provider.ObservationData;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.GridSampleDimension;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.data.session.Session;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.display.canvas.control.NeverFailMonitor;
import org.geotoolkit.display2d.service.CanvasDef;
import org.geotoolkit.display2d.service.OutputDef;
import org.geotoolkit.display2d.service.SceneDef;
import org.geotoolkit.display2d.service.ViewDef;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.feature.type.DefaultName;
import org.geotoolkit.feature.type.FeatureType;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.feature.type.PropertyDescriptor;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.process.coverage.copy.StatisticOp;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.ReferencingUtilities;
import org.geotoolkit.sld.xml.Specification;
import org.geotoolkit.sld.xml.StyleXmlIO;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.MutableStyleFactory;
import org.geotoolkit.style.function.InterpolationPoint;
import org.geotoolkit.style.function.Method;
import org.geotoolkit.style.function.Mode;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.style.ChannelSelection;
import org.opengis.style.ColorMap;
import org.opengis.style.ContrastEnhancement;
import org.opengis.style.ContrastMethod;
import org.opengis.style.Description;
import org.opengis.style.OverlapBehavior;
import org.opengis.style.RasterSymbolizer;
import org.opengis.style.ShadedRelief;
import org.opengis.style.Symbolizer;
import org.opengis.util.FactoryException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import static org.apache.sis.util.ArgumentChecks.ensurePositive;
import static org.geotoolkit.style.StyleConstants.DEFAULT_CATEGORIZE_LOOKUP;
import static org.geotoolkit.style.StyleConstants.DEFAULT_DESCRIPTION;
import static org.geotoolkit.style.StyleConstants.DEFAULT_FALLBACK;
import static org.geotoolkit.style.StyleConstants.DEFAULT_GEOM;
import static org.geotoolkit.style.StyleConstants.LITERAL_ONE_FLOAT;

/**
 * Utility class for layer provider management/configuration.
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
@Component
public final class LayerProviders {
    
    @Inject
    StyleBusiness styleBusiness;

    /**
     * Default rendering options.
     */
    private static final NeverFailMonitor DEFAULT_MONITOR = new NeverFailMonitor();
    private static final Hints DEFAULT_HINTS = new Hints(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON,
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);

    public static final MutableStyleFactory SF = (MutableStyleFactory) FactoryFinder.getStyleFactory(
            new Hints(Hints.STYLE_FACTORY, MutableStyleFactory.class));


    /**
     * Gives a {@link DataDescription} instance describing the layer data source.
     * <p/>
     * {@link DataDescription} has two distinct implementations:
     * <ui>
     * <li>{@link FeatureDataDescription} for feature layers.</li>
     * <li>{@link CoverageDataDescription} for coverage layers.</li>
     * </ui>
     *
     * @param providerId the layer provider id
     * @param layerName  the layer name
     * @return the {@link DataDescription} instance
     * @throws CstlServiceException if failed to extracts the data description for any reason
     */
    public static DataDescription getDataDescription(final String providerId, final String layerName) throws CstlServiceException {
        ensureNonNull("providerId", providerId);
        ensureNonNull("layerName", layerName);

        // Get the layer.
        final Data layer = getLayer(providerId, layerName);

        // Try to extract layer data info.
        try {
            if (layer instanceof FeatureData) {
                return getFeatureDataDescription((FeatureData) layer);
            } else {
                return getCoverageDataDescription(layer);
            }
        } catch (DataStoreException ex) {
            throw new CstlServiceException("An error occurred while trying get/open datastore for provider with id \"" + providerId + "\".");
        } catch (IOException ex) {
            throw new CstlServiceException("An error occurred while trying get data for provider with id \"" + providerId + "\".");
        }
    }

    /**
     * Returns all the values of a layer feature collection filtered on the specified
     * property.
     *
     * @param providerId the layer provider id
     * @param layerName  the layer name
     * @param property   the property name
     * @return the values
     * @throws CstlServiceException if failed to extracts the attribute values for any reason
     */
    public static Object[] getPropertyValues(final String providerId, final String layerName, final String property) throws CstlServiceException {
        ensureNonNull("providerId", providerId);
        ensureNonNull("layerName", layerName);
        ensureNonNull("property", property);

        // Get the layer.
        final Data layer = getLayer(providerId, layerName);

        // Try to extract attribute values.
        if (layer instanceof FeatureData) {

            // Open session.
            final Session session = ((FeatureData) layer).getStore().createSession(false);

            // Get feature collection.
            final QueryBuilder qb = new QueryBuilder();
            qb.setProperties(new String[]{property});
            qb.setTypeName(layer.getName());
            final FeatureCollection<Feature> collection = session.getFeatureCollection(qb.buildQuery());

            // Visit collection.
            final Object[] values = new Object[collection.size()];
            int i = 0;
            for (final Feature feature : collection) {
                values[i] = feature.getProperty(property).getValue();
                i++;
            }

            return values;
        }

        // Not a feature layer.
        throw new CstlServiceException("The layer named \"" + layerName + "\" for provider with id \"" + providerId + "\" is not a feature layer.");
    }

    /**
     * Returns all the values of a coverage layer band filtered on the specified band
     * index.
     *
     * @param providerId the layer provider id
     * @param layerName  the layer name
     * @param bandIndex  the band index
     * @return the values
     * @throws CstlServiceException if failed to extracts the band values for any reason
     */
    public static double[] getBandValues(final String providerId, final String layerName, final int bandIndex) throws CstlServiceException {
        ensureNonNull("providerId", providerId);
        ensureNonNull("layerName", layerName);
        ensurePositive("bandIndex", bandIndex);

        // Get the layer.
        final Data layer = getLayer(providerId, layerName);

        // Try to extract band values.
        if (!(layer instanceof FeatureData)) {
            try {
                final GridSampleDimension[] dims = layer.getCoverage(null, null, null, null).getSampleDimensions();
                // TODO
            } catch (DataStoreException ex) {
                throw new CstlServiceException("An error occurred while trying get/open datastore for provider with id \"" + providerId + "\".");
            } catch (IOException ex) {
                throw new CstlServiceException("An error occurred while trying get data info for provider with id \"" + providerId + "\".");
            }
        }

        // Not a coverage layer.
        throw new CstlServiceException("The layer named \"" + layerName + "\" for provider with id \"" + providerId + "\" is not a coverage layer.");
    }

    /**
     * Produces a {@link PortrayalResponse} from the specified {@link PortrayalContext}.
     * <p/>
     * This method allows to perform data rendering without WMS layer.
     *
     * @param context the portrayal context
     * @return a {@link PortrayalResponse} instance
     * @throws CstlServiceException if the {@link PortrayalResponse} can't be produced for
     * any reason
     */
    public static PortrayalResponse portray(final PortrayalContext context) throws CstlServiceException {
        return portray(context.getDataName(),
                context.getProviderId(),
                context.getProjection(),
                context.getWest() + "," + context.getSouth() + "," + context.getEast() + "," + context.getNorth(),
                context.getWidth(),
                context.getHeight(),
                context.getStyleBody(),
                context.getSldVersion(),
                null);
    }

    /**
     * Produces a {@link PortrayalResponse} from the specified parameters.
     * <p/>
     * This method allows to perform data rendering without WMS layer.
     *
     * @param providerId  the layer provider id
     * @param layerName   the layer name
     * @param crsCode     the projection code
     * @param bbox        the bounding box
     * @param width       the image width
     * @param height      the image height
     * @param sldVersion  the SLD version
     * @param sldProvider the SLD provider name
     * @param styleId     the style identifier in the provider
     * @param filter      the filter on data
     *
     * @return a {@link PortrayalResponse} instance
     * @throws CstlServiceException if the {@link PortrayalResponse} can't be produced for
     * any reason
     * @throws TargetNotFoundException CstlServiceException
     * @throws JAXBException
     */
    public PortrayalResponse portray(final String providerId, final String layerName, final String crsCode,
                                            final String bbox, final int width, final int height, final String sldVersion,
                                            final String sldProvider, final String styleId, final String filter)
                                            throws CstlServiceException, TargetNotFoundException, JAXBException {
        if (sldProvider == null || styleId == null) {
            return portray(providerId, layerName, crsCode, bbox, width, height, null, sldVersion, filter);
        }
    	MutableStyle style = styleBusiness.getStyle(sldProvider, styleId);
        if (style == null){
            throw new CstlServiceException("styleid : "+styleId+" on provider : "+sldProvider+" not found");
        }
    	StyleXmlIO styleXmlIO = new StyleXmlIO();
    	final StringWriter sw = new StringWriter();
    	styleXmlIO.writeStyle(sw, style, Specification.StyledLayerDescriptor.V_1_1_0);
    	return portray(providerId, layerName, crsCode, bbox, width, height, sw.toString(), sldVersion, filter);
    }

    /**
     * Produces a {@link PortrayalResponse} from the specified parameters.
     * <p/>
     * This method allows to perform data rendering without WMS layer.
     *
     * @param providerId the layer provider id
     * @param layerName  the layer name
     * @param crsCode    the projection code
     * @param bbox       the bounding box
     * @param width      the image width
     * @param height     the image height
     * @param sldBody    the style to apply
     * @param sldVersion the style version
     * @return a {@link PortrayalResponse} instance
     * @throws CstlServiceException if the {@link PortrayalResponse} can't be produced for
     * any reason
     */
    public static PortrayalResponse portray(final String providerId, final String layerName, final String crsCode,
                                            final String bbox, final int width, final int height, final String sldBody,
                                            final String sldVersion, final String filter) throws CstlServiceException {
        ensureNonNull("providerId", providerId);
        ensureNonNull("layerName", layerName);

        // Get the layer (throws exception if doesn't exist).
        final Data layer = getLayer(providerId, layerName);

        try {
            // Envelope.
            final String[] bboxSplit = bbox.split(",");
            final GeneralEnvelope envelope = new GeneralEnvelope(CRS.decode(crsCode));
            envelope.setRange(0, Double.valueOf(bboxSplit[0]), Double.valueOf(bboxSplit[2]));
            envelope.setRange(1, Double.valueOf(bboxSplit[1]), Double.valueOf(bboxSplit[3]));

            // Dimension.
            final Dimension dimension = new Dimension(width, height);

            // Style.
            final MutableStyle style;
            if (sldBody != null) {
                // Use specified style.
                final StringReader reader = new StringReader(sldBody);
                if ("1.1.0".equals(sldVersion)) {
                    style = new StyleXmlIO().readStyle(reader, Specification.SymbologyEncoding.V_1_1_0);
                } else {
                    style = new StyleXmlIO().readStyle(reader, Specification.SymbologyEncoding.SLD_1_0_0);
                }
            } else {
                // Fallback to a default/auto-generated style.
                if (layer instanceof FeatureData || layer instanceof ObservationData) {
                    style = null; // Let portrayal process apply is own style.
                } else {
                    style = generateCoverageStyle(layer);
                }
            }

            // Map context.
            final MapContext mapContext = MapBuilder.createContext();
            final MapItem mapItem;
            if (filter != null && !filter.isEmpty()) {
                final Map<String,Object> params = new HashMap<>();
                params.put("CQL_FILTER", filter);
                final Map<String,Object> extraParams = new HashMap<>();
                extraParams.put(Data.KEY_EXTRA_PARAMETERS, params);
                mapItem = layer.getMapLayer(style, extraParams);
            } else {
                mapItem = layer.getMapLayer(style, null);
            }
            mapContext.items().add(mapItem);

            // Inputs.
            final SceneDef sceneDef = new SceneDef(mapContext, DEFAULT_HINTS);
            final CanvasDef canvasDef = new CanvasDef(dimension, null);
            final ViewDef viewDef = new ViewDef(envelope, 0, DEFAULT_MONITOR);
            final OutputDef outputDef = new OutputDef("image/png", new Object());

            // Create response.
            return new PortrayalResponse(canvasDef, sceneDef, viewDef, outputDef);

        } catch (FactoryException | JAXBException | PortrayalException | DataStoreException | IOException ex) {
            throw new CstlServiceException(ex.getLocalizedMessage());
        }
    }


    /**************************************************************************
     *                            Private methods                             *
     **************************************************************************/

    /**
     * Gets a {@link DataProvider} instance from its ID.
     *
     * @param providerId the layer provider id
     * @return the {@link DataProvider} instance
     * @throws CstlServiceException if the provider does not exists
     */
    private static DataProvider getProvider(final String providerId) throws CstlServiceException {
        final DataProvider provider = DataProviders.getInstance().getProvider(providerId);
        if (provider == null) {
            throw new CstlServiceException("No layer provider for id \"" + providerId + "\".");
        }
        return provider;
    }


    /**
     * Gets a {@link Data} instance from its layer provider and its name.
     *
     * @param provider  the layer provider
     * @param layerName the layer name
     * @return the {@link Data} instance
     * @throws CstlServiceException if the layer does not exists
     */
    private static Data getLayer(final DataProvider provider, final String layerName) throws CstlServiceException {
        final Name name = DefaultName.valueOf(layerName);
        final Data layer = provider.get(name);
        if (layer == null) {
            throw new CstlServiceException("No layer named \"" + layerName + "\" in provider with id \"" + provider.getId() + "\".");
        }
        return layer;
    }

    /**
     * Gets a {@link Data} instance from its layer provider ID and its name.
     *
     * @param providerId the layer provider id
     * @param layerName  the layer name
     * @return the {@link Data} instance
     * @throws CstlServiceException if the provider or the layer does not exists
     */
    public static Data getLayer(final String providerId, final String layerName) throws CstlServiceException {
        return getLayer(getProvider(providerId), layerName);
    }

    /**
     * Gives a {@link CoverageDataDescription} instance describing the coverage layer
     * data source.
     *
     * @param layer the layer to visit
     * @return the {@link CoverageDataDescription} instance
     * @throws IOException        if an error occurred while trying to read the coverage
     * @throws DataStoreException if an error occurred during coverage store operations
     */
    private static CoverageDataDescription getCoverageDataDescription(final Data layer) throws IOException, DataStoreException {
        final CoverageDataDescription description = new CoverageDataDescription();

        final CoverageReference ref = (CoverageReference)layer.getOrigin();
        final GridCoverageReader reader = ref.acquireReader();
        final List<GridSampleDimension> dims = reader.getSampleDimensions(ref.getImageIndex());

        // Bands description.
        if (dims != null) {
            for (final GridSampleDimension dim : dims) {
                final String dimName = dim.getDescription().toString();
                description.getBands().add(new BandDescription(dimName, dim.getMinimumValue(), dim.getMaximumValue(), dim.getNoDataValues()));
            }
        } else {
            Map<String, Object> map = StatisticOp.analyze(reader, ref.getImageIndex());
            double[] min = (double[]) map.get("min");
            double[] max = (double[]) map.get("max");
            if (min != null && min.length > 0) {
                for (int i=0; i<min.length; i++) {
                    description.getBands().add(new BandDescription(String.valueOf(i), min[i], max[i], null));
                }
            }
        }

        // Geographic extent description.
        final Envelope envelope = reader.getGridGeometry(ref.getImageIndex()).getEnvelope();
        fillGeographicDescription(envelope, description);

        ref.recycle(reader);
        return description;
    }

    /**
     * Gives a {@link FeatureDataDescription} instance describing the feature layer
     * data source.
     *
     * @param layer the layer to visit
     * @return the {@link FeatureDataDescription} instance
     * @throws DataStoreException if an error occurred during feature store operations
     */
    private static FeatureDataDescription getFeatureDataDescription(final FeatureData layer) throws DataStoreException {
        final FeatureDataDescription description = new FeatureDataDescription();

        // Acquire data feature type.
        final FeatureStore store = layer.getStore();
        final FeatureType featureType = store.getFeatureType(layer.getName());

        // Feature attributes description.
        final PropertyDescriptor geometryDesc = featureType.getGeometryDescriptor();
        description.setGeometryProperty(new PropertyDescription(
                geometryDesc.getName().getNamespaceURI(),
                geometryDesc.getName().getLocalPart(),
                geometryDesc.getType().getBinding() != null ? geometryDesc.getType().getBinding() : Geometry.class));
        for (final PropertyDescriptor desc : featureType.getDescriptors()) {
            description.getProperties().add(new PropertyDescription(
                    desc.getName().getNamespaceURI(),
                    desc.getName().getLocalPart(),
                    desc.getType().getBinding()));
        }

        // Geographic extent description.
        final QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.setTypeName(layer.getName());
        final Envelope envelope = store.getEnvelope(queryBuilder.buildQuery());
        fillGeographicDescription(envelope, description);

        return description;
    }

    /**
     * Analyzes a "coverage" layer first band values (statistics) to generate a
     * {@link MutableStyle} instance.
     *
     * @param layer the layer to analyze
     * @return a {@link MutableStyle} instance
     * @throws IOException if an error occurred while acquiring coverage statistics
     * @throws DataStoreException if an error occurred while acquiring coverage statistics
     */
    private static MutableStyle generateCoverageStyle(final Data layer) throws DataStoreException, IOException {
        // Acquire coverage data.
        final CoverageReference ref = (CoverageReference) layer.getOrigin();
        if (ref == null) {
            return null;
        }
        final GridCoverageReader reader = ref.acquireReader();
        final List<GridSampleDimension> dims = reader.getSampleDimensions(ref.getImageIndex());
        ref.recycle(reader);

        // Determine if we should apply this palette (should be applied only for geophysics data!)
        // HACK: normally we should test if the view types set contains photographic, but this is not working here
        // because all coverage readers seems to have it ... so just test the number of sample dimensions.
        // It won't work for all cases ...
        // TODO: fix netcdf reader, should not add photographic in the view types possibilities
        final int nbSamples = (dims==null) ? 0 : dims.size();
        if (nbSamples==0 || nbSamples==3 || nbSamples==4) {
            // should be RGB, no need to apply a palette, let the renderer display this image unchanged
            return null;
        }

        // Extract first band statistics.
        double min = dims.get(0).getMinimumValue();
        double max = dims.get(0).getMaximumValue();
        double average = (max + min) / 2;

        // Generate a color map from band statistics.
        final List<InterpolationPoint> values = new ArrayList<>();
        values.add(SF.interpolationPoint(Float.NaN, SF.literal(new Color(0, 0, 0, 0))));
        values.add(SF.interpolationPoint(min, SF.literal(new Color(0, 54, 204, 255))));
        values.add(SF.interpolationPoint(average, SF.literal(new Color(255, 254, 162, 255))));
        values.add(SF.interpolationPoint(max, SF.literal(new Color(199, 8, 30, 255))));
        final Function function = SF.interpolateFunction(DEFAULT_CATEGORIZE_LOOKUP, values, Method.COLOR, Mode.LINEAR, DEFAULT_FALLBACK);
        final ColorMap colorMap = SF.colorMap(function);

        // Select the first band.
        final ChannelSelection selection = SF.channelSelection(SF.selectedChannelType("0", (ContrastEnhancement) null));

        // Create final style.
        final Expression opacity = LITERAL_ONE_FLOAT;
        final OverlapBehavior overlap = OverlapBehavior.LATEST_ON_TOP;
        final ContrastEnhancement enhance = SF.contrastEnhancement(LITERAL_ONE_FLOAT, ContrastMethod.NONE);
        final ShadedRelief relief = SF.shadedRelief(LITERAL_ONE_FLOAT);
        final Unit uom = NonSI.PIXEL;
        final String geom = DEFAULT_GEOM;
        final String name = "raster symbol name";
        final Description desc = DEFAULT_DESCRIPTION;
        final Symbolizer outline = null;
        final RasterSymbolizer symbol = SF.rasterSymbolizer(name, geom, desc, uom, opacity, selection, overlap, colorMap, enhance, relief, outline);
        final MutableStyle style = SF.style(symbol);
        style.setDefaultSpecification(symbol);
        return style;
    }

    /**
     * Fills the geographical field of a {@link DataDescription} instance according the
     * specified {@link Envelope}.
     *
     * @param envelope    the envelope to visit
     * @param description the data description to update
     */
    private static void fillGeographicDescription(Envelope envelope, final DataDescription description) {
        double[] lower, upper;
        try {
            envelope = CRS.transform(envelope, CRS.decode("CRS:84"));
            lower = envelope.getLowerCorner().getCoordinate();
            upper = envelope.getUpperCorner().getCoordinate();
        } catch (Exception ignore) {
            lower = new double[]{-180, -90};
            upper = new double[]{180, 90};
        }
        description.setBoundingBox(new double[]{lower[0], lower[1], upper[0], upper[1]});
    }

    /**
     *
     * @param providerId
     * @param layerName
     * @return
     * @throws CstlServiceException
     * @throws IOException
     * @throws DataStoreException
     */
    public static List<String> getCrs(final String providerId, final String layerName) throws CstlServiceException, IOException, DataStoreException {
        final List<String> crsListString = new ArrayList<>(0);
        final Data layer = getLayer(getProvider(providerId), layerName);

        // Acquire coverage data.
        GridCoverage2D coverage = layer.getCoverage(null, null, null, null);
        CoordinateReferenceSystem coverageCRS = coverage.getCoordinateReferenceSystem();

        // decompose crs to found all components
        final List<CoordinateReferenceSystem> crsList = ReferencingUtilities.decompose(coverageCRS);

        //create list
        for (CoordinateReferenceSystem referenceSystem : crsList) {
            crsListString.add(referenceSystem.getName().toString());
        }

        return crsListString;
    }
}
