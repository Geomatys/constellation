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

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.jai.Interpolation;
import org.constellation.catalog.CatalogException;
import org.constellation.coverage.catalog.Layer;

import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GeneralGridEnvelope;
import org.geotools.coverage.grid.GeneralGridGeometry;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.io.CoverageReadParam;
import org.geotools.coverage.processing.ColorMap;
import org.geotools.coverage.processing.CoverageProcessingException;
import org.geotools.coverage.processing.Operations;
import org.geotools.display.exception.PortrayalException;
import org.geotools.display.primitive.GraphicJ2D;
import org.geotools.display.renderer.RenderingContext;
import org.geotools.display.renderer.RenderingContext2D;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.AbstractMapLayer;
import org.geotools.map.DynamicMapLayer;
import org.geotools.map.GraphicBuilder;
import org.geotools.map.MapLayer;
import org.geotools.map.MapBuilder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.style.MutableStyle;
import org.geotools.style.StyleFactory;
import org.geotools.util.MeasurementRange;

import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.PixelInCell;
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
 *
 * @deprecated
 */
public class PostGridMapLayer extends AbstractMapLayer implements DynamicMapLayer {
    /**
     * Default logger.
     */
    private static final Logger LOGGER = Logger.getLogger("org.constellation.provider.postgrid");

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

//    /**
//     * Connection to a PostGRID database.
//     */
//    private final Database db;
//
//    /**
//     * Layer to consider.
//     */
//    private final Layer layer;

//    /**
//     * grid coverage table.
//     */
//    private final GridCoverageTable table;

    /**
     * postgrid reader
     */
    private final PostGridReader reader;
    
    /**
     * Builds a map layer, using a database connection and a layer.
     *
     * @param db The database connection.
     * @param layer The current layer.
     */
    public PostGridMapLayer(final PostGridReader reader) {
        super(createDefaultRasterStyle());
//        this.db = db;
//        this.layer = layer;
        this.times = new ArrayList<Date>();
        setName(reader.getTable().getLayer().getName());
        
//        GridCoverageTable immutableTable = null;
//        try {
//            immutableTable = db.getTable(GridCoverageTable.class);
//        } catch (NoSuchTableException ex) {
//            LOGGER.log(Level.SEVERE, "No GridCoverageTable", ex);
//        }
//        this.table = new GridCoverageTable(immutableTable);
//        this.table.setLayer(layer);
        this.reader = reader;
    }

    public Object prepare(final RenderingContext context) throws PortrayalException {
        return query(context);
    }

    public Object query(final RenderingContext context) throws PortrayalException {
        final ReferencedEnvelope env;
        CoordinateReferenceSystem requestCRS;
        final int width;
        final int height;
        final MathTransform objToDisp;

        if(context instanceof RenderingContext2D) {
            final RenderingContext2D context2D = (RenderingContext2D) context;
            final Rectangle2D shape = context2D.getCanvasObjectiveShape().getBounds2D();
            final Rectangle rect = context2D.getCanvasDisplayBounds();
            env = new ReferencedEnvelope(shape, context2D.getObjectiveCRS());
            requestCRS = context2D.getObjectiveCRS();
            width = rect.width;
            height = rect.height;
            objToDisp = context2D.getCanvas().getObjectiveToDisplayTransform();
        } else {
            throw new PortrayalException("PostGrid layer only support rendering for RenderingContext2D");
        }

        final Envelope genv;
        try {
            genv = CRS.transform(env, DefaultGeographicCRS.WGS84);
        } catch (TransformException ex) {
            throw new PortrayalException(ex);
        }
        final GeneralEnvelope renv = new GeneralEnvelope(genv);

        //Create resolution-----------------------------------------------------
        final double w = renv.toRectangle2D().getWidth() /width;
        final double h = renv.toRectangle2D().getHeight() /height;

//        table.setTimeRange(getTime(), getTime());
//        table.setVerticalRange(elevation, elevation);

        final CoverageReadParam readParam = new CoverageReadParam(renv, new double[]{w,h});

        GridCoverage2D coverage = null;
        try{
            coverage = reader.read(readParam,elevation,getTime(),null);
        }catch(Exception ex){
            throw new PortrayalException(ex);
        }

        final BufferedImage buffer = new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB);

