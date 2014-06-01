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
package org.constellation.provider.coveragesql;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;

import org.constellation.ServiceDef;
import org.constellation.provider.AbstractData;
import org.constellation.provider.CoverageData;
import org.constellation.provider.DataProviders;
import org.constellation.provider.StyleProviders;

import org.geotoolkit.coverage.DefaultCoverageReference;
import org.geotoolkit.coverage.grid.GeneralGridGeometry;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReadParam;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.coverage.sql.Layer;
import org.geotoolkit.coverage.sql.LayerCoverageReader;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.display2d.ext.dimrange.DimRangeSymbolizer;
import org.geotoolkit.display2d.ext.legend.LegendTemplate;
import org.geotoolkit.image.io.metadata.SpatialMetadata;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.map.ElevationModel;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapLayer;
import org.apache.sis.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.DateRange;
import org.apache.sis.measure.MeasurementRange;
import org.geotoolkit.style.RandomStyleBuilder;

import org.geotoolkit.feature.type.Name;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.operation.TransformException;
import org.opengis.style.FeatureTypeStyle;
import org.opengis.style.RasterSymbolizer;
import org.opengis.style.Rule;
import org.opengis.style.ShadedRelief;
import org.opengis.style.Style;
import org.opengis.style.Symbolizer;


/**
 * Regroups information about a {@linkplain Layer layer}.
 *
 * @author Johann Sorel (Geomatys)
 */
class CoverageSQLLayerDetails extends AbstractData implements CoverageData {

    private final LayerCoverageReader reader;

    private final Name elevationModel;

    /**
     * Stores information about a {@linkplain Layer layer} in a {@code PostGRID}
     * {@linkplain Database database}.
     *
     * @param reader         The reader for this layer.
     * @param favorites      Favorites styles for this layer.
     * @param elevationModel The elevation model to apply.
     * @param name           The name of the layer.
     */
    CoverageSQLLayerDetails(final LayerCoverageReader reader, final List<String> favorites,
            final Name elevationModel, final Name name) {
        super(name,favorites);

        this.reader = reader;
        this.elevationModel = elevationModel;
    }

    /**
     * Returns the rectified grid of this layer.
     */
    @Override
    public SpatialMetadata getSpatialMetadata() throws DataStoreException {
        return reader.getCoverageMetadata(0);
    }

