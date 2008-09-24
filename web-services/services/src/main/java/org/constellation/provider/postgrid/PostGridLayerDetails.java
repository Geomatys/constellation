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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.constellation.coverage.catalog.CoverageReference;
import org.constellation.coverage.catalog.Layer;
import org.constellation.coverage.web.Service;
import org.constellation.coverage.web.WebServiceException;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.NamedStyleDP;
import org.constellation.query.wms.WMSQuery;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.map.GraphicBuilder;
import org.geotools.map.MapLayer;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.style.MutableStyle;
import org.geotools.util.MeasurementRange;

import org.opengis.geometry.DirectPosition;
import org.opengis.metadata.extent.GeographicBoundingBox;


/**
 * Regroups information about a {@linkplain Layer layer}.
 *
 * @version $Id$
 * @author Cédric Briançon
 */
class PostGridLayerDetails implements LayerDetails {
    /**
     * The database connection.
     */
    private final Database database;

    /**
     * Current layer to consider.
     */
    private final Layer layer;
    
    /**
     * Favorites styles associated with this layer.
     */
    private final List<String> favorites;

    /**
     * Stores information about a {@linkplain Layer layer} in a {@code PostGRID}
     * {@linkplain Database database}.
     *
     * @param database The database connection.
     * @param layer The layer to consider in the database.
     */
    PostGridLayerDetails(final Database database, final Layer layer, final List<String> favorites) {
        this.database = database;
        this.layer = layer;

        if (favorites == null) {
            this.favorites = Collections.emptyList();
        } else {
            this.favorites = favorites;
        }
    }

    /**
     * {@inheritDoc}
     */
    public double getInformationAt(final double x, final double y, final Date time,
                                   final double elevation) throws CatalogException
    {
        final CoverageReference coverage = layer.getCoverageReference(time, elevation);
        try {
            final GridCoverage2D grid2D = coverage.getCoverage(null);
            final DirectPosition position = new DirectPosition2D(x, y);
            final Object result = grid2D.evaluate(position);
            if (result instanceof Double) {
                return (Double) result;
            } else {
                throw new CatalogException(Errors.format(ErrorKeys.CANT_CONVERT_FROM_TYPE_$1, Double.class));
            }
        } catch (IOException io) {
            throw new CatalogException(io);
        }
    }

    /**
     * {@inheritDoc}
     */
    public MapLayer getMapLayer(Object style, final Map<String, Object> params) {
        return createMapLayer(style, params);
    }
    
    /**
     * {@inheritDoc}
     */
    public String getName() {
        return layer.getName();
    }
    
    /**
     * {@inheritDoc}
     */
    public List<String> getFavoriteStyles(){
        return favorites;
    }   

    /**
     * {@inheritDoc}
     */
    public boolean isQueryable(Service service) {
        return layer.isQueryable(service);
    }

    /**
     * {@inheritDoc}
     */
    public GeographicBoundingBox getGeographicBoundingBox() throws CatalogException {
        return layer.getGeographicBoundingBox();
    }

    /**
     * {@inheritDoc}
     */
    public SortedSet<Date> getAvailableTimes() throws CatalogException {
        return layer.getAvailableTimes();
    }

    /**
     * {@inheritDoc}
     */
    public SortedSet<Number> getAvailableElevations() throws CatalogException {
        return layer.getAvailableElevations();
    }

    /**
     * {@inheritDoc}
     */
    public BufferedImage getLegendGraphic(final Dimension dimension) {
        return layer.getLegend((dimension != null) ? dimension : LEGEND_SIZE);
    }

    /**
     * {@inheritDoc}
     */
    public MeasurementRange<?>[] getSampleValueRanges() {
        return layer.getSampleValueRanges();
    }

    /**
     * {@inheritDoc}
     */
    public String getRemarks() {
        return layer.getRemarks();
    }

    /**
     * {@inheritDoc}
     */
    public String getThematic() {
        return layer.getThematic();
    }
    
    private MapLayer createMapLayer(Object style, final Map<String, Object> params){
        final PostGridMapLayer mapLayer = new PostGridMapLayer(database, layer);
        
        if(style == null){
            //no style provided, try to get the favorite one
            if(favorites.size() > 0){
                //there are some favorites styles
                style = favorites.get(0);
            }else{
                //no favorites defined, create a default one
                style = RANDOM_FACTORY.createRasterStyle();
            }
        }
                
        if(style instanceof String){
            //the given style is a named style
            style = NamedStyleDP.getInstance().get((String)style);
            if(style == null){
                //somehting is wrong, the named style doesnt exist, create a default one
                style = RANDOM_FACTORY.createRasterStyle();
            }
        }
               
        if(style instanceof MutableStyle){
            //style is a commun SLD style
            mapLayer.setStyle((MutableStyle) style);
        }else if( style instanceof GraphicBuilder){
            //special graphic builder
            mapLayer.setStyle(RANDOM_FACTORY.createRasterStyle());
            mapLayer.graphicBuilders().add((GraphicBuilder) style);
        }else{
            //style is unknowed type, use a random style
            mapLayer.setStyle(RANDOM_FACTORY.createRasterStyle());
        }
        
        if (params != null) {
            mapLayer.setDimRange((MeasurementRange) params.get(WMSQuery.KEY_DIM_RANGE));
            final Double elevation = (Double) params.get(WMSQuery.KEY_ELEVATION);
            if (elevation != null) {
                mapLayer.setElevation(elevation);
            }
            final Date date = (Date) params.get(WMSQuery.KEY_TIME);
            if (date != null) {
                mapLayer.times().add(date);
            }
        }
        
        return mapLayer;
    }
}