        if(coverage == null) return buffer;

        int coverageID = -1;
        int requestID = -1;
        try{
            final Integer crs1 = CRS.lookupEpsgCode(coverage.getCoordinateReferenceSystem2D(),false);
            final Integer crs2 = CRS.lookupEpsgCode(requestCRS,false);
            if(crs1 != null) coverageID = crs1;
            if(crs2 != null) requestID = crs2;
        }catch(FactoryException ex){
            LOGGER.log(Level.WARNING,"",ex);
        }
                
        // Here a resample is done, to get the coverage into the requested crs.
        if( !CRS.equalsIgnoreMetadata(coverage.getCoordinateReferenceSystem2D(), requestCRS)
            || coverageID != requestID){
            
//            System.out.println("Different CRS, reprojecting coverage, coverage is :\n" + coverage.getCoordinateReferenceSystem2D()
//                    + " \n while request CRS is :\n" + requestCRS);
            
            if (coverage.getDimension() == env.getDimension()) {
                //same dimension number
                try {
                    coverage = (GridCoverage2D) Operations.DEFAULT.resample(coverage, requestCRS);
                } catch (CoverageProcessingException c) {
                    throw new PortrayalException(c);
                }
                
            }else{
                //different number of dimensions
            

            //FIRST TECHNIC USING a NEW CRS-------------------------------------
//            final CoordinateReferenceSystem crscov = coverage.getCoordinateReferenceSystem();
//            if(crscov instanceof CompoundCRS){
//                System.out.println("IS COUMPOUND");
//                final CompoundCRS comp = (CompoundCRS) crscov;
//                final List<CoordinateReferenceSystem> crss = comp.getCoordinateReferenceSystems();
//                final CoordinateReferenceSystem[] group = new CoordinateReferenceSystem[crss.size()];
//                group[0] = requestCRS;
//                for(int i=1,n=group.length; i<n; i++){
//                    group[i] = crss.get(i);
//                }
//                requestCRS = new DefaultCompoundCRS("Extended CRS", group);
//            }else{
//                System.out.println("IS NOT COMPOUND");
//            }
//            try {
//                coverage = (GridCoverage2D) Operations.DEFAULT.resample(coverage, requestCRS);
//            } catch (CoverageProcessingException c) {
//                throw new PortrayalException(c);
//            }
            
            // SECOND TECHNIC USING A NEW GRID TO CRS---------------------------
                try {
                    final GeneralEnvelope general = new GeneralEnvelope(env);
                    general.setCoordinateReferenceSystem(env.getCoordinateReferenceSystem());
                    final GridEnvelope range = new GeneralGridEnvelope(new Rectangle(width, height), 2);
                    final GridGeometry gridGeom = getGridGeometry(
                                new GeneralEnvelope(env), 
                                reader.getTable().getLayer(),
                                range);

                    coverage = (GridCoverage2D) Operations.DEFAULT.resample(
                            coverage, 
                            requestCRS,
                            gridGeom,
                            Interpolation.getInstance(Interpolation.INTERP_NEAREST));

                } catch (CoverageProcessingException c) {
                    throw new PortrayalException(c);
                } catch (CatalogException c) {
                    throw new PortrayalException(c);
                }
            }
            
        }
        final Graphics2D g2 = buffer.createGraphics();


        //---------------GRAPHIC BUILDER----------------------------------------

        final GraphicBuilder<? extends GraphicJ2D> builder = getGraphicBuilder(GraphicJ2D.class);

        if(builder != null ){
            //TODO Find a better way to solve this issue
            final MapLayer coverageLayer = MapBuilder.createCoverageLayer(coverage, getStyle(), "name");
            RenderingContext2D context2D = (RenderingContext2D) context;
            //special graphic builder
            final Collection<? extends GraphicJ2D> graphics = builder.createGraphics(coverageLayer, context2D.getCanvas());
            g2.setClip(context2D.getGraphics().getClip());
            context2D = context2D.create(g2);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for(GraphicJ2D gra : graphics){
                gra.paint(context2D);
            }

            g2.dispose();
            return buffer;
        }