    /**
     * Returns the time range of this layer.
     */
    @Override
    public DateRange getDateRange() throws DataStoreException {
        return reader.getInput().getEnvelope(null, null).getTimeRange();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GridCoverage2D getCoverage(final Envelope envelope, final Dimension dimension,
            final Double elevation, final Date time) throws DataStoreException, IOException{

        final GridCoverageReadParam param = new GridCoverageReadParam();
        param.setEnvelope(envelope);
        try {
            return (GridCoverage2D) reader.read(0, param);
        } catch (CancellationException ex) {
            throw new IOException(ex.getMessage(),ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MapLayer getMapLayer(MutableStyle style, final Map<String, Object> params) {


        // DIM_RANGE extra parameter ///////////////////////////////////////////
        if (params != null) {
            final Map<String,?> extras = (Map<String, ?>) params.get(KEY_EXTRA_PARAMETERS);
            if(extras != null){
                for(String key : extras.keySet()){
                    if(key.equalsIgnoreCase("dim_range")){
                        final String strDimRange = ((List)extras.get(key)).get(0).toString();
                        final MeasurementRange dimRange = toMeasurementRange(strDimRange);
                        if(dimRange != null){
                            //a dim range is define, it replace any given style.
                            final DimRangeSymbolizer symbol = new DimRangeSymbolizer(dimRange);
                            style = StyleProviders.STYLE_FACTORY.style(symbol);
                            final DefaultCoverageReference reference = new DefaultCoverageReference(reader, getName());
                            return MapBuilder.createCoverageLayer(reference, style);
                        }
                        break;
                    }
                }
            }
        }
        ////////////////////////////////////////////////////////////////////////

        if(style == null && favorites.size() > 0){
            //no style provided, try to get the favorite one
            //there are some favorites styles
            final String namedStyle = favorites.get(0);
            style = StyleProviders.getInstance().get(namedStyle);
        }

        if(style == null){
            //no favorites defined, create a default one
            style = RandomStyleBuilder.createDefaultRasterStyle();
        }

        final String title = getName().getLocalPart();
        final DefaultCoverageReference reference = new DefaultCoverageReference(reader, getName());
        final CoverageMapLayer mapLayer = MapBuilder.createCoverageLayer(reference, style);
        mapLayer.setDescription(StyleProviders.STYLE_FACTORY.description(title,title));

        //search if we need an elevationmodel for style
        search_loop:
        for (FeatureTypeStyle fts : mapLayer.getStyle().featureTypeStyles()){
            for (Rule rule : fts.rules()){
                for (Symbolizer symbol : rule.symbolizers()){
                    if (symbol instanceof RasterSymbolizer){
                        final RasterSymbolizer rs = (RasterSymbolizer) symbol;
                        final ShadedRelief sr     = rs.getShadedRelief();
                        if (sr.getReliefFactor().evaluate(null, Float.class) != 0 && elevationModel!=null){
                            final ElevationModel model = DataProviders.getInstance().getElevationModel(elevationModel);
                            if (model != null){
                                mapLayer.setElevationModel(model);
                            }
                            break search_loop;
                        }
                    }
                }
            }
        }

        return mapLayer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isQueryable(ServiceDef.Query service) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Envelope getEnvelope() throws DataStoreException {
        final GeneralGridGeometry generalGridGeom = reader.getGridGeometry(0);
        if (generalGridGeom == null) {
            LOGGER.log(Level.INFO, "The layer \"{0}\" does not contain a grid geometry information.", name);
            return null;
        }
        try {
            return generalGridGeom.getEnvelope();
        } catch (CancellationException ex) {
            throw new DataStoreException(ex);
        } 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Date> getAvailableTimes() throws DataStoreException {
        return reader.getInput().getAvailableTimes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Number> getAvailableElevations() throws DataStoreException {
        return reader.getInput().getAvailableElevations();
    }

    /**
     * {@inheritDoc}
     
    private MutableStyle getDefaultStyle() {
        if (getSampleValueRanges().length > 0) {
            return StyleProviderProxy.getInstance().get("GO2:DimRange");
        }
        return StyleProviderProxy.STYLE_FACTORY.style(StyleProviderProxy.STYLE_FACTORY.rasterSymbolizer());
    }*/

    /**
     * {@inheritDoc}
     */
    @Override
    public MeasurementRange<?>[] getSampleValueRanges() {
        try {
            final List<MeasurementRange<?>> sampleValueRanges = reader.getInput().getSampleValueRanges();
            return sampleValueRanges.toArray(new MeasurementRange<?>[0]);
        } catch (CoverageStoreException ex) {
            LOGGER.log(Level.INFO, ex.getMessage(), ex);
            return new MeasurementRange<?>[0];
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getImageFormat() {
        try {
            return reader.getInput().getImageFormats().first();
        } catch (CoverageStoreException ex) {
            LOGGER.log(Level.INFO, ex.getMessage(), ex);
            return "unknown";
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BufferedImage getLegendGraphic(Dimension dimension, final LegendTemplate template,
                                          final Style style, final String rule, final Double scale)
                                          throws PortrayalException
    {
        final Map<String,?> properties = Collections.singletonMap("size", dimension);
        RenderedImage legend = null;
        try {
            legend = reader.getInput().getColorRamp(0, null, properties);
        } catch (CoverageStoreException ex) {
            LOGGER.log(Level.INFO, ex.getMessage(), ex);
        }
        if (legend != null) {
            // Always an instance of BufferedImage in Geotk implementation.
            // We don't check because we want a ClassCastException if this
            // assumption does not hold anymore, so we know we have to fix.
            return (BufferedImage) legend;
        }
        return super.getLegendGraphic(dimension, template, style, rule, scale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRemarks() {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getThematic() {
        return "";
    }

    protected GridCoverageReader getReader(){
        return reader;
    }

    /**
     * Specifies that the type of this layer is coverage.
     */
    @Override
    public TYPE getType() {
        return TYPE.COVERAGE;
    }

    public static MeasurementRange toMeasurementRange(final String strDimRange) {
        if (strDimRange == null) {
            return null;
        }
        final String[] split = strDimRange.split(",");
        final double min = Double.valueOf(split[0]);
        final double max = Double.valueOf(split[1]);
        return MeasurementRange.create(min, true, max, true, null);
    }

}
