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
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.constellation.catalog.NoSuchTableException;
import org.constellation.coverage.catalog.GridCoverageTable;
import org.constellation.coverage.catalog.Layer;

import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.ColorMap;
import org.geotools.coverage.processing.Operations;
import org.geotools.display.primitive.GraphicJ2D;
import org.geotools.display.renderer.RenderingContext2D;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.AbstractMapLayer;
import org.geotools.map.DynamicMapLayer;
import org.geotools.map.GraphicBuilder;
import org.geotools.map.MapLayer;
import org.geotools.map.MapLayerBuilder;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.style.MutableStyle;
import org.geotools.style.StyleFactory;

import org.geotools.util.MeasurementRange;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.TransformException;
import org.opengis.style.RasterSymbolizer;


/**
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public class PostGridMapLayer extends AbstractMapLayer implements DynamicMapLayer{
    /**
     * The requested elevation.
     */
    private double elevation;

    /**
     * The envelope of current layer, including its CRS.
     */
    private GeneralEnvelope envelope;

    /**
     * Range for the {@linkplain ColorMap color map} to apply on the
     * {@linkplain GridCoverage2D grid coverage}.
     */
    private MeasurementRange dimRange;

    /**
     * List of available dates for a request.
     */
    private final List<Date> times;

    /**
     * Connection to a PostGRID database.
     */
    private final Database db;

    /**
     * Layer to consider.
     */
    private final Layer layer;

    /**
     * Builds a map layer, using a database connection and a layer.
     *
     * @param db The database connection.
     * @param layer The current layer.
     */
    public PostGridMapLayer(final Database db, final Layer layer){
        super(createDefaultRasterStyle());
        this.db = db;
        this.layer = layer;
        this.times = new ArrayList<Date>();
        setName(layer.getName());
    }

    public Object prepare(ReferencedEnvelope env, int width, int height,
            MathTransform objToDisp, CoordinateReferenceSystem displayCRS,
            Object extent) {
        return query(env, width, height,objToDisp,displayCRS,extent);
    }

    public Object query(ReferencedEnvelope env, int width, int height,
            MathTransform objToDisp, CoordinateReferenceSystem displayCRS,
            Object extent) {

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
        table.setVerticalRange(elevation, elevation);
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

        final BufferedImage buffer = new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB);
        
        if(coverage == null){
            return buffer;
        }
        
        // Here a resample is done, to get the coverage into the requested crs.
        coverage = (GridCoverage2D) Operations.DEFAULT.resample(coverage, env.getCoordinateReferenceSystem());
        final RenderedImage img = coverage.getRenderableImage(0, 1).createDefaultRendering();
        final Graphics2D g2 = buffer.createGraphics();
        
        
        //---------------GRAPHIC BUILDER----------------------------------------
        
        final GraphicBuilder<? extends GraphicJ2D> builder = getGraphicBuilder(GraphicJ2D.class);

        if(builder != null && extent instanceof RenderingContext2D){
            //TODO Find a better way to solve this issue
            final MapLayerBuilder mapBuilder = new MapLayerBuilder();
            final MapLayer coverageLayer = mapBuilder.create(coverage, getStyle(), "name");
            RenderingContext2D context2D = (RenderingContext2D) extent;
            //special graphic builder
            Collection<? extends GraphicJ2D> graphics = builder.createGraphics(coverageLayer, context2D.getCanvas());
            g2.setClip(context2D.getGraphics().getClip());
            context2D = context2D.create(g2);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for(GraphicJ2D gra : graphics){
                try {
                    gra.paint(context2D);
                } catch (TransformException ex) {
                    Logger.getLogger(PostGridMapLayer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(PostGridMapLayer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            g2.dispose();
            return buffer;
        }
        
        //----------------------------------------------------------------------

        // use the provided dim range
        if (dimRange != null) {
            final GridSampleDimension[] samples = coverage.getSampleDimensions();
            if (samples != null && samples.length == 1 && samples[0] != null) {
                if (samples[0].getSampleToGeophysics() != null) {
                    final ColorMap colorMap = new ColorMap();
                    colorMap.setGeophysicsRange(ColorMap.ANY_QUANTITATIVE_CATEGORY, dimRange);
                    coverage = (GridCoverage2D) Operations.DEFAULT.recolor(coverage, new ColorMap[]{colorMap});
                    coverage = coverage.geophysics(false);
                }
            }
        }
        
        //normal image rendering
        final AffineTransform trs = g2.getTransform();
        trs.concatenate((AffineTransform)objToDisp);
        g2.setTransform(trs);

        final MathTransform2D mathTrans2D = coverage.getGridGeometry().getGridToCRS2D();
        if (mathTrans2D instanceof AffineTransform) {
            final AffineTransform transform = (AffineTransform) mathTrans2D;
            g2.drawRenderedImage(img, transform);
        } else {
            Logger.getLogger(PostGridMapLayer.class.getName()).log(Level.SEVERE, null, new IllegalArgumentException());
        }
        g2.dispose();

        return buffer;
    }

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
     * Returns the maximum range for the color map to apply.
     */
    public MeasurementRange getDimRange() {
        return dimRange;
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

    /**
     * Fixes the range to apply for the color map.
     */
    public void setDimRange(final MeasurementRange dimRange) {
        this.dimRange = dimRange;
    }

    /**
     * Fixes the elevation to request.
     */
    public void setElevation(final double elevation) {
        this.elevation = elevation;
    }

    /**
     * Returns a modifiable list of dates.
     */
    public List<Date> times() {
        return times;
    }

}