        // use the provided dim range
        if (dimRange != null) {
            final GridSampleDimension[] samples = coverage.getSampleDimensions();
            if (samples != null && samples.length == 1 && samples[0] != null) {
                if (samples[0].getSampleToGeophysics() != null) {
                    final ColorMap colorMap = new ColorMap();
                    colorMap.setGeophysicsRange(ColorMap.ANY_QUANTITATIVE_CATEGORY, dimRange);
                    try {
                        coverage = (GridCoverage2D) Operations.DEFAULT.recolor(coverage, new ColorMap[]{colorMap});
                    } catch (CoverageProcessingException c) {
                        throw new PortrayalException(c);
                    }
                }
            }
        }
        coverage = coverage.geophysics(false);
        final RenderedImage img = coverage.getRenderableImage(0, 1).createDefaultRendering();

        //normal image rendering
        final AffineTransform trs = g2.getTransform();
        trs.concatenate((AffineTransform)objToDisp);
        g2.setTransform(trs);

        final MathTransform2D mathTrans2D = coverage.getGridGeometry().getGridToCRS2D();
        if (mathTrans2D instanceof AffineTransform) {
            final AffineTransform transform = (AffineTransform) mathTrans2D;
            g2.drawRenderedImage(img, transform);
        } else {
            throw new PortrayalException("Should have been an affine transform at this state.");
        }
        g2.dispose();

