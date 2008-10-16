/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.provider.postgrid;

import java.awt.geom.Dimension2D;
import java.io.IOException;
import java.sql.SQLException;

import org.constellation.catalog.CatalogException;
import org.constellation.coverage.catalog.GridCoverageTable;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.io.CoverageReadParam;
import org.geotools.coverage.io.CoverageReader;
import org.geotools.coverage.processing.Operations;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.resources.geometry.XDimension2D;

import org.opengis.geometry.DirectPosition;
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
    
    private GridCoverageTable table;
        
    private final ReferencedEnvelope ref;
    
    public PostGridReader(final GridCoverageTable table, final ReferencedEnvelope bounds){
        this.table = table;
        this.ref = bounds;
    }
    
    public GridCoverage2D read(final CoverageReadParam param) throws FactoryException, TransformException, IOException {
        
        if(param == null){
            
            //no parameters, return the complete image
            GridCoverage2D coverage = null;
            try {
                coverage = table.getEntry().getCoverage(null);
            } catch (CatalogException ex) {
                throw new IOException(ex);
            } catch (SQLException ex) {
                throw new IOException(ex);
            } catch (IOException ex) {
                throw new IOException(ex);
            } finally{
                table.flush();
            }
            
            return coverage;
        }
        
        GeneralEnvelope requestEnvelope = param.getEnveloppe();
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
                    requestEnvelope.getCoordinateReferenceSystem(), crs);
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
            coverage = table.getEntry().getCoverage(null);
        } catch (CatalogException ex) {
            throw new IOException(ex);
        } catch (SQLException ex) {
            throw new IOException(ex);
        } catch (IOException ex) {
            throw new IOException(ex);
        } finally{
            table.flush();
        }
        
        if(coverage != null){
            coverage = (GridCoverage2D) Operations.DEFAULT.resample(coverage, requestCRS);
        }
        return coverage;
    }

    public ReferencedEnvelope getCoverageBounds() {
        return ref;
    }

}
