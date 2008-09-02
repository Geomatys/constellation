package net.seagis.portrayal;

//import java.awt.Color;
//import java.awt.Dimension;
//import java.io.File;
//import java.io.IOException;
//import java.sql.SQLException;
//import java.text.ParseException;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.SortedSet;
//import java.util.StringTokenizer;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//import net.seagis.catalog.CatalogException;
//import net.seagis.catalog.Database;
//import net.seagis.coverage.catalog.CoverageReference;
//import net.seagis.coverage.catalog.Layer;
//import net.seagis.coverage.catalog.LayerTable;
//import net.seagis.coverage.web.TimeParser;
//
//import org.geotools.coverage.grid.GeneralGridGeometry;
//import org.geotools.coverage.grid.GridCoverage2D;
//import org.geotools.coverage.processing.Operations;
//import org.geotools.display.service.DefaultPortrayalService;
//import org.geotools.factory.CommonFactoryFinder;
//import org.geotools.geometry.GeneralEnvelope;
//import org.geotools.geometry.jts.ReferencedEnvelope;
//import org.geotools.map.DefaultMapContext;
//import org.geotools.map.DefaultMapLayer;
//import org.geotools.map.MapContext;
//import org.geotools.map.MapLayer;
//import org.geotools.referencing.crs.DefaultGeographicCRS;
//
//import org.geotools.style.MutableStyle;
//import org.geotools.style.StyleFactory;
//import org.opengis.coverage.grid.GridCoverage;
//import org.opengis.coverage.grid.GridGeometry;
//import org.opengis.referencing.FactoryException;
//import org.opengis.referencing.crs.CoordinateReferenceSystem;
//import org.opengis.referencing.operation.TransformException;
//import org.opengis.style.Symbolizer;


