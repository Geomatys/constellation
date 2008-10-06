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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.constellation.catalog.CatalogException;
import org.constellation.coverage.web.Service;
import org.constellation.query.wms.GetFeatureInfo;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.display.exception.PortrayalException;
import org.geotools.display.renderer.GlyphLegendFactory;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapLayer;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.util.MeasurementRange;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.PropertyName;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;


/**
 * Abstract LayerDetail used by Feature providers.
 * 
 * @version $Id$
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public abstract class AbstractFeatureLayerDetails implements LayerDetails {
    protected static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    protected static final GeographicBoundingBox DUMMY_BBOX =
            new GeographicBoundingBoxImpl(-180, 180, -77, +77);

    protected final FeatureSource<SimpleFeatureType,SimpleFeature> fs;
    protected final List<String> favorites;
    protected final String name;

    protected AbstractFeatureLayerDetails(String name, FeatureSource<SimpleFeatureType,SimpleFeature> fs, List<String> favorites){
        
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
                System.out.println(bbox);
                return bbox;
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return DUMMY_BBOX;
    }

    /**
     * {@inheritDoc}
     */
    public SortedSet<Date> getAvailableTimes() throws CatalogException {
        return Collections.unmodifiableSortedSet(new TreeSet<Date>());
    }

    /**
     * {@inheritDoc}
     */
    public SortedSet<Number> getAvailableElevations() throws CatalogException {
        return Collections.unmodifiableSortedSet(new TreeSet<Number>());
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

    /**
     * {@inheritDoc}
     */
    public Object getInformationAt(final GetFeatureInfo gfi) throws CatalogException, IOException {
        // Pixel coordinates in the request.
        final int pixelUpX        = gfi.getX();
        final int pixelUpY        = gfi.getY();
        final int pixelDownX      = pixelUpX + 1;
        final int pixelDownY      = pixelUpY + 1;
        final Envelope envObj     = gfi.getEnvelope();
        final double widthEnv     = envObj.getSpan(0);
        final double heightEnv    = envObj.getSpan(1);
        final int width           = gfi.getSize().width;
        final int height          = gfi.getSize().height;
        // Coordinates of the lower corner and upper corner of the objective envelope.
        final double lowerCornerX = widthEnv  * pixelUpX   / width  + envObj.getMinimum(0);
        final double lowerCornerY = heightEnv * pixelUpY   / height + envObj.getMinimum(1);
        final double upperCornerX = widthEnv  * pixelDownX / width  + envObj.getMinimum(0);
        final double upperCornerY = heightEnv * pixelDownY / height + envObj.getMinimum(1);

        final SimpleFeatureType sft = fs.getSchema();
        final CoordinateReferenceSystem crsObj = envObj.getCoordinateReferenceSystem();
        final CoordinateReferenceSystem crsData = sft.getCoordinateReferenceSystem();
        /* Here we build the final envelope on which to filter features.
         * If the objective crs is the same as the data one, then we do not have to apply
         * a transformation on the coordinates.
         */
        final ReferencedEnvelope filterEnv;
        if (!crsObj.equals(crsData)) {
            final GeneralEnvelope objEnv = new GeneralEnvelope(crsObj);
            objEnv.setRange(0, lowerCornerX, upperCornerX);
            objEnv.setRange(1, lowerCornerY, upperCornerY);
            try {
                filterEnv = new ReferencedEnvelope(CRS.transform(objEnv, crsData));
            } catch (TransformException t) {
                throw new CatalogException(t);
            } catch (MismatchedDimensionException m) {
                throw new CatalogException(m);
            }
        } else {
            filterEnv = new ReferencedEnvelope(lowerCornerX, upperCornerX, lowerCornerY, upperCornerY, crsData);
        }

        final Coordinate[] coord = new Coordinate[5];
        coord[0] = new Coordinate(filterEnv.getMinX(), filterEnv.getMinY());
        coord[1] = new Coordinate(filterEnv.getMinX(), filterEnv.getMaxY());
        coord[2] = new Coordinate(filterEnv.getMaxX(), filterEnv.getMaxY());
        coord[3] = new Coordinate(filterEnv.getMaxX(), filterEnv.getMinY());
        coord[4] = coord[0];
        final LinearRing lr1 = GEOMETRY_FACTORY.createLinearRing(coord);
        final Geometry geom = GEOMETRY_FACTORY.createPolygon(lr1, null);
        /* Now that we have the envelope, we need to know the name of the property which
         * stores the geometry (usually "the_geom").
         */
        final Name geomAtt = sft.getGeometryDescriptor().getName();
        final FilterFactory2 factory = CommonFactoryFinder.getFilterFactory2(null);
        final PropertyName geomProp = factory.property(geomAtt);
        final Filter filter = factory.intersects(geomProp, factory.literal(geom));

        // Apply the bbox filter on the feature source.
        final FeatureCollection<SimpleFeatureType, SimpleFeature> features = fs.getFeatures(filter);
        final FeatureIterator<SimpleFeature> featureIt = features.features();

        final List<SimpleFeature> requestedFeatures = new ArrayList<SimpleFeature>();
        while (featureIt.hasNext()) {
            final SimpleFeature feature = featureIt.next();
            if (feature == null) {
                continue;
            }
            requestedFeatures.add(feature);
        }
        featureIt.close();
        return requestedFeatures;
    }

    protected abstract MapLayer createMapLayer(Object style, final Map<String, Object> params) throws IOException;
    
}
