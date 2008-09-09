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

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.constellation.catalog.NoSuchTableException;
import org.constellation.coverage.catalog.GridCoverageTable;
import org.constellation.coverage.catalog.Layer;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.DynamicMapLayer;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.style.MutableStyle;
import org.geotools.style.StyleFactory;

import org.opengis.coverage.grid.GridRange;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.style.RasterSymbolizer;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class PostGridMapLayer extends DefaultMapLayer implements DynamicMapLayer{
        
    /**
     * The <cite>grid to CRS</cite> transform specified by the user.
     */
    protected MathTransform gridToCRS;
    
    /**
     * The requested elevation.
     */
    protected Number elevation;
    
    /**
     * The envelope of current layer, including its CRS.
     */
    protected GeneralEnvelope envelope;

    /**
     * The dimension of target image.
     */
    protected GridRange gridRange;
    
    private final Database db;
    private final Layer layer;
    private final List<Date> times;
    
    
    public PostGridMapLayer(Database db, Layer layer){
        super(createDefaultRasterStyle());
        this.db = db;
        this.layer = layer;
        this.times = new ArrayList<Date>();
    }
    
    public Object prepare(ReferencedEnvelope env, int width, int height, 
            MathTransform objToDisp, CoordinateReferenceSystem displayCRS) {
        return query(env, width, height,objToDisp,displayCRS);
    }

    public Object query(ReferencedEnvelope env, int width, int height, 
            MathTransform objToDisp, CoordinateReferenceSystem displayCRS) {
        
        Envelope genv = null;
        try {
            genv = CRS.transform(env, DefaultGeographicCRS.WGS84);
        } catch (TransformException ex) {
            Logger.getLogger(PostGridMapLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        GeneralEnvelope renv = new GeneralEnvelope(genv);
        
        //Create BBOX-----------------------------------------------------------
        GeographicBoundingBox bbox = null;
        try {
            bbox = new GeographicBoundingBoxImpl(renv);
        } catch (TransformException ex) {
            Logger.getLogger(PostGridMapLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Create resolution-----------------------------------------------------
        double w = renv.toRectangle2D().getWidth() /width;
        double h = renv.toRectangle2D().getHeight() /height;
        Dimension2D resolution = new org.geotools.resources.geometry.XDimension2D.Double(w, h);
        
        
        GridCoverageTable table = null;
        try {
            table = db.getTable(GridCoverageTable.class);
        } catch (NoSuchTableException ex) {
            Logger.getLogger(PostGridMapLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        table = new GridCoverageTable(table);
        
        table.setGeographicBoundingBox(bbox);
        table.setPreferredResolution(resolution);
        table.setTimeRange(getTime(), getTime());
        table.setLayer(layer);
        
        GridCoverage2D coverage = null;
        try {
            coverage = table.getEntry().getCoverage(null);
        } catch (CatalogException ex) {
            Logger.getLogger(PostGridMapLayer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(PostGridMapLayer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PostGridMapLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        //TODO RETURN a RenderedImage, but doesnt work
//        coverage = (GridCoverage2D) Operations.DEFAULT.resample(coverage, displayCRS);
//        return coverage.getRenderedImage();
        
        
        final BufferedImage buffer = new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB);
        RenderedImage img = coverage.getRenderableImage(0, 1).createDefaultRendering();
        
        
        AffineTransform dataToObj = null;
        try {
            dataToObj = (AffineTransform) CRS.findMathTransform(env.getCoordinateReferenceSystem(), coverage.getCoordinateReferenceSystem());
        } catch (FactoryException ex) {
            Logger.getLogger(PostGridMapLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Graphics2D g2 = buffer.createGraphics();
        AffineTransform trs = g2.getTransform();
        trs.concatenate((AffineTransform)objToDisp);
        trs.concatenate(dataToObj);
        g2.setTransform(trs);
        
        AffineTransform transform = (AffineTransform)coverage.getGridGeometry().getGridToCRS();
        g2.drawRenderedImage(img, transform);
        g2.dispose();
        
        
        return buffer;
    }

    @Override
    public ReferencedEnvelope getBounds() {
        CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
        GeographicBoundingBox bbox = null;
        try {
            bbox = layer.getGeographicBoundingBox();
        } catch (CatalogException ex) {
            Logger.getLogger(PostGridMapLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(bbox != null){
            return new ReferencedEnvelope(bbox.getWestBoundLongitude(), 
                                    bbox.getEastBoundLongitude(), 
                                    bbox.getSouthBoundLatitude(), 
                                    bbox.getNorthBoundLatitude(), 
                                    crs);
        }else{
         return new ReferencedEnvelope(crs);
        }
        
    }

    /**
     * Returns a single time from the {@linkplain #times} list, or {@code null} if none.
     * If there is more than one time, select the last one on the basis that it is typically
     * the most recent one.
     */
    private Date getTime() {
        return times.isEmpty() ? null : times.get(times.size() - 1);
    }
    
    /**
     * Returns the coordinate reference system, or {@code null} if unknown.
     *
     * @return The current CRS for queries, or {@code null}.
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return (envelope != null) ? envelope.getCoordinateReferenceSystem() : null;
    }
        
    private static final MutableStyle createDefaultRasterStyle(){
        StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
        RasterSymbolizer symbol =sf.createDefaultRasterSymbolizer();
        return sf.createRasterStyle(symbol);
    }
    
}