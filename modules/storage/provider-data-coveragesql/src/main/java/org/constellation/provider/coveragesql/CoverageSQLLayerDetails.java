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
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.constellation.ServiceDef;
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
import org.geotoolkit.display2d.ext.legend.DefaultLegendService;
import org.geotoolkit.display2d.ext.legend.LegendTemplate;
import org.geotoolkit.display2d.service.DefaultGlyphService;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.map.ElevationModel;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.MeasurementRange;
import org.geotoolkit.util.logging.Logging;

import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.operation.TransformException;
import org.opengis.style.FeatureTypeStyle;
import org.opengis.style.RasterSymbolizer;
import org.opengis.style.Rule;
import org.opengis.style.ShadedRelief;
import org.opengis.style.Symbolizer;


/**
 * Regroups information about a {@linkplain Layer layer}.
 *
 * @author Johann Sorel (Geomatys)
 */
class CoverageSQLLayerDetails implements CoverageLayerDetails {
    /**
     * Default logger.
     */
    private static final Logger LOGGER = Logging.getLogger(CoverageSQLLayerDetails.class);

    private final LayerCoverageReader reader;

    /**
     * Favorites styles associated with this layer.
     */
    private final List<String> favorites;
    private final String elevationModel;
    private final String name;

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
            final String elevationModel, final String name) {

        this.reader = reader;
        this.name = name;

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

        final CoverageMapLayer mapLayer = MapBuilder.createCoverageLayer(reader, style, getName());

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
    public String getName() {
        return name;
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
        final Envelope env;
        final GeneralGridGeometry generalGridGeom = reader.getGridGeometry(0);
        if (generalGridGeom == null) {
            LOGGER.info("The layer \""+ name +"\" does not contain" +
                    " a grid geometry information.");
            return null;
        }
        try {
            env = generalGridGeom.getEnvelope();
            return new DefaultGeographicBoundingBox(env);
        } catch (CancellationException ex) {
            throw new DataStoreException(ex);
        } catch (TransformException ex) {
            throw new DataStoreException(ex);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Date> getAvailableTimes() throws DataStoreException {
        return new TreeSet<Date>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Number> getAvailableElevations() throws DataStoreException {
        return new TreeSet<Number>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BufferedImage getLegendGraphic(final Dimension dimension, final LegendTemplate template) {
        final MutableStyle style = StyleProviderProxy.getInstance().get(getFavoriteStyles().get(0));
        try {
            final MapLayer layer = getMapLayer(style, null);
            final MapContext context = MapBuilder.createContext(DefaultGeographicCRS.WGS84);
            context.layers().add(layer);
            return DefaultLegendService.portray(template, context, dimension);

        } catch (PortrayalException ex) {
            Logger.getLogger(CoverageSQLLayerDetails.class.getName()).log(Level.SEVERE, null, ex);
        }

        return DefaultGlyphService.create(style, dimension,null);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Dimension getPreferredLegendSize(final LegendTemplate template, final MutableStyle ms) throws PortrayalException {
        final MapLayer ml = getMapLayer(ms, null);
        final MapContext mc = MapBuilder.createContext(DefaultGeographicCRS.WGS84);
        mc.layers().add(ml);
        return DefaultLegendService.legendPreferredSize(template, mc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MeasurementRange<?>[] getSampleValueRanges() {
        return new MeasurementRange[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getImageFormat() {
        try {
            return reader.getInput().getImageFormats().first();
        } catch (CoverageStoreException ex) {
            Logger.getLogger(CoverageSQLLayerDetails.class.getName()).log(Level.SEVERE, null, ex);
            return "unknown";
        }
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

}
