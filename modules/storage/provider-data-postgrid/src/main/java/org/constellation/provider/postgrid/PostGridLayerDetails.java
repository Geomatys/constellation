/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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
package org.constellation.provider.postgrid;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.constellation.coverage.catalog.CoverageReference;
import org.constellation.coverage.catalog.GridCoverageTable;
import org.constellation.coverage.catalog.Layer;
import org.constellation.coverage.catalog.Series;
import org.constellation.ws.ServiceType;
import org.constellation.map.PostGridMapLayer2;
import org.constellation.map.PostGridReader;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.StyleProviderProxy;

import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.display.shape.DoubleDimension2D;
import org.geotoolkit.coverage.io.CoverageReader;
import org.geotoolkit.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotoolkit.map.ElevationModel;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.StyleConstants;
import org.geotoolkit.style.function.InterpolationPoint;
import org.geotoolkit.style.function.Method;
import org.geotoolkit.style.function.Mode;
import org.geotoolkit.util.MeasurementRange;

import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.operation.TransformException;
import org.opengis.style.ChannelSelection;
import org.opengis.style.ColorMap;
import org.opengis.style.ContrastEnhancement;
import org.opengis.style.Description;
import org.opengis.style.FeatureTypeStyle;
import org.opengis.style.OverlapBehavior;
import org.opengis.style.RasterSymbolizer;
import org.opengis.style.Rule;
import org.opengis.style.ShadedRelief;
import org.opengis.style.Symbolizer;


/**
 * Regroups information about a {@linkplain Layer layer}.
 *
 * @version $Id$
 * @author Cédric Briançon
 */
class PostGridLayerDetails implements LayerDetails {
    private final PostGridReader reader;

    /**
     * Favorites styles associated with this layer.
     */
    private final List<String> favorites;

    private final String elevationModel;

