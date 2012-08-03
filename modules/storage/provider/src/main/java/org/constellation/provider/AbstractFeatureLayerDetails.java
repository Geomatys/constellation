/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2010, Geomatys
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
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;

import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.data.DataStore;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.display.exception.PortrayalException;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.filter.text.cql2.CQL;
import org.geotoolkit.filter.text.cql2.CQLException;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.MeasurementRange;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.PropertyName;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;


/**
 * Abstract LayerDetail used by Feature providers.
 *
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public abstract class AbstractFeatureLayerDetails extends AbstractLayerDetails implements FeatureLayerDetails {

    protected static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    protected static final GeographicBoundingBox DUMMY_BBOX =
            new DefaultGeographicBoundingBox(-180, 180, -77, +77);
    /**
     * Defines the number of pixels we want to add to the specified coordinates given by
     * the GetFeatureInfo request.
     */
    protected static final int MARGIN = 4;

    protected final DataStore store;
    protected final PropertyName dateStartField;
    protected final PropertyName dateEndField;
    protected final PropertyName elevationStartField;
    protected final PropertyName elevationEndField;

    protected AbstractFeatureLayerDetails(Name name, DataStore store, List<String> favorites){
        this(name,store,favorites,null,null,null,null);

    }

    protected AbstractFeatureLayerDetails(Name name, DataStore store, List<String> favorites,
            String dateStart, String dateEnd, String elevationStart, String elevationEnd){
        super(name,favorites);

        if(store == null){
            throw new IllegalArgumentException("FeatureSource can not be null.");
        }
        /*try {
            if (!store.getNames().contains(name)) {
                throw new IllegalArgumentException("Provided name " + name + " is not in the datastore known names");
            }
        } catch (DataStoreException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
        }*/

        this.store = store;

        final FilterFactory ff = FactoryFinder.getFilterFactory(null);

        if(dateStart != null)       this.dateStartField = ff.property(dateStart);
        else                        this.dateStartField = null;

        if(dateEnd != null)         this.dateEndField = ff.property(dateEnd);
        else                        this.dateEndField = null;

        if(elevationStart != null)      this.elevationStartField = ff.property(elevationStart);
        else                            this.elevationStartField = null;

        if(elevationEnd != null)        this.elevationEndField = ff.property(elevationEnd);
        else                            this.elevationEndField = null;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataStore getStore(){
        return store;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final MapLayer getMapLayer(MutableStyle style, final Map<String, Object> params) throws PortrayalException{

        final MapLayer layer;
        try {
            layer = createMapLayer(style, params);
        } catch (DataStoreException ex) {
            throw new PortrayalException(ex);
        }

        // EXTRA FILTER extra parameter ////////////////////////////////////////
        if (params != null && layer instanceof FeatureMapLayer) {
            final Map<String,?> extras = (Map<String, ?>) params.get(KEY_EXTRA_PARAMETERS);
            if(extras != null){
                for(String key : extras.keySet()){
                    if(key.equalsIgnoreCase("cql_filter")){
                        final String cqlFilter = ((List)extras.get(key)).get(0).toString();
                        if(cqlFilter == null){
                            break;
                        }
                        try {
                            final Filter filter = CQL.toFilter(cqlFilter);
                            if(filter != null){
                                final FeatureMapLayer fml = (FeatureMapLayer) layer;
                                fml.setQuery(QueryBuilder.filtered(fml.getCollection().getFeatureType().getName(), filter));
                            }

                        } catch (CQLException ex) {
                            LOGGER.log(Level.INFO,  ex.getMessage(),ex);
                        }
                        break;
                    }
                }
            }
        }
        ////////////////////////////////////////////////////////////////////////

        return layer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GeographicBoundingBox getGeographicBoundingBox() throws DataStoreException {
        try{
            Envelope env = store.getEnvelope(QueryBuilder.all(name));
            if(!CRS.equalsIgnoreMetadata(env.getCoordinateReferenceSystem(), DefaultGeographicCRS.WGS84)){
                env = CRS.transform(env, DefaultGeographicCRS.WGS84);
            }

            if(env != null){
                return new DefaultGeographicBoundingBox(env);
            }

        }catch(Exception e){
            LOGGER.log(Level.WARNING , "Could not evaluate the bounding box for the layer \"" +getName() +"\". " +
                    "The selected one by defaut will be: " + DUMMY_BBOX, e);
        }

        return DUMMY_BBOX;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Date> getAvailableTimes() throws DataStoreException {
        final SortedSet<Date> dates = new TreeSet<Date>();
        FeatureIterator<SimpleFeature> features = null;
        if(dateStartField != null){
            try{
                final AttributeDescriptor desc = (AttributeDescriptor)
                        dateStartField.evaluate((SimpleFeatureType)store.getFeatureType(name));

                if(desc == null){
                    LOGGER.log(Level.WARNING , "Invalide field : "+ dateStartField + " Doesnt exists in layer :" + name);
                    return dates;
                }

                final Class type = desc.getType().getBinding();
                if( !(Date.class.isAssignableFrom(type)) ){
                    LOGGER.log(Level.WARNING , "Invalide field type for dates, layer " + name +", must be a Date, found a " + type);
                    return dates;
                }

                final QueryBuilder builder = new QueryBuilder();
                builder.setTypeName(name);
                builder.setProperties(new String[]{dateStartField.getPropertyName()});
                final Query query = builder.buildQuery();

                final FeatureCollection<SimpleFeature> coll = store.createSession(false).getFeatureCollection(query);
                features = coll.iterator();
                while(features.hasNext()){
                    final SimpleFeature sf = features.next();
                    final Date date = dateStartField.evaluate(sf,Date.class);
                    if(date != null){
                        dates.add(date);
                    }
                }
            } catch(DataStoreException ex) {
                LOGGER.log(Level.WARNING , "Could not evaluate dates",ex);
            } finally {
                if(features != null) features.close();
            }

        }

        return dates;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Number> getAvailableElevations() throws DataStoreException {
        final SortedSet<Number> elevations = new TreeSet<Number>();
        FeatureIterator<SimpleFeature> features = null;
        if (elevationStartField != null) {

            try {
                final AttributeDescriptor desc = (AttributeDescriptor)
                        elevationStartField.evaluate((SimpleFeatureType)store.getFeatureType(name));
                if(desc == null){
                    LOGGER.log(Level.WARNING , "Invalide field : "+ elevationStartField + " Doesnt exists in layer :" + name);
                    return elevations;
                }

                final Class type = desc.getType().getBinding();
                if (!(Number.class.isAssignableFrom(type)) ){
                    LOGGER.log(Level.WARNING , "Invalide field type for elevations, layer " + name +", must be a Number, found a " + type);
                    return elevations;
                }

                final QueryBuilder builder = new QueryBuilder();
                builder.setTypeName(name);
                builder.setProperties(new String[]{elevationStartField.getPropertyName()});
                final Query query = builder.buildQuery();

                final FeatureCollection<SimpleFeature> coll = store.createSession(false).getFeatureCollection(query);
                features = coll.iterator();
                while(features.hasNext()){
                    final SimpleFeature sf = features.next();
                    final Number ele = elevationStartField.evaluate(sf,Number.class);
                    if(ele != null){
                        elevations.add(ele);
                    }

                }

            } catch(DataStoreException ex) {
                LOGGER.log(Level.WARNING , "Could not evaluate elevationss",ex);
            } finally {
                if(features != null) features.close();
            }

        }

        return elevations;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MeasurementRange<?>[] getSampleValueRanges() {
        return new MeasurementRange<?>[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected MutableStyle getDefaultStyle(){
        try {
            return StyleProviderProxy.STYLE_RANDOM_FACTORY.createDefaultVectorStyle(store.getFeatureType(name));
        } catch (DataStoreException ex) {
            return StyleProviderProxy.STYLE_RANDOM_FACTORY.createPolygonStyle();
        }
    }

    /**
     * Returns {@code null}. This method should not be used in this context.
     *
     * @todo the super class should probably not define this method as abstract.
     */
    @Override
    public GridCoverage2D getCoverage(final Envelope envelope, final Dimension dimension,
            final Double elevation, final Date time) throws DataStoreException, IOException
    {
        return null;
    }

    /**
     * Returns a {@linkplain FeatureCollection feature collection} containing all the data.
     */
    @Override
    public Object getOrigin() {
        return store.createSession(false).getFeatureCollection(QueryBuilder.all(name));
    }


    /**
     * Specifies that the type of this layer is feature.
     */
    @Override
    public TYPE getType() {
        return TYPE.FEATURE;
    }

    protected abstract MapLayer createMapLayer(MutableStyle style, final Map<String, Object> params) throws DataStoreException;

}
