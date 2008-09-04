
package net.seagis.provider;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import net.seagis.portrayal.CSTLPortrayalService;
import net.seagis.provider.shapefile.ShapeFileNamedLayerDP;
import net.seagis.provider.sld.SLDFileNamedLayerDP;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.sld.MutableLayer;
import org.geotools.style.MutableRule;
import org.geotools.style.MutableStyle;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.style.ExternalGraphic;
import org.opengis.style.GraphicFill;
import org.opengis.style.PolygonSymbolizer;

/**
 *
 * @author sorel
 */
public class tester {

    private static final CSTLPortrayalService service = new CSTLPortrayalService();
    
    public static void main(String[] args){
        try {
//       testService();
//       testSLDService();
//            testFiltering();
         testXMLSLDRead();
        } catch (Exception ex) {
            Logger.getLogger(tester.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    private static void testXMLSLDRead() throws IOException, TransformException {
        ShapeFileNamedLayerDP layerDP = new ShapeFileNamedLayerDP(new File("/home/sorel/GIS_DATA/data-wms-1.3.0/shapefile"));
        SLDFileNamedLayerDP styleDP = new SLDFileNamedLayerDP(new File("/home/sorel/GIS_DATA/Styles"));
        MutableStyle style = styleDP.get("cite_style_Forests");
        
        MutableRule R = style.featureTypeStyles().get(0).rules().get(0);
        PolygonSymbolizer symbol = (PolygonSymbolizer) R.symbolizers().get(0);
        GraphicFill fill = symbol.getFill().getGraphicFill();
        ExternalGraphic ext = (ExternalGraphic) fill.graphicalSymbols().get(0);
        System.out.println("Resource = " + ext.getOnlineResource() );
        System.out.println("icon = " + ext.getInlineContent() );
                
        URI uri = ext.getOnlineResource().getLinkage();
        File f = new File(uri);
        System.out.println(f.exists());
        
        CSTLPortrayalService service = new CSTLPortrayalService();
        
        Rectangle2D rect = new Rectangle2D.Double(-180d, 90d, 360d, -180d);
        CoordinateReferenceSystem crs = null;
        crs = DefaultGeographicCRS.WGS84;
        ReferencedEnvelope dataEnvelope = null;
        dataEnvelope = new ReferencedEnvelope(rect, crs);
        final String mime = "image/png";
        final Dimension size = new Dimension( 600,600 );
        final File output = File.createTempFile("forest", ".png");

        MapLayer layer = layerDP.get("cite:Forests");
        layer.setStyle(style);
        MapContext context = new DefaultMapContext(crs);
        context.layers().add(layer);
        
        service.portray(context, layer.getBounds(), output, mime, size, null);
        
    }
    
    
    public static void testFiltering() throws IOException, TransformException{
        ShapeFileNamedLayerDP layerDP = new ShapeFileNamedLayerDP(new File("/home/sorel/GIS_DATA/data-wms-1.3.0/shapefile"));
        SLDFileNamedLayerDP styleDP = new SLDFileNamedLayerDP(new File("/home/sorel/GIS_DATA/Styles"));
        
        MapLayer layer = layerDP.get("cite:RoadSegments");
        MutableStyle style = styleDP.get("cite_style_RoadSegments");
        
        System.out.println("Layer =>" + layer);
        System.out.println("Style =>" + style);
        MutableRule r0 = style.featureTypeStyles().get(0).rules().get(0);
        MutableRule r1 = style.featureTypeStyles().get(0).rules().get(1);
        MutableRule r2 = style.featureTypeStyles().get(0).rules().get(2);
        
        layer.setStyle(style);
        
        
        System.out.println("R0 =>" + r0.getFilter());
        System.out.println("R1 =>" + r1.getFilter());
        System.out.println("R2 =>" + r2.getFilter());
        
        CSTLPortrayalService service = new CSTLPortrayalService();
        
        Rectangle2D rect = new Rectangle2D.Double(-180d, 90d, 360d, -180d);
        CoordinateReferenceSystem crs = null;
        crs = DefaultGeographicCRS.WGS84;
        ReferencedEnvelope dataEnvelope = null;
        dataEnvelope = new ReferencedEnvelope(rect, crs);
        final String mime = "image/png";
        final Dimension size = new Dimension( 800,600 );
        final File output = File.createTempFile("temp", ".png");

        MapContext context = new DefaultMapContext(crs);
        context.layers().add(layer);
        
        service.portray(context, dataEnvelope, output, mime, size, null);
        
        
    }
    
    
    
    public static void testService(){
        try {
            final String mime = "image/png";
            final List<String> layers = new ArrayList<String>();
            layers.add("BlueMarble");
    //        layers.add("ROADL");
            final List<String> styles = new ArrayList<String>();
            final Dimension size = new Dimension( 800,600 );
            final File output = File.createTempFile("temp", ".png");

            Rectangle2D rect = new Rectangle2D.Double(-180d, 90d, 360d, -180d);
            CoordinateReferenceSystem crs = null;
            crs = DefaultGeographicCRS.WGS84;
            ReferencedEnvelope dataEnvelope = null;
            dataEnvelope = new ReferencedEnvelope(rect, crs);

            service.portray(layers, styles, null, dataEnvelope, output, mime, size, null);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    public static void testSLDService(){
        ShapeFileNamedLayerDP layerDP = new ShapeFileNamedLayerDP(new File("/home/sorel/GIS_DATA/data-wms-1.3.0/shapefile"));
        SLDFileNamedLayerDP styleDP = new SLDFileNamedLayerDP(new File("/home/sorel/GIS_DATA/Styles"));
//       NamedStyleDP dp = NamedStyleDP.getInstance();
        Set<String> layerKeys = layerDP.getKeys();
        Set<String> styleKeys = styleDP.getKeys();
        
        for(String key : layerKeys){
            System.out.println("KEY = " + key);
            List<String> styles = layerDP.getFavoriteStyles(key);
            for(String style : styles){
                System.out.println("STYLE = "+ style);
            }
        }
        
        
//        for(String key : styleKeys){
//            System.out.println("KEY = " + key);
//        }
    }

    
    
}