    /**
     * Stores information about a {@linkplain Layer layer} in a {@code PostGRID}
     * {@linkplain Database database}.
     *
     * @param database The database connection.
     * @param layer The layer to consider in the database.
     */
    PostGridLayerDetails(final PostGridReader reader, final List<String> favorites, final String elevationModel) {
        this.reader = reader;

        if (favorites == null) {
            this.favorites = Collections.emptyList();
        } else {
            this.favorites = favorites;
        }
        this.elevationModel = elevationModel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GridCoverage2D getCoverage(final Envelope envelope, final Dimension dimension,
            final Double elevation, final Date time) throws CatalogException, IOException
    {
        final ReferencedEnvelope objEnv = new ReferencedEnvelope(envelope);
        final int width = dimension.width;
        final int height = dimension.height;
        final Envelope genv;
        try {
            genv = CRS.transform(objEnv, DefaultGeographicCRS.WGS84);
        } catch (TransformException ex) {
            throw new CatalogException(ex);
        }
        final GeneralEnvelope renv = new GeneralEnvelope(genv);

        //Create BBOX-----------------------------------------------------------
        final GeographicBoundingBox bbox;
        try {
            bbox = new DefaultGeographicBoundingBox(renv);
        } catch (TransformException ex) {
            throw new CatalogException(ex);
        }

        //Create resolution-----------------------------------------------------
        final double w = renv.toRectangle2D().getWidth()  / width;
        final double h = renv.toRectangle2D().getHeight() / height;
        final Dimension2D resolution = new DoubleDimension2D(w, h);

        GridCoverageTable table = reader.getTable();
        table = new GridCoverageTable(table);

        table.setGeographicBoundingBox(bbox);
        table.setPreferredResolution(resolution);
        table.setTimeRange(time, time);
        if (elevation != null) {
            table.setVerticalRange(elevation, elevation);
        } else {
            table.setVerticalRange(null);
        }
        final CoverageReference coverageRef;
        try {
            coverageRef = table.getEntry();
        } catch (SQLException ex) {
            throw new CatalogException(ex);
        }
        if (coverageRef == null) {
            throw new CatalogException("The request done is not in the domain of validity of the coverage. " +
                    "Either the envelope or the date (or both) is/are not defined for this coverage.");
        }
        return coverageRef.getCoverage(null);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public MapLayer getMapLayer(MutableStyle style, final Map<String, Object> params) {
        final PostGridMapLayer2 mapLayer = new PostGridMapLayer2(reader);

        mapLayer.setName(getName());

        if(style == null && favorites.size() > 0){
            //no style provided, try to get the favorite one
            //there are some favorites styles
            String namedStyle = favorites.get(0);
            style = StyleProviderProxy.getInstance().get(namedStyle);
        }

        if(style == null){
            //no favorites defined, create a default one
            style = RANDOM_FACTORY.createRasterStyle();
        }

        mapLayer.setStyle(style);

        if (params != null) {
            mapLayer.setDimRange((MeasurementRange) params.get(KEY_DIM_RANGE));
            final Double elevation = (Double) params.get(KEY_ELEVATION);
            if (elevation != null) {
                mapLayer.setElevation(elevation);
            }
            final Date time = (Date) params.get(KEY_TIME);
            if (time != null) {
                mapLayer.times().add(time);
            }
        }

        //search if we need an elevationmodel for style
        search_loop:
        for(FeatureTypeStyle fts : mapLayer.getStyle().featureTypeStyles()){
            for(Rule rule : fts.rules()){
                for(Symbolizer symbol : rule.symbolizers()){
                    if(symbol instanceof RasterSymbolizer){
                        RasterSymbolizer rs = (RasterSymbolizer) symbol;
                        ShadedRelief sr = rs.getShadedRelief();
                        if(sr.getReliefFactor().evaluate(null, Float.class) != 0){
                            ElevationModel model = LayerProviderProxy.getInstance().getElevationModel(elevationModel);
                            if(model != null){
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
    public String getName() {
        return reader.getTable().getLayer().getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getFavoriteStyles(){
        return favorites;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isQueryable(ServiceType service) {
        return reader.getTable().getLayer().isQueryable(service);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GeographicBoundingBox getGeographicBoundingBox() throws CatalogException {
        return reader.getTable().getLayer().getGeographicBoundingBox();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Date> getAvailableTimes() throws CatalogException {
        return reader.getTable().getLayer().getAvailableTimes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Number> getAvailableElevations() throws CatalogException {
        return reader.getTable().getLayer().getAvailableElevations();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BufferedImage getLegendGraphic(final Dimension dimension) {
        return reader.getTable().getLayer().getLegend((dimension != null) ? dimension : LEGEND_SIZE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MeasurementRange<?>[] getSampleValueRanges() {
        return reader.getTable().getLayer().getSampleValueRanges();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Series> getSeries() {
        return reader.getTable().getLayer().getSeries();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRemarks() {
        return reader.getTable().getLayer().getRemarks();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getThematic() {
        return reader.getTable().getLayer().getThematic();
    }

    protected CoverageReader getReader(){
        return reader;
    }

    private MutableStyle toStyle(final MeasurementRange dimRange) {
        final List<InterpolationPoint> values = new ArrayList<InterpolationPoint>();
        values.add(STYLE_FACTORY.interpolationPoint(
                        STYLE_FACTORY.literal(Color.WHITE), dimRange.getMinimum()));
        values.add(STYLE_FACTORY.interpolationPoint(
                        STYLE_FACTORY.literal(Color.BLUE), dimRange.getMaximum()));
        final Literal lookup = StyleConstants.DEFAULT_CATEGORIZE_LOOKUP;
        final Literal fallback = StyleConstants.DEFAULT_FALLBACK;
        final Function interpolateFunction = STYLE_FACTORY.interpolateFunction(
                lookup, values, Method.COLOR, Mode.LINEAR, fallback);

        final ChannelSelection selection = STYLE_FACTORY.channelSelection(
                STYLE_FACTORY.selectedChannelType("0", FILTER_FACTORY.literal(1)));

        final Expression opacity = FILTER_FACTORY.literal(1f);
        final OverlapBehavior overlap = OverlapBehavior.LATEST_ON_TOP;
        final ColorMap colorMap = STYLE_FACTORY.colorMap(interpolateFunction);
        final ContrastEnhancement enhanced = StyleConstants.DEFAULT_CONTRAST_ENHANCEMENT;
        final ShadedRelief relief = StyleConstants.DEFAULT_SHADED_RELIEF;
        final Symbolizer outline = null; //createRealWorldLineSymbolizer();
        final Unit uom = NonSI.FOOT;
        final String geom = StyleConstants.DEFAULT_GEOM;
        final String name = "raster symbol name";
        final Description desc = StyleConstants.DEFAULT_DESCRIPTION;

//        final RasterSymbolizer symbol = STYLE_FACTORY.rasterSymbolizer(
//                name,geom,desc,uom,opacity, selection,
//                overlap, colorMap, enhanced, relief, outline);

        final RasterSymbolizer symbol = STYLE_FACTORY.rasterSymbolizer();

        return STYLE_FACTORY.style(symbol);
    }
}