        return buffer;
    }

    @Override
    public void portray(final RenderingContext context) throws PortrayalException {
        
        if( !(context instanceof RenderingContext2D) ){
            throw new PortrayalException("PostGrid layer only support rendering for RenderingContext2D");
        }

        final RenderingContext2D context2D = (RenderingContext2D) context;
        final CoordinateReferenceSystem requestCRS = context2D.getObjectiveCRS();

//        table.setTimeRange(getTime(), getTime());
//        table.setVerticalRange(elevation, elevation);

        final GeneralEnvelope env = new GeneralEnvelope(context2D.getCanvasObjectiveBounds());
        env.setCoordinateReferenceSystem(context2D.getObjectiveCRS());
        final double[] resolution = context2D.getResolution();
        final CoverageReadParam readParam = new CoverageReadParam(env, resolution);
        
        GridCoverage2D coverage = null;
        try{
            coverage = reader.read(readParam,elevation,getTime(),null);
        }catch(Exception ex){
            throw new PortrayalException(ex);
        }


        if(coverage == null) return;

        int coverageID = -1;
        int requestID = -1;
        try{
            final Integer crs1 = CRS.lookupEpsgCode(coverage.getCoordinateReferenceSystem2D(),false);
            final Integer crs2 = CRS.lookupEpsgCode(requestCRS,false);
            if(crs1 != null) coverageID = crs1;
            if(crs2 != null) requestID = crs2;
        }catch(FactoryException ex){
            LOGGER.log(Level.WARNING,"",ex);
        }
                
        // Here a resample is done, to get the coverage into the requested crs.
        if( !CRS.equalsIgnoreMetadata(coverage.getCoordinateReferenceSystem2D(), requestCRS)
            || coverageID != requestID){
            
//            System.out.println("Different CRS, reprojecting coverage, coverage is :\n" + coverage.getCoordinateReferenceSystem2D()
//                    + " \n while request CRS is :\n" + requestCRS);
            
            if (coverage.getDimension() == env.getDimension()) {
                //same dimension number
                try {
                    coverage = (GridCoverage2D) Operations.DEFAULT.resample(coverage, requestCRS);
                } catch (CoverageProcessingException c) {
                    throw new PortrayalException(c);
                }
                
            }else{
                //different number of dimensions
            

            //FIRST TECHNIC USING a NEW CRS-------------------------------------
//            final CoordinateReferenceSystem crscov = coverage.getCoordinateReferenceSystem();
//            if(crscov instanceof CompoundCRS){
//                System.out.println("IS COUMPOUND");
//                final CompoundCRS comp = (CompoundCRS) crscov;
//                final List<CoordinateReferenceSystem> crss = comp.getCoordinateReferenceSystems();
//                final CoordinateReferenceSystem[] group = new CoordinateReferenceSystem[crss.size()];
//                group[0] = requestCRS;
//                for(int i=1,n=group.length; i<n; i++){
//                    group[i] = crss.get(i);
//                }
//                requestCRS = new DefaultCompoundCRS("Extended CRS", group);
//            }else{
//                System.out.println("IS NOT COMPOUND");
//            }
//            try {
//                coverage = (GridCoverage2D) Operations.DEFAULT.resample(coverage, requestCRS);
//            } catch (CoverageProcessingException c) {
//                throw new PortrayalException(c);
//            }
            
            // SECOND TECHNIC USING A NEW GRID TO CRS---------------------------
                try {
                    final GeneralEnvelope general = new GeneralEnvelope(env);
                    general.setCoordinateReferenceSystem(env.getCoordinateReferenceSystem());
                    final GridEnvelope range = new GeneralGridEnvelope(context2D.getCanvasDisplayBounds(), 2);
                    final GridGeometry gridGeom = getGridGeometry(
                                new GeneralEnvelope(env), 
                                reader.getTable().getLayer(),
                                range);

                    coverage = (GridCoverage2D) Operations.DEFAULT.resample(
                            coverage, 
                            requestCRS,
                            gridGeom,
                            Interpolation.getInstance(Interpolation.INTERP_NEAREST));

                } catch (CoverageProcessingException c) {
                    throw new PortrayalException(c);
                } catch (CatalogException c) {
                    throw new PortrayalException(c);
                }
            }
            
        }
        final Graphics2D g2 = context2D.getGraphics();

        try {
            context2D.setGraphicsCRS(context2D.getDisplayCRS());
        } catch (TransformException ex) {
            throw new PortrayalException(ex);
        }

        //---------------GRAPHIC BUILDER----------------------------------------

        final GraphicBuilder<? extends GraphicJ2D> builder = getGraphicBuilder(GraphicJ2D.class);

        if(builder != null ){
            //TODO Find a better way to solve this issue
            final MapLayer coverageLayer = MapBuilder.createCoverageLayer(coverage, getStyle(), "name");
            //special graphic builder
            final Collection<? extends GraphicJ2D> graphics = builder.createGraphics(coverageLayer, context2D.getCanvas());
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for(GraphicJ2D gra : graphics){
                gra.paint(context2D);
            }
            return;
        }

        // use the provided dim range
        final RenderedImage img;
        if (dimRange != null) {
            final GridSampleDimension[] samples = coverage.getSampleDimensions();
            if (samples != null && samples.length == 1 && samples[0] != null) {
                if (samples[0].getSampleToGeophysics() != null) {
                    final ColorMap colorMap = new ColorMap();
                    colorMap.setGeophysicsRange(ColorMap.ANY_QUANTITATIVE_CATEGORY, dimRange);
                    try {
                        coverage = (GridCoverage2D) Operations.DEFAULT.recolor(coverage, new ColorMap[]{colorMap});
                    } catch (CoverageProcessingException c) {
                        throw new PortrayalException(c);
                    }
                }
            }
            coverage = coverage.geophysics(false);
            img = coverage.getRenderableImage(0, 1).createDefaultRendering();
            
            try {
                context2D.setGraphicsCRS(context2D.getObjectiveCRS());
            } catch (TransformException ex) {
                throw new PortrayalException(ex);
            }

            final MathTransform2D mathTrans2D = coverage.getGridGeometry().getGridToCRS2D();
            if (mathTrans2D instanceof AffineTransform) {
                final AffineTransform transform = (AffineTransform) mathTrans2D;
                g2.drawRenderedImage(img, transform);
            } else {
                throw new PortrayalException("Should have been an affine transform at this state.");
            }
            
        }else{
            //normal portraying should replace dim range once ready
            normalProtray(context2D, coverage);
        }
        
    }
    
    public ReferencedEnvelope getBounds() {
        final CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
        final GeographicBoundingBox bbox;
        try {
            bbox = reader.getTable().getLayer().getGeographicBoundingBox();
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

    /**
     * Computes the grid geometry. Returns {@code null} if the geometry can not be computed.
     * The geometry CRS is the one returned by {@link #getCoordinateReferenceSystem()},
     * not the {@linkplain #getResponseCRS response CRS}.
     *
     * @param envelope : query envelope
     * @return The grid geometry, or {@code null}.
     * @throws CatalogException if an error occurred while querying the layer.
     */
    private GeneralGridGeometry getGridGeometry(GeneralEnvelope envelope, final Layer layer, GridEnvelope gridRange) throws CatalogException{
        GeneralGridGeometry gridGeometry = null;
        MathTransform gridToCRS = null;
        
        if (envelope == null || gridRange == null) {
            if (gridRange == null) {
                final Rectangle bounds = layer.getTypicalBounds();
                if (bounds != null) {
                    gridRange = new GeneralGridEnvelope(bounds, 2);
                }
            }
            if (envelope == null) {
                final GeographicBoundingBox box = layer.getGeographicBoundingBox();
                if (box != null) {
                    envelope = new GeneralEnvelope(box);
                }
            }

        }
        // We know that gridToCRS is null, but we try to select constructors that accept
        // null arguments. If we are wrong, an IllegalArgumentException will be thrown.
        if (envelope == null || envelope.isInfinite()) {
            gridGeometry = new GeneralGridGeometry(gridRange, gridToCRS, getCoordinateReferenceSystem());
        } else if (gridRange != null) {
            gridGeometry = new GeneralGridGeometry(gridRange, envelope);
        } else {
            gridGeometry = new GeneralGridGeometry(PixelInCell.CELL_CENTER, gridToCRS, envelope);
        }
        
        return gridGeometry;
    }
    
    private void normalProtray(RenderingContext2D renderingContext, GridCoverage2D coverage) throws PortrayalException{
//        final Name coverageName = new NameImpl(getName());
//        final List<CachedRule> rules = J2DGraphicUtilities.getValidRules(getStyle(), renderingContext.getScale(), coverageName);
//
//        //we perform a first check on the style to see if there is at least
//        //one valid rule at this scale, if not we just continue.
//        if (rules.isEmpty()) {
//            return;   //----------------------------------------------------->CONTINUE
//        }
//
//        if(coverage != null){
//            final CoordinateReferenceSystem dataCRS = coverage.getCoordinateReferenceSystem();
//            final CoordinateReferenceSystem displayCRS = renderingContext.getDisplayCRS();
//            final CoordinateReferenceSystem objectiveCRS = renderingContext.getObjectiveCRS();
//            final J2DGraphicUtilities J2Dtool = J2DGraphicUtilities.getInstance();
//
//            J2Dtool.prepare(1,dataCRS,displayCRS,objectiveCRS,renderingContext.getCanvas().getObjectiveToDisplayTransform());
//
//            for(final CachedRule rule : rules){
////                final Filter filter = rule.getFilter();
//                //test if the rule is valid for this feature
////                if(filter == null  || filter.evaluate(feature)){
//                    final List<CachedSymbolizer> symbols = rule.symbolizers();
//
//                    for(final CachedSymbolizer symbol : symbols){
//                        if(symbol instanceof CachedRasterSymbolizer){
//                            J2Dtool.portray(coverage,null, (CachedRasterSymbolizer)symbol, renderingContext);
//                        }
//                    }
//
////                }
//            }
//
//        }
            
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

    private static final MutableStyle createDefaultRasterStyle() {
        final StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
        final RasterSymbolizer symbol =sf.rasterSymbolizer();
        return sf.style(symbol);
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