/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class MapBuilder {

//    private final DefaultPortrayalService service = new DefaultPortrayalService();
//    private final Parser parser = new Parser();
//
//    private final MapContext context = new DefaultMapContext(DefaultGeographicCRS.WGS84);
//    private CoordinateReferenceSystem crs = null;
//    private GeneralEnvelope envelope = null;
//    private String mime = null;
//    private boolean transparant = false;
//    private Color background = Color.WHITE;
//    private final Dimension dimension = new Dimension(1, 1);
//    private double elevation = 0;
//    protected final List<Date> times = new ArrayList<Date>();
//    protected final List<String> layers = new ArrayList<String>();
//
//
//    public void setFormat(String format) {
//        mime = format;
//    }
//
//    public String getFormat(){
//        return mime;
//    }
//
//    public void setCoordinateReferenceSystem(CoordinateReferenceSystem crs){
//        if(crs == null){
//            throw new NullPointerException("CRS can not be null");
//        }
//        this.crs = crs;
//
//        if(envelope != null){
//            envelope.setCoordinateReferenceSystem(crs);
//        }else{
//            envelope = new GeneralEnvelope(crs);
//            envelope.setToInfinite();
//        }
//    }
//
//    public void setCoordinateReferenceSystem(String epsg) {
//        try {
//            setCoordinateReferenceSystem(parser.toCRS(epsg));
//        } catch (FactoryException ex) {
//            Logger.getLogger(MapBuilder.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//
//    public void setLayer(String layer) {
//        layers.clear();
//        StringTokenizer token = new StringTokenizer(layer,",");
//        while(token.hasMoreTokens()){
//            layers.add(token.nextToken());
//        }
//        
//    }
//
//    public void setColormapRange(String dimRange) {
//        //TOTO handle this
//    }
//
//    public void setBoundingBox(GeneralEnvelope bbox){
//        if(bbox == null){
//            throw new NullPointerException("BBox can not be null");
//        }
//        
//        this.envelope = bbox;
//        
//        if(crs != null){
//            envelope.setCoordinateReferenceSystem(crs);
//        }
//    }
//    
//    public void setBoundingBox(String bbox) {
//        setBoundingBox(parser.toBBox(bbox));
//    }
//
//    public void setElevation(double elevation){
//        this.elevation = elevation;
//    }
//    
//    public void setElevation(String elevation) {
//        if(elevation != null){
//            setElevation(parser.toDouble(elevation));
//        }
//    }
//
//    private Date getTime() {
//        return times.isEmpty() ? null : times.get(times.size() - 1);
//    }
//    
//    public void setTime(String date) {
//        times.clear();
//        if (date != null) try {
//            // 'times' will hold the List<Date> that we will use for executing WCS requests across multiple dates.
//            TimeParser.parse(date.trim(), TimeParser.MILLIS_IN_DAY, times);
//        } catch (ParseException exception) {
//            throw new IllegalArgumentException("Invalid date");
//        }
//    }
//
//    public void setDimension(String width, String height, String depth) {
//        int w = parser.toInt("width",width);
//        int h = parser.toInt("height",height);
//        dimension.setSize(w, h);
//    }
//
//    public void setBackgroundColor(Color background) {
//        if(background == null){
//            this.background = Color.WHITE;
//        }else{
//            this.background = background;
//        }
//    }
//    
//    public void setBackgroundColor(String background) {
//        setBackgroundColor(parser.toColor(background));
//    }
//
//    public void setTransparency(boolean trans){
//        this.transparant = trans;
//    }
//    
//    public void setTransparency(String transparent) {
//        if (transparent != null) {
//            transparent = transparent.trim();
//        }
//        setTransparency(Boolean.parseBoolean(transparent));
//    }
//
//    private void prepareContext(){
//        context.clearLayerList();
//        
//        for(String layerName : layers){
//            GridCoverage gc = null;
//            
//            try{
//                gc = createPostGridLayer(layerName);
//            }catch(CatalogException ex){
//                ex.printStackTrace();
//            }catch(SQLException ex){
//                ex.printStackTrace();
//            }catch(IOException ex){
//                ex.printStackTrace();
//            }
//            
//            System.out.println("GC is = " + gc);
//            
//            if(gc != null){
//                MapLayer layer = createPostGridLayer(gc);
//                if(layer != null){
//                    layer.setTitle(layerName);
//                    context.addLayer(layer);
//                }
//            }
//        }
//        
//    }
//    
////    public GeneralGridGeometry getGridGeometry() {
////        if (gridGeometry == null) {
////            if (gridToCRS != null) {
////                if (envelope == null || envelope.isInfinite()) {
////                    final CoordinateReferenceSystem crs = getCoordinateReferenceSystem();
////                    gridGeometry = new GeneralGridGeometry(gridRange, gridToCRS, crs);
////                } else {
////                    gridGeometry = new GeneralGridGeometry(PixelInCell.CELL_CENTER, gridToCRS, envelope);
////                }
////            } else {
////                GeneralEnvelope envelope = this.envelope;
////                GridRange gridRange = this.gridRange;
////                if (envelope == null || gridRange == null) {
////                    final Layer layer = getLayer();
////                    try {
////                        if (gridRange == null) {
////                            final Rectangle bounds = layer.getTypicalBounds();
////                            if (bounds != null) {
////                                gridRange = new GeneralGridRange(bounds);
////                            }
////                        }
////                        if (envelope == null) {
////                            final GeographicBoundingBox box = layer.getGeographicBoundingBox();
////                            if (box != null) {
////                                envelope = new GeneralEnvelope(box);
////                            }
////                        }
////                    } catch (CatalogException exception) {
////                        throw new WMSWebServiceException(exception, NO_APPLICABLE_CODE, version);
////                    }
////                }
////                // We know that gridToCRS is null, but we try to select constructors that accept
////                // null arguments. If we are wrong, an IllegalArgumentException will be thrown.
////                if (envelope == null || envelope.isInfinite()) {
////                    gridGeometry = new GeneralGridGeometry(gridRange, gridToCRS, getCoordinateReferenceSystem());
////                } else if (gridRange != null) {
////                    gridGeometry = new GeneralGridGeometry(gridRange, envelope);
////                } else {
////                    gridGeometry = new GeneralGridGeometry(PixelInCell.CELL_CENTER, gridToCRS, envelope);
////                }
////            }
////        }
////        return gridGeometry;
////    }
//    
//    
////    public GridCoverage2D getGridCoverage2D(final Layer layer, final boolean resample) {
////        final CoverageReference ref;
////        if (times.isEmpty()) {
////            /*
////             * If the WMS request does not include a TIME parameter, then use the latest time available.
////             *
////             * NOTE! - this gets the time of the LAYER's latest entry, but not necessarily within
////             *         the requested bounding box.
////             * TODO: This fix should probably be incorporated as part of the CoverageComparator,
////             *       the but this quick hack keeps the Comparator from looking at ALL coverages
////             *       in layer when no time parameter is given.
////             */
////            try {
////                final SortedSet<Date> availableTimes = layer.getAvailableTimes();
////                if (availableTimes != null && !availableTimes.isEmpty()) {
////                    times.addAll(availableTimes);
////                }
////            } catch (CatalogException ex) {
//////                Logging.unexpectedException(LOGGER, ImageProducer.class, "getGridCoverage2D", ex);
////                // 'time' still null, which is a legal value.
////            }
////        }
////        
////        ref = layer.getCoverageReference(getTime(), elevation);
////        
////        if (ref == null) {
////            // TODO: provides a better message.
////            throw new NullPointerException("layer coverage reference is null");
////        }
////        GridCoverage2D coverage;
////        try {
////            coverage = ref.getCoverage(null);
////        } catch (IOException exception) {
////            Object file = ref.getFile();
////            if (file == null) {
////                file = ref.getName();
////            }
////            throw new WMSWebServiceException(Errors.format(ErrorKeys.CANT_READ_$1, file),
////                    exception, LAYER_NOT_QUERYABLE, version);
////        }
////        if (resample) {
////            final GridGeometry gridGeometry = getGridGeometry();
////            if (gridGeometry != null) {
////                final Operations op = Operations.DEFAULT;
////                final CoordinateReferenceSystem targetCRS = crs;
////                coverage = (GridCoverage2D) op.resample(coverage, targetCRS, gridGeometry, interpolation);
////            }
////        }
////        return coverage;
////    }
//    
//    
//    private GridCoverage createPostGridLayer(String name) throws IOException, CatalogException, SQLException{
//        GridCoverage gc = null;
//        Database database = null;
//        LayerTable layers = null;
//        Layer selectedLayer = null;
//
//        database = new Database();
//        layers = database.getTable(LayerTable.class);
//
//        Layer entry = layers.getEntry(name);
//        
//        System.out.println("GRID Layer =" + entry);
//        
//        if(times.isEmpty()){
//            System.out.println("Doesnt have time set");
//            gc = entry.getCoverageReference(null, elevation).getCoverage(null);
//            System.out.println("grid ok");
//        }else{
//            System.out.println("Does have time set");
//            gc = entry.getCoverageReference(times.get(0), elevation).getCoverage(null);
//            System.out.println("grid ok");
//        }
//        
//        return gc;
//    }
//
//    private static MapLayer createPostGridLayer(GridCoverage coverage){
//
//        StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
//        Symbolizer symbol = sf.createDefaultRasterSymbolizer();
//        
//        MutableStyle ms = sf.createStyle(new Symbolizer[]{symbol});
//        
//        MapLayer layer = null;
//        try{
//            layer = new DefaultMapLayer(coverage,ms);
////            layer.graphicbuilders().add(new GridMarkGraphicBuilder());
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//
//        return layer;
//    }
//    
//    public File getImageFile(){
//        System.out.println("---->CREATING MAP");
//        System.out.println("Enveloppe = " +envelope);
//        System.out.println("Mime = "+ mime);
//        System.out.println("Dimension = " + dimension);
//        System.out.print("Layers = ");
//        for(String name : layers){
//            System.out.print(name+" / ");
//        }
//        System.out.println("");
//        
//        prepareContext();
//        
//        System.out.println("Context size =" + context.getLayers().length);
//        
//        File f = null;
//        try{
//            f = File.createTempFile("temp", ".png");
//            ReferencedEnvelope env = new ReferencedEnvelope(envelope);
//            System.out.println("ENV = " + env);
//            service.portray(context, env, f, mime, dimension);
//        }catch(IOException ex){
//            ex.printStackTrace();
//        }catch(TransformException ex){
//            ex.printStackTrace();
//        }
//        return f;
//    }
    

//we set the attribute od the webservice worker with the parameters.
//        webServiceWorker.setService("WMS", getCurrentVersion().toString());
//        webServiceWorker.setFormat(getParameter("FORMAT", true));
//        webServiceWorker.setLayer(getParameter("LAYERS", true));
//        webServiceWorker.setColormapRange(getParameter("DIM_RANGE", false));
//
//        String crs;
//        if (getCurrentVersion().toString().equals("1.3.0")) {
//            crs = getParameter("CRS", true);
//        } else {
//            crs = getParameter("SRS", true);
//        }
//        webServiceWorker.setCoordinateReferenceSystem(crs);
//        webServiceWorker.setBoundingBox(getParameter("BBOX", true));
//        webServiceWorker.setElevation(getParameter("ELEVATION", false));
//        webServiceWorker.setTime(getParameter("TIME", false));
//        webServiceWorker.setDimension(getParameter("WIDTH", true), getParameter("HEIGHT", true), null);
//        webServiceWorker.setBackgroundColor(getParameter("BGCOLOR", false));
//        webServiceWorker.setTransparency(getParameter("TRANSPARENT", false));
}
