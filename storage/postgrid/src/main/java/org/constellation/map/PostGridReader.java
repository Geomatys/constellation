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
package org.constellation.map;

import java.awt.geom.Dimension2D;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.constellation.catalog.NoSuchTableException;
import org.constellation.coverage.catalog.CoverageReference;
import org.constellation.coverage.catalog.GridCoverageTable;
import org.constellation.coverage.catalog.Layer;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.io.CoverageReadParam;
import org.geotools.coverage.io.CoverageReader;
import org.geotools.coverage.processing.Operations;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.resources.geometry.XDimension2D;
import org.geotools.util.MeasurementRange;

import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class PostGridReader implements CoverageReader{
    
    private final Logger LOGGER = Logger.getLogger("org/constellation/map/PostGridReader");

    private final GridCoverageTable table;
    
    public PostGridReader(final GridCoverageTable table){
        this.table = table;
    }

    public PostGridReader(final Database database, final Layer layer){
        GridCoverageTable gridTable = null;
        try {
            gridTable = database.getTable(GridCoverageTable.class);
        } catch (NoSuchTableException ex) {
            LOGGER.log(Level.SEVERE, "No GridCoverageTable", ex);
        }
        //create a mutable copy
        this.table = new GridCoverageTable(gridTable);
        this.table.setLayer(layer);
    }

    public GridCoverageTable getTable(){
        return table;
    }

    public synchronized GridCoverage2D read(final CoverageReadParam param, final double elevation,
            final Date time, final MeasurementRange dimRange)
            throws FactoryException, TransformException, IOException{

        table.setTimeRange(time, time);
        table.setVerticalRange(elevation, elevation);

        return read(param);
    }


    public synchronized GridCoverage2D read(final CoverageReadParam param) throws FactoryException, TransformException, IOException {
        
        if(param == null){
            
            //no parameters, return the complete image
            GridCoverage2D coverage = null;
            try {
                CoverageReference ref = table.getEntry();
                if(ref == null){
                    //TODO sometimes the postgrid reader go a bit crazy and found no coveragreference
                    //if so clear the cache and retry
                    table.flush();
                    ref = table.getEntry();
                }
                
                if(ref != null) coverage = ref.getCoverage(null);
                
            } catch (CatalogException ex) {
            //TODO fix in postgrid
            //catch anything, looks like sometimes, postgrid throw an ArithmeticException
//        Exception in thread "Thread-4" java.lang.ArithmeticException: Le calcul ne converge pas pour les points 89°20,3'W 00°06,6'S et 91°06,2'E 00°06,6'S.
//        at org.geotools.referencing.datum.DefaultEllipsoid.orthodromicDistance(DefaultEllipsoid.java:507)
//        at org.constellation.coverage.catalog.CoverageComparator.getArea(CoverageComparator.java:181)
                throw new IOException(ex);
            } catch (SQLException ex){
                throw new IOException(ex);
            }
            
            return coverage;
        }
        
        Envelope requestEnvelope = param.getEnveloppe();
        final CoordinateReferenceSystem requestCRS = requestEnvelope.getCoordinateReferenceSystem();
        final double[] objResolution = param.getResolution();
        final CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
        
        DirectPosition dataResolution = new DirectPosition2D(
                    requestEnvelope.getCoordinateReferenceSystem(), 
                    objResolution[0], 
                    objResolution[1]);
        
        if(!requestCRS.equals(crs)){
            //reproject requested enveloppe to dataCRS
            final MathTransform objToData = CRS.findMathTransform(
                    requestEnvelope.getCoordinateReferenceSystem(), crs, true);
            requestEnvelope = CRS.transform(objToData, requestEnvelope);
            dataResolution = objToData.transform(dataResolution, dataResolution);
        }
        
        final GeographicBoundingBox bbox = new GeographicBoundingBoxImpl(requestEnvelope);
        final Dimension2D resolution = new XDimension2D.Double(
                Math.abs(dataResolution.getOrdinate(0)),
                Math.abs(dataResolution.getOrdinate(1)) );

        table.setGeographicBoundingBox(bbox);
        table.setPreferredResolution(resolution);
                
        GridCoverage2D coverage = null;
        try {
            CoverageReference ref = table.getEntry();
            if(ref == null){
                //TODO sometimes the postgrid reader go a bit crazy and found no coveragreference
                //if so clear the cache and retry
                table.flush();
                ref = table.getEntry();
            }
            
            if(ref != null) coverage = ref.getCoverage(null);

        } catch (CatalogException ex) {
            //TODO fix in postgrid
            //catch anything, looks like sometimes, postgrid throw an ArithmeticException
//        Exception in thread "Thread-4" java.lang.ArithmeticException: Le calcul ne converge pas pour les points 89°20,3'W 00°06,6'S et 91°06,2'E 00°06,6'S.
//        at org.geotools.referencing.datum.DefaultEllipsoid.orthodromicDistance(DefaultEllipsoid.java:507)
//        at org.constellation.coverage.catalog.CoverageComparator.getArea(CoverageComparator.java:181)
            
            throw new IOException(ex);
        } catch (SQLException ex){
            throw new IOException(ex);
        }
        
        if(coverage != null){
            coverage = (GridCoverage2D) Operations.DEFAULT.resample(coverage, requestCRS);
        }
        return coverage;
    }

    public ReferencedEnvelope getCoverageBounds() {
        final CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
        final GeographicBoundingBox bbox;
        try {
            bbox = table.getLayer().getGeographicBoundingBox();
        } catch (CatalogException ex) {
            LOGGER.warning(ex.getLocalizedMessage());
            return new ReferencedEnvelope(crs);
        }
        return new ReferencedEnvelope(bbox.getWestBoundLongitude(),
                bbox.getEastBoundLongitude(),
                bbox.getSouthBoundLatitude(),
                bbox.getNorthBoundLatitude(),
                crs);
    }

}
