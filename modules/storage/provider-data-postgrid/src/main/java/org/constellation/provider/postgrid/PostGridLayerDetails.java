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

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import org.constellation.ServiceDef;

import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.constellation.coverage.catalog.CoverageReference;
import org.constellation.coverage.catalog.GridCoverageTable;
import org.constellation.coverage.catalog.Layer;
import org.constellation.coverage.catalog.Series;
import org.constellation.ws.ServiceType;
import org.constellation.map.PostGridMapLayer;
import org.constellation.map.PostGridReader;
import org.constellation.provider.CoverageLayerDetails;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.StyleProviderProxy;

import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.display.exception.PortrayalException;
import org.geotoolkit.display.shape.DoubleDimension2D;
import org.geotoolkit.display2d.ext.legend.LegendTemplate;
import org.geotoolkit.geometry.GeneralEnvelope;
import org.geotoolkit.map.ElevationModel;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.MeasurementRange;

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
 * @version $Id$
 * @author Cédric Briançon
 */
class PostGridLayerDetails implements CoverageLayerDetails {
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
            final Double elevation, final Date time) throws CatalogException, IOException{

        final int width = dimension.width;
        final int height = dimension.height;
        final Envelope genv;
        try {
            genv = CRS.transform(envelope, DefaultGeographicCRS.WGS84);
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
        final PostGridMapLayer mapLayer = new PostGridMapLayer(reader);

        mapLayer.setName(getName());

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
        
        mapLayer.setStyle(style);

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
    public boolean isQueryable(ServiceDef.Query query) {
        ServiceType service;
        switch(query.specification){
            case CSW : service = ServiceType.CSW; break;
            case SOS : service = ServiceType.SOS; break;
            case WCS : service = ServiceType.WCS; break;
            case WFS : service = ServiceType.WFS; break;
            case WMS : service = ServiceType.WMS; break;
            default: service = ServiceType.OTHER;
        }

        if(query == ServiceDef.Query.WMS_GETINFO){
            service = ServiceType.GETINFO;
        }

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
    public BufferedImage getLegendGraphic(final Dimension dimension, final LegendTemplate template) {
        return reader.getTable().getLayer().getLegend((dimension != null) ? dimension : LEGEND_SIZE);
    }

    /**
     * Returns the default legend size for all postgrid layers.
     *
     * @param template Not used in this implementation.
     * @param ms Not used in this implementation.
     * @return The default legend size.
     * @throws PortrayalException never thrown in this implementation.
     */
    @Override
    public Dimension getPreferredLegendSize(final LegendTemplate template, final MutableStyle ms) throws PortrayalException {
        return LEGEND_SIZE;
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
    public String getImageFormat() {
        final Set<Series> series = reader.getTable().getLayer().getSeries();
        for(Series ser : series){
            String type = ser.getFormat().getImageFormat();
            if(type != null) return type;
        }

        return null;
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
