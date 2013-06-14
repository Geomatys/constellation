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
package org.constellation.provider.coveragefile;

import java.awt.Dimension;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;

import org.constellation.ServiceDef;
import org.constellation.provider.AbstractLayerDetails;
import org.constellation.provider.CoverageLayerDetails;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.StyleProviderProxy;

import org.geotoolkit.coverage.grid.GeneralGridGeometry;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.io.GridCoverageReadParam;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.image.io.metadata.SpatialMetadata;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.map.ElevationModel;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.MeasurementRange;

import org.opengis.feature.type.Name;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.operation.TransformException;
import org.opengis.sld.Layer;
import org.opengis.style.FeatureTypeStyle;
import org.opengis.style.RasterSymbolizer;
import org.opengis.style.Rule;
import org.opengis.style.ShadedRelief;
import org.opengis.style.Symbolizer;


/**
 * Regroups information about a {@linkplain Layer layer}.
 *
 * @version $Id$
 * @author Johann Sorel (Geomatys)
 */
class GridCoverageReaderLayerDetails extends AbstractLayerDetails implements CoverageLayerDetails {
    private final GridCoverageReader reader;

    private final Name elevationModel;

    /**
     * Stores information about a {@linkplain Layer layer} in a {@code PostGRID}
     * {@linkplain Database database}.
     *
     * @param Coverage The database connection.
     * @param layer The layer to consider in the database.
     */
    GridCoverageReaderLayerDetails(final GridCoverageReader reader, final List<String> favorites,
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

        final String title = getName().getLocalPart();
        final CoverageMapLayer mapLayer = MapBuilder.createCoverageLayer(reader, 0, style, title);
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
     * Returns the native envelope of this layer.
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
        return new TreeSet<Date>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Number> getAvailableElevations() throws DataStoreException {
        return new TreeSet<Number>();
    }

    /*
    private MutableStyle getDefaultStyle() {
        return StyleProviderProxy.STYLE_FACTORY.style(StyleProviderProxy.STYLE_FACTORY.rasterSymbolizer());
    }*/

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
        return null;
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
