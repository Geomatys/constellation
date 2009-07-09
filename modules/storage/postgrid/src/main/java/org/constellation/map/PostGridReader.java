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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.constellation.catalog.NoSuchTableException;
import org.constellation.coverage.catalog.CoverageReference;
import org.constellation.coverage.catalog.GridCoverageTable;
import org.constellation.coverage.catalog.Layer;

import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.io.CoverageReadParam;
import org.geotoolkit.coverage.io.CoverageReader;
import org.geotoolkit.coverage.processing.Operations;
import org.geotoolkit.display.shape.DoubleDimension2D;
import org.geotoolkit.geometry.DirectPosition2D;
import org.geotoolkit.geometry.GeneralEnvelope;
import org.geotoolkit.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.geotoolkit.util.logging.Logging;

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
    
    private final Logger LOGGER = Logging.getLogger(PostGridReader.class);

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

    @Override
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
//        at org.geotoolkit.referencing.datum.DefaultEllipsoid.orthodromicDistance(DefaultEllipsoid.java:507)
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

        //we would like to quey postgrid in the real asked CRS, but only CRS:84 can be used.
//        final GeographicBoundingBox bbox = new DefaultGeographicBoundingBox(requestEnvelope);
//        final Dimension2D resolution = new DoubleDimension2D(objResolution[0],objResolution[1]);
//
//        table.setGeographicBoundingBox(bbox);
//        table.setPreferredResolution(resolution);
//
//        GridCoverage2D coverage = null;
//        try {
//            coverage = (GridCoverage2D) table.asCoverage();
//        } catch (CatalogException ex) {
//            Logger.getLogger(PostGridReader.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (SQLException ex) {
//            Logger.getLogger(PostGridReader.class.getName()).log(Level.SEVERE, null, ex);
//        }


        //calculate the resolution at the center of the requested envelope
        //we must use the center since conique or other kind of projection may
        //have unlinear resolution
        final CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;

        DirectPosition point1 = new DirectPosition2D(
                    requestEnvelope.getCoordinateReferenceSystem(),
                    requestEnvelope.getSpan(0),
                    requestEnvelope.getSpan(1));

        DirectPosition point2 = new DirectPosition2D(
                    requestEnvelope.getCoordinateReferenceSystem(),
                    requestEnvelope.getSpan(0) + objResolution[0],
                    requestEnvelope.getSpan(1) + objResolution[1]);

        if(!requestCRS.equals(crs)){
            //reproject requested enveloppe to dataCRS
            final MathTransform objToData = CRS.findMathTransform(
                    requestEnvelope.getCoordinateReferenceSystem(), crs, true);
            requestEnvelope = CRS.transform(objToData, requestEnvelope);
            point1 = objToData.transform(point1, point1);
            point2 = objToData.transform(point2, point2);
        }

        final GeographicBoundingBox bbox = new DefaultGeographicBoundingBox(requestEnvelope);
        final Dimension2D resolution = new DoubleDimension2D(
                Math.abs( Math.abs(point2.getOrdinate(0)) - Math.abs(point1.getOrdinate(0))),
                Math.abs( Math.abs(point2.getOrdinate(1)) - Math.abs(point1.getOrdinate(1))) );

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
            else{
                throw new CatalogException("No coverage reference found for layer : " + getTable().getLayer().getName());
            }

        } catch (SQLException ex){
            throw new IOException(ex);
        } catch (Exception ex) {
            //TODO fix in postgrid
            //catch anything, looks like sometimes, postgrid throw an ArithmeticException or IllegalArgumentException
//        Exception in thread "Thread-4" java.lang.ArithmeticException: Le calcul ne converge pas pour les points 89°20,3'W 00°06,6'S et 91°06,2'E 00°06,6'S.
//        at org.geotoolkit.referencing.datum.DefaultEllipsoid.orthodromicDistance(DefaultEllipsoid.java:507)
//        at org.constellation.coverage.catalog.CoverageComparator.getArea(CoverageComparator.java:181)

            throw new IOException(ex);
        } 

        if(coverage != null){
            coverage = (GridCoverage2D) Operations.DEFAULT.resample(coverage, requestCRS);
        }
        
        return coverage;
    }

    @Override
    public Envelope getCoverageBounds() {
        final CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
        final GeographicBoundingBox bbox;
        try {
            table.getLayer().getCoverage().getEnvelope();
            bbox = table.getLayer().getGeographicBoundingBox();
        } catch (CatalogException ex) {
            LOGGER.warning(ex.getLocalizedMessage());
            return new GeneralEnvelope(crs);
        }

        GeneralEnvelope env = new GeneralEnvelope(bbox);
        env.setCoordinateReferenceSystem(crs);
        return env;

        //this returns the correct bounds in the original CRS of the data
        //since we can only query postgrid en CRS:84, we should return the envelope
        //only in crs:84 to be coherent.
//        try {
//            return table.getLayer().getCoverage().getEnvelope();
//        } catch (CatalogException ex) {
//            LOGGER.warning(ex.getLocalizedMessage());
//            return new ReferencedEnvelope(DefaultGeographicCRS.WGS84);
//        }

    }

}
