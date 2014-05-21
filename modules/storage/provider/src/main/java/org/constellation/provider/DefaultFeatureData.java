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
import org.apache.sis.measure.MeasurementRange;
import org.apache.sis.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.apache.sis.storage.DataStoreException;
import static org.constellation.provider.AbstractData.LOGGER;
import static org.constellation.provider.Data.KEY_EXTRA_PARAMETERS;
import org.geotoolkit.coverage.grid.GridCoverage2D;

import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.RandomStyleBuilder;
import org.geotoolkit.feature.simple.SimpleFeature;
import org.geotoolkit.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.PropertyName;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;

/**
 * Default layer details for a datastore type.
 *
 * @author Johann Sorel (Geomatys)
 */
public class DefaultFeatureData extends AbstractData implements FeatureData {

    
    protected static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    protected static final GeographicBoundingBox DUMMY_BBOX =
            new DefaultGeographicBoundingBox(-180, 180, -77, +77);
    /**
     * Defines the number of pixels we want to add to the specified coordinates given by
     * the GetFeatureInfo request.
     */
    protected static final int MARGIN = 4;

    protected final FeatureStore store;
    protected final PropertyName dateStartField;
    protected final PropertyName dateEndField;
    protected final PropertyName elevationStartField;
    protected final PropertyName elevationEndField;

    /**
     * Data version date. Use to query Features is input FeatureStore is versioned.
     */
    protected final Date versionDate;
    
    /**
     * Build a FeatureLayerDetails with layer name, store and favorite style names.
     *
     * @param name layer name
     * @param store FeatureStore
     * @param favorites style names
     */
    public DefaultFeatureData(Name name, FeatureStore store, List<String> favorites){
        this(name,store,favorites,null,null,null,null,null);
    }

    /**
     * Build a FeatureLayerDetails with layer name, store, favorite style names and data version date.
     *
     * @param name layer name
     * @param store FeatureStore
     * @param favorites style names
     * @param versionDate data version date of the layer (can be null)
     */
    public DefaultFeatureData(Name name, FeatureStore store, List<String> favorites, Date versionDate){
        this(name,store,favorites,null,null,null,null, versionDate);
    }

    /**
     * Build a FeatureLayerDetails with layer name, store, favorite style names and temporal/elevation filters.
     *
     * @param name layer name
     * @param store FeatureStore
     * @param favorites style names
     * @param dateStart temporal filter start
     * @param dateEnd temporal filter end
     * @param elevationStart elevation filter start
     * @param elevationEnd elevation filter end
     */
    public DefaultFeatureData(Name name, FeatureStore store, List<String> favorites,
            String dateStart, String dateEnd, String elevationStart, String elevationEnd){
        this(name,store,favorites,dateStart,dateEnd,elevationStart,elevationEnd , null);
    }

    /**
     * Build a FeatureLayerDetails with layer name, store, favorite style names, temporal/elevation filters and
     * data version date.
     *
     * @param name layer name
     * @param store FeatureStore
     * @param favorites style names
     * @param dateStart temporal filter start
     * @param dateEnd temporal filter end
     * @param elevationStart elevation filter start
     * @param elevationEnd elevation filter end
     * @param versionDate data version date of the layer (can be null)
     */
    public DefaultFeatureData(Name name, FeatureStore store, List<String> favorites,
                                        String dateStart, String dateEnd, String elevationStart, String elevationEnd, Date versionDate){
        
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
        this.versionDate = versionDate;

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

    protected MapLayer createMapLayer(MutableStyle style, final Map<String, Object> params) throws DataStoreException {
        if(style == null && favorites.size() > 0){
            //no style provided, try to get the favorite one
            //there are some favorites styles
            final String namedStyle = favorites.get(0);
            style = StyleProviders.getInstance().get(namedStyle);
        }

        final FeatureType featureType = store.getFeatureType(name);
        if(style == null){
            //no favorites defined, create a default one
            style = RandomStyleBuilder.createDefaultVectorStyle(featureType);
        }

        final FeatureMapLayer layer = MapBuilder.createFeatureLayer((FeatureCollection)getOrigin(), style);

        final String title = getName().getLocalPart();
        layer.setName(title);
        layer.setDescription(StyleProviders.STYLE_FACTORY.description(title,title));

        return layer;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public FeatureStore getStore(){
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
            if (extras != null){
                Filter filter = null;
                for (String key : extras.keySet()) {
                    if (key.equalsIgnoreCase("cql_filter")) {
                        final Object extra = extras.get(key);
                        String cqlFilter = null;
                        if (extra instanceof List) {
                            cqlFilter = ((List) extra).get(0).toString();
                        } else if (extra instanceof String){
                            cqlFilter = (String)extra;
                        }
                        if (cqlFilter != null) {
                            filter = buildCQLFilter(cqlFilter, filter);
                        }
                    } else if (key.startsWith("dim_") || key.startsWith("DIM_")) {
                        final String dimValue = ((List) extras.get(key)).get(0).toString();
                        final String dimName = key.substring(4);
                        filter = buildDimFilter(dimName, dimValue, filter);
                    }
                }
                if (filter != null) {
                    final FeatureMapLayer fml = (FeatureMapLayer) layer;
                    final FeatureType type = fml.getCollection().getFeatureType();
                    if (filter instanceof PropertyIsEqualTo) {
                        final String propName = ((PropertyName)((PropertyIsEqualTo)filter).getExpression1()).getPropertyName();
                        for (PropertyDescriptor desc : type.getDescriptors()) {
                            if (desc.getName().getLocalPart().equalsIgnoreCase(propName)) {
                                fml.setQuery(QueryBuilder.filtered(type.getName(), filter));
                                break;
                            }
                        }
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
    public Envelope getEnvelope() throws DataStoreException {
        final QueryBuilder query = new QueryBuilder(name);
        query.setVersionDate(versionDate);
        return store.getEnvelope(query.buildQuery());
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Date> getAvailableTimes() throws DataStoreException {
        final SortedSet<Date> dates = new TreeSet<>();
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
                builder.setVersionDate(versionDate);
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
        final SortedSet<Number> elevations = new TreeSet<>();
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
                builder.setVersionDate(versionDate);
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
     
    private MutableStyle getDefaultStyle(){
        try {
            return StyleProviderProxy.STYLE_RANDOM_FACTORY.createDefaultVectorStyle(store.getFeatureType(name));
        } catch (DataStoreException ex) {
            return StyleProviderProxy.STYLE_RANDOM_FACTORY.createPolygonStyle();
        }
    }*/

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
        final QueryBuilder builder = new QueryBuilder();
        builder.setTypeName(name);

        //build query using versionDate if not null and sotre support versioning.
        if (store.getQueryCapabilities().handleVersioning()) {
            if (versionDate != null) {
                builder.setVersionDate(versionDate);
            }
        }

        final Query query =  builder.buildQuery();
        return store.createSession(false).getFeatureCollection(query);
    }


    /**
     * Specifies that the type of this layer is feature.
     */
    @Override
    public TYPE getType() {
        return TYPE.FEATURE;
    }

    
}
