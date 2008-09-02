
package net.seagis.provider;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.seagis.portrayal.CSTLPortrayalService;
import net.seagis.provider.sld.SLDFileNamedLayerDP;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 *
 * @author sorel
 */
public class tester {

    private static final CSTLPortrayalService service = new CSTLPortrayalService();
    
    public static void main(String[] args){
//       testService();
//       testSLDService();
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
    
//    public static void testSLDService(){
//        SLDFileNamedLayerDP dp = SLDFileNamedLayerDP.getDefault();
//        
//        Set<String> keys = dp.getKeys();
//        
//        for(String key : keys){
//            
//            System.out.println("KEY = " + key);
//            System.out.println("Style = " + dp.get(key));
//        }
//    }
    
    
}
