/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.provider;

import com.vividsolutions.jts.geom.GeometryFactory;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.constellation.catalog.CatalogException;
import org.constellation.coverage.catalog.Series;
import org.constellation.ws.Service;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.display.exception.PortrayalException;
import org.geotools.display.renderer.GlyphLegendFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapBuilder;
import org.geotools.map.MapLayer;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.util.MeasurementRange;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;


/**
 * Abstract LayerDetail used by Feature providers.
 * 
 * @version $Id$
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public abstract class AbstractFeatureLayerDetails implements LayerDetails {
    
    protected static final Logger LOGGER = Logger.getLogger("org.constellation.provider");
    protected static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    protected static final GeographicBoundingBox DUMMY_BBOX =
            new GeographicBoundingBoxImpl(-180, 180, -77, +77);
    protected static final MapBuilder MAP_BUILDER = MapBuilder.getInstance();
    /**
     * Defines the number of pixels we want to add to the specified coordinates given by
     * the GetFeatureInfo request.
     */
    protected static final int MARGIN = 4;

    protected final FeatureSource<SimpleFeatureType,SimpleFeature> fs;
    protected final List<String> favorites;
    protected final String name;
    protected final String dateStartField;
    protected final String dateEndField;
    protected final String elevationStartField;
    protected final String elevationEndField;

    protected AbstractFeatureLayerDetails(String name, FeatureSource<SimpleFeatureType,SimpleFeature> fs, List<String> favorites){
        this(name,fs,favorites,null,null,null,null);
        
    }
    
    protected AbstractFeatureLayerDetails(String name, FeatureSource<SimpleFeatureType,SimpleFeature> fs, List<String> favorites,
            String dateStart, String dateEnd, String elevationStart, String elevationEnd){
        
        if(fs == null){
            throw new NullPointerException("FeatureSource can not be null.");
        }
        
        this.name = name;
        this.fs = fs;

        if(favorites == null){
            this.favorites = Collections.emptyList();
        }else{
            this.favorites = Collections.unmodifiableList(favorites);
        }
        
        if(dateStart != null)       this.dateStartField = dateStart;
        else if(dateEnd != null)    this.dateStartField = dateEnd;
        else                        this.dateStartField = null;
        
        if(dateEnd != null)         this.dateEndField = dateEnd;
        else if(dateStart != null)  this.dateEndField = dateStart;
        else                        this.dateEndField = null;
        
        if(elevationStart != null)      this.elevationStartField = elevationStart;
        else if(elevationEnd != null)   this.elevationStartField = elevationEnd;
        else                            this.elevationStartField = null;
        
        if(elevationEnd != null)        this.elevationEndField = elevationEnd;
        else if(elevationStart != null) this.elevationEndField = elevationStart;
        else                            this.elevationEndField = null;
        
    }
    

    /**
     * {@inheritDoc}
     */
    public MapLayer getMapLayer(Object style, final Map<String, Object> params) throws PortrayalException{
        try {
            return createMapLayer(style, params);
        } catch (IOException ex) {
            throw new PortrayalException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getFavoriteStyles() {
        return favorites;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isQueryable(Service service) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public GeographicBoundingBox getGeographicBoundingBox() throws CatalogException {
        //TODO handle this correctly
        try{
            final ReferencedEnvelope env = fs.getBounds();

            Envelope renv = null;
            if(env.getCoordinateReferenceSystem().equals(DefaultGeographicCRS.WGS84)){
                renv = CRS.transform(env, DefaultGeographicCRS.WGS84);
            }

            if(renv != null){
                GeographicBoundingBox bbox = new GeographicBoundingBoxImpl(renv);
                return bbox;
            }

        }catch(Exception e){
            LOGGER.log(Level.WARNING , "Could not evaluate bounding box",e);
        }

        return DUMMY_BBOX;
    }

    /**
     * {@inheritDoc}
     */
    public SortedSet<Date> getAvailableTimes() throws CatalogException {
        final SortedSet<Date> dates = new TreeSet<Date>();
        
        if(dateStartField != null){
            
            final AttributeDescriptor desc = fs.getSchema().getDescriptor(dateStartField);
            if(desc == null){
                LOGGER.log(Level.WARNING , "Invalide field : "+ dateStartField + " Doesnt exists in layer :" + name);
                return dates;
            }
            
            final Class type = desc.getType().getBinding();
            if( !(Date.class.isAssignableFrom(type)) ){
                LOGGER.log(Level.WARNING , "Invalide field type for dates, layer " + name +", must be a Date, found a " + type);
                return dates;
            }
            
            final DefaultQuery query = new DefaultQuery();
            query.setPropertyNames(new String[]{dateStartField});
            
            FeatureIterator<SimpleFeature> features = null;
            try{
                final FeatureCollection<SimpleFeatureType,SimpleFeature> coll = fs.getFeatures(query);
                features = coll.features();
                while(features.hasNext()){
                    final SimpleFeature sf = features.next();
                    Date date = (Date) sf.getAttribute(dateStartField);
                    if(date != null){
                        dates.add(date);
                    }
                    
                }
                
            }catch(IOException ex){
                LOGGER.log(Level.WARNING , "Could not evaluate dates",ex);
            }finally{
                if(features != null) features.close();
            }
            
        }
        
        return dates;
    }

    /**
     * {@inheritDoc}
     */
    public SortedSet<Number> getAvailableElevations() throws CatalogException {
        final SortedSet<Number> elevations = new TreeSet<Number>();
        
        if(elevationStartField != null){

            final AttributeDescriptor desc = fs.getSchema().getDescriptor(elevationStartField);
            if(desc == null){
                LOGGER.log(Level.WARNING , "Invalide field : "+ elevationStartField + " Doesnt exists in layer :" + name);
                return elevations;
            }
            
            final Class type = desc.getType().getBinding();
            if( !(Number.class.isAssignableFrom(type)) ){
                LOGGER.log(Level.WARNING , "Invalide field type for elevations, layer " + name +", must be a Number, found a " + type);
                return elevations;
            }
            
            final DefaultQuery query = new DefaultQuery();
            query.setPropertyNames(new String[]{elevationStartField});
            
            FeatureIterator<SimpleFeature> features = null;
            try{
                final FeatureCollection<SimpleFeatureType,SimpleFeature> coll = fs.getFeatures(query);
                features = coll.features();
                while(features.hasNext()){
                    final SimpleFeature sf = features.next();
                    Number date = (Number) sf.getAttribute(elevationStartField);
                    if(date != null){
                        elevations.add(date);
                    }
                    
                }
                
            }catch(IOException ex){
                LOGGER.log(Level.WARNING , "Could not evaluate elevationss",ex);
            }finally{
                if(features != null) features.close();
            }
            
        }
        
        return elevations;
    }

    /**
     * {@inheritDoc}
     */
    public MeasurementRange<?>[] getSampleValueRanges() {
        return new MeasurementRange<?>[0];
    }

    /**
     * {@inheritDoc}
     */
    public String getRemarks() {
        //TODO we should get this from metadata associated to the layer.
        return "Vector datas";
    }

    /**
     * {@inheritDoc}
     */
    public String getThematic() {
        //TODO we should get this from metadata associated to the layer.
        return "Vector datas";
    }

    /**
     * {@inheritDoc}
     */
    public BufferedImage getLegendGraphic(final Dimension dimension) {
        final GlyphLegendFactory sldFact = new GlyphLegendFactory();
        return sldFact.create(RANDOM_FACTORY.createDefaultVectorStyle(fs), dimension);
    }

    public GridCoverage2D getCoverage(final Envelope envelope, final Dimension dimension,
            final Double elevation, final Date time) throws CatalogException, IOException
    {
        return null;
    }

    /**
     * Should not have been called in this implementation.
     */
    public Set<Series> getSeries() {
        throw new UnsupportedOperationException();
    }
    
    protected Query createQuery(final Date date, final Number elevation){
        final DefaultQuery query = new DefaultQuery();
        final StringBuilder builder = new StringBuilder();
        
        if (date != null && this.dateStartField != null) {
            //make the date CQL
            builder.append("(").append(this.dateStartField).append(" <= '").append(date).append("'");
            builder.append(" AND ");
            builder.append(this.dateEndField).append(" >= '").append(date).append("'").append(")");
        }
        
        if(elevation != null && this.elevationStartField != null){
            //make the elevation CQL
            
            if(builder.length() >0){
                builder.append(" AND ");
            }
            
            builder.append("(").append(this.elevationStartField).append(" <= '").append(elevation.floatValue()).append("'");
            builder.append(" AND ");
            builder.append(this.elevationEndField).append(" >= '").append(elevation.floatValue()).append("'").append(")");
        }
        
        final String cqlQuery = builder.toString();
        if(cqlQuery != null && !cqlQuery.isEmpty()){
            try {
                query.setFilter(CQL.toFilter(cqlQuery));
            } catch (CQLException ex) {
                LOGGER.log(Level.SEVERE, "Could not parse CQL query", ex);
            }
        }
        
        return query;
    }
    
    protected abstract MapLayer createMapLayer(Object style, final Map<String, Object> params) throws IOException;
    
}
