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
package net.seagis.provider.postgrid;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.jai.Interpolation;
import net.seagis.catalog.CatalogException;
import net.seagis.catalog.Database;
import net.seagis.coverage.catalog.CoverageReference;
import net.seagis.coverage.catalog.Layer;
import org.geotools.coverage.grid.GeneralGridGeometry;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.Operations;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.display.style.J2DGraphicUtilities;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.DynamicMapLayer;
import org.geotools.map.GraphicBuilder;
import org.geotools.map.LayerListener;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.style.MutableStyle;
import org.geotools.style.StyleFactory;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.coverage.grid.GridRange;
import org.opengis.display.primitive.Graphic;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.style.Description;
import org.opengis.style.RasterSymbolizer;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class PostGridMapLayer extends DefaultMapLayer implements DynamicMapLayer{

    private final J2DGraphicUtilities painter = new J2DGraphicUtilities();
    
    /**
     * The default interpolation to use.
     */
    static final int DEFAULT_INTERPOLATION = Interpolation.INTERP_NEAREST;
    
    /**
     * The interpolation to use for resampling.
     */
    protected Interpolation interpolation = Interpolation.getInstance(DEFAULT_INTERPOLATION);
    
    /**
     * The <cite>grid to CRS</cite> transform specified by the user.
     */
    protected MathTransform gridToCRS;
    
    /**
     * The requested elevation.
     */
    protected Number elevation;
    
    /**
     * The grid geometry. This field is computed automatically from other user-supplied values
     * like {@link #gridCRS}, {@link #gridRange} and {@link #envelope}.
     *
     * @see #getGridGeometry
     */
    private transient GeneralGridGeometry gridGeometry;

    /**
     * The envelope of current layer, including its CRS.
     */
    protected GeneralEnvelope envelope;

    /**
     * The CRS of the response, or {@code null} if not specified.
     */
    protected CoordinateReferenceSystem responseCRS;

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
    
    public Object prepare(ReferencedEnvelope env, int width, int height) {
        BufferedImage buffer = new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB);
        envelope = new GeneralEnvelope(env);
        GridCoverage2D coverage = null;
        try {
            coverage = getGridCoverage2D(true);
        } catch (CatalogException ex) {
            ex.printStackTrace();
            Logger.getLogger(PostGridMapLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        Graphics2D g2d = buffer.createGraphics();
        g2d.drawRenderedImage(coverage.getRenderedImage(), null);
        g2d.dispose();
        
        return buffer;
    }

    public BufferedImage query(ReferencedEnvelope env, int width, int height) {
        BufferedImage buffer = new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB);
        envelope = new GeneralEnvelope(env);
        GridCoverage2D coverage = null;
        try {
            coverage = getGridCoverage2D(true);
        } catch (CatalogException ex) {
            ex.printStackTrace();
            Logger.getLogger(PostGridMapLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        Graphics2D g2d = buffer.createGraphics();
        g2d.drawRenderedImage(coverage.getRenderedImage(), null);
        g2d.dispose();
        
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
    
    
    /**
     * Returns the response CRS, or {@code null} if unknown.
     *
     * @return The current CRS for responses, or {@code null}.
     */
    public CoordinateReferenceSystem getResponseCRS() {
        return (responseCRS != null) ? responseCRS : getCoordinateReferenceSystem();
    }
    
    
    /**
     * Computes the grid geometry. Returns {@code null} if the geometry can not be computed.
     * The geometry CRS is the one returned by {@link #getCoordinateReferenceSystem()},
     * not the {@linkplain #getResponseCRS response CRS}.
     *
     * @return The grid geometry, or {@code null}.
     * @throws WebServiceException if an error occured while querying the layer.
     */
    public GeneralGridGeometry getGridGeometry() throws CatalogException{
        if (gridGeometry == null) {
            if (gridToCRS != null) {
                if (envelope == null || envelope.isInfinite()) {
                    final CoordinateReferenceSystem crs = getCoordinateReferenceSystem();
                    gridGeometry = new GeneralGridGeometry(gridRange, gridToCRS, crs);
                } else {
                    gridGeometry = new GeneralGridGeometry(PixelInCell.CELL_CENTER, gridToCRS, envelope);
                }
            } else {
                GeneralEnvelope envelope = this.envelope;
                GridRange gridRange = this.gridRange;
                if (envelope == null || gridRange == null) {
                    if (gridRange == null) {
                        final Rectangle bounds = layer.getTypicalBounds();
                        if (bounds != null) {
                            gridRange = new GeneralGridRange(bounds);
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
            }
        }
        return gridGeometry;
    }
    
    
    /**
     * Gets the grid coverage for the current layer, time, elevation, <cite>etc.</cite>
     *
     * @param  resample {@code true} for resampling the coverage to the specified envelope
     *         and dimension, or {@code false} for getting the coverage as in the database.
     * @return The coverage for the requested time and elevation.
     * @throws WebServiceException if an error occured while querying the coverage.
     */
    public GridCoverage2D getGridCoverage2D(final boolean resample) throws CatalogException{
        final CoverageReference ref;
        if (times.isEmpty()) {
            /*
             * If the WMS request does not include a TIME parameter, then use the latest time available.
             *
             * NOTE! - this gets the time of the LAYER's latest entry, but not necessarily within
             *         the requested bounding box.
             * TODO: This fix should probably be incorporated as part of the CoverageComparator,
             *       the but this quick hack keeps the Comparator from looking at ALL coverages
             *       in layer when no time parameter is given.
             */
            try {
                final SortedSet<Date> availableTimes = layer.getAvailableTimes();
                if (availableTimes != null && !availableTimes.isEmpty()) {
                    times.addAll(availableTimes);
                }
            } catch (CatalogException ex) {
                ex.printStackTrace();
                // 'time' still null, which is a legal value.
            }
        }
        
        ref = layer.getCoverageReference(getTime(), elevation);
        
        if (ref == null) {
            // TODO: provides a better message.
            //throw error
            throw new IllegalArgumentException("Coverage Reference is null, should not happen");
        }
        GridCoverage2D coverage;
        try {
            coverage = ref.getCoverage(null);
        } catch (IOException exception) {
            Object file = ref.getFile();
            if (file == null) {
                file = ref.getName();
            }
            throw new IllegalArgumentException();
        }
        if (resample) {
            final GridGeometry gridGeometry = getGridGeometry();
            if (gridGeometry != null) {
                final Operations op = Operations.DEFAULT;
                final CoordinateReferenceSystem targetCRS = getResponseCRS();
                coverage = (GridCoverage2D) op.resample(coverage, targetCRS, gridGeometry, interpolation);
            }
        }
        return coverage;
    }
    
//    /**
//     * Gets the image for the {@linkplain #getGridCoverage2D current coverage}.
//     * The image is resized to the requested dimension and CRS.
//     *
//     * @return The rendered image for the requested time and elevation.
//     * @throws WebServiceException if an error occured while querying the coverage.
//     */
//    public RenderedImage getRenderedImage(){
//        GridCoverage2D coverage = getGridCoverage2D(true);
//        final Service service = version.getService();
//        if (service != null) {
//            switch (service) {
//                case WMS: coverage = coverage.view(ViewType.RENDERED);   break;
//                case WCS: coverage = coverage.view(ViewType.GEOPHYSICS); break;
//            }
//        }
//        if (colormapRange != null) {
//            final ColorMap colorMap = new ColorMap();
//            colorMap.setGeophysicsRange(ColorMap.ANY_QUANTITATIVE_CATEGORY, new MeasurementRange<Double>(colormapRange, null));
//            coverage = (GridCoverage2D) Operations.DEFAULT.recolor(coverage, new ColorMap[] {colorMap});
//        }
//        RenderedImage image = coverage.getRenderedImage();
//        if (LOGGER.isLoggable(Level.FINE)) {
//            final Vocabulary resources = Vocabulary.getResources(null);
//            LOGGER.fine(resources.getLabel (VocabularyKeys.IMAGE_SIZE) +
//                        resources.getString(VocabularyKeys.IMAGE_SIZE_$3,
//                        image.getWidth(), image.getHeight(), image.getSampleModel().getNumBands()));
//        }
//        if (indexedShortAllowed) {
//            return image;
//        }
//        /*
//         * If the image is not geophysics and is indexed on 16 bits, then rescale the image to 8
//         * bits while preserving the colors. TODO: this algorithm is simplist and doesn't consider
//         * the "no data" values, which is a risk of wrong output. We need to find some better way.
//         */
//        ColorModel model = image.getColorModel();
//        if (model instanceof IndexColorModel) {
//            final IndexColorModel icm = (IndexColorModel) model;
//            final int sourceMapSize = icm.getMapSize();
//            final int targetMapSize = 256;
//            if (sourceMapSize > targetMapSize) {
//                final GridSampleDimension dimension = coverage.getSampleDimension(
//                        CoverageUtilities.getVisibleBand(image));
//                if (dimension.geophysics(false) == dimension) {
//                    final NumberRange range = dimension.getRange();
//                    final double scale = (range != null ? range.getMaximum(false) :
//                            dimension.getMaximumValue()) / targetMapSize;
//                    if (scale > 1 && scale < Double.POSITIVE_INFINITY) {
//                        final int[] ARGB = new int[targetMapSize];
//                        for (int i=0; i<targetMapSize; i++) {
//                            final int index = Math.min(sourceMapSize-1, (int) Math.round(i * scale));
//                            ARGB[i] = icm.getRGB(index);
//                        }
//                        int transparent = (int) Math.round(icm.getTransparentPixel() / scale);
//                        if (transparent != (int) Math.round(transparent * scale)) {
//                            transparent = -1;
//                        }
//                        model = new IndexColorModel(8, targetMapSize, ARGB, 0, icm.hasAlpha(), transparent, DataBuffer.TYPE_BYTE);
//                        final ImageLayout layout = new ImageLayout(image);
//                        layout.setColorModel(model);
//                        layout.setSampleModel(model.createCompatibleSampleModel(image.getWidth(), image.getHeight()));
//                        final RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
//                        hints.put(JAI.KEY_REPLACE_INDEX_COLOR_MODEL, Boolean.FALSE);
//                        hints.put(JAI.KEY_TRANSFORM_ON_COLORMAP, Boolean.FALSE);
//                        image = DivideByConstDescriptor.create(image, new double[] {scale}, hints);
//                        assert image.getColorModel() == model;
//                    }
//                }
//            }
//        }
//        return image;
//    }
    
    
    
    
    
    private static final MutableStyle createDefaultRasterStyle(){
        StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
        RasterSymbolizer symbol =sf.createDefaultRasterSymbolizer();
        return sf.createRasterStyle(symbol);
    }
    
}
