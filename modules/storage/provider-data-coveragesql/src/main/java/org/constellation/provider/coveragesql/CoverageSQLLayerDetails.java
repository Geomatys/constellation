/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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
import org.constellation.provider.AbstractLayerDetails;
import org.constellation.provider.CoverageLayerDetails;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.StyleProviderProxy;

import org.geotoolkit.coverage.grid.GeneralGridGeometry;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReadParam;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.coverage.sql.Layer;
import org.geotoolkit.coverage.sql.LayerCoverageReader;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.display.exception.PortrayalException;
import org.geotoolkit.display2d.ext.dimrange.DimRangeSymbolizer;
import org.geotoolkit.display2d.ext.legend.LegendTemplate;
import org.geotoolkit.image.io.metadata.SpatialMetadata;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.map.ElevationModel;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.DateRange;
import org.geotoolkit.util.MeasurementRange;

import org.opengis.feature.type.Name;
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
class CoverageSQLLayerDetails extends AbstractLayerDetails implements CoverageLayerDetails {

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
                            style = STYLE_FACTORY.style(symbol);
                            return MapBuilder.createCoverageLayer(reader, style, getName().getLocalPart());
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
            style = StyleProviderProxy.getInstance().get(namedStyle);
        }

        if(style == null){
            //no favorites defined, create a default one
            style = RANDOM_FACTORY.createRasterStyle();
        }

        final String title = getName().getLocalPart();
        final CoverageMapLayer mapLayer = MapBuilder.createCoverageLayer(reader, style, title);
        mapLayer.setDescription(STYLE_FACTORY.description(title,title));

        //search if we need an elevationmodel for style
        search_loop:
        for (FeatureTypeStyle fts : mapLayer.getStyle().featureTypeStyles()){
            for (Rule rule : fts.rules()){
                for (Symbolizer symbol : rule.symbolizers()){
                    if (symbol instanceof RasterSymbolizer){
                        final RasterSymbolizer rs = (RasterSymbolizer) symbol;
                        final ShadedRelief sr     = rs.getShadedRelief();
                        if (sr.getReliefFactor().evaluate(null, Float.class) != 0){
                            final ElevationModel model = LayerProviderProxy.getInstance().getElevationModel(elevationModel);
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
     * Returns the geographic bounding box for the coverage, or {@code null} if the
     * coverage does not contain any geographic information.
     *
     * @throws DataStoreException
     */
    @Override
    public GeographicBoundingBox getGeographicBoundingBox() throws DataStoreException {
        final GeneralGridGeometry generalGridGeom = reader.getGridGeometry(0);
        if (generalGridGeom == null) {
            LOGGER.log(Level.INFO, "The layer \"{0}\" does not contain a grid geometry information.", name);
            return null;
        }
        try {
            final Envelope env = generalGridGeom.getEnvelope();
            return new DefaultGeographicBoundingBox(env);
        } catch (CancellationException ex) {
            throw new DataStoreException(ex);
        } catch (TransformException ex) {
            throw new DataStoreException(ex);
        }

    }

    /**
     * Returns the netive envelope of this layer.
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
     */
    @Override
    protected MutableStyle getDefaultStyle() {
        if (getSampleValueRanges().length > 0) {
            return StyleProviderProxy.getInstance().get("GO2:DimRange");
        }
        return StyleProviderProxy.STYLE_FACTORY.style(StyleProviderProxy.STYLE_FACTORY.rasterSymbolizer());
    }

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
        return MeasurementRange.create(min, max, null);
    }

}
