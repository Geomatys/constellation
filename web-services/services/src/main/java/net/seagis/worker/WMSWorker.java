
package net.seagis.worker;

import java.awt.Dimension;
import java.io.IOException;
import java.util.List;

import net.seagis.portrayal.CSTLPortrayalService;
import net.seagis.query.WMSQuery111;

import org.geotools.factory.Hints;
import org.geotools.geometry.jts.ReferencedEnvelope;

import org.geotools.sld.MutableStyledLayerDescriptor;
import org.opengis.referencing.operation.TransformException;

/**
 * @author Johann Sorel (Geomatys)
 */
public class WMSWorker {

    private CSTLPortrayalService service = new CSTLPortrayalService();
    private WMSQuery111 query = null;
    
    
    public WMSWorker(){
        
    }
    
    public void setQuery(WMSQuery111 query){
        if(query == null){
            throw new NullPointerException("Query can not be null");
        }
        this.query = query;
    }
    
    public Object getMap(Object outputFile) throws IOException, TransformException{
        if(query == null){
            throw new NullPointerException("Query must be set before calling for getMap()");
        }
        
        final List<String> layers = query.layers;
        final List<String> styles = query.styles;
        final MutableStyledLayerDescriptor sld = query.sld;
        final ReferencedEnvelope contextEnv = new ReferencedEnvelope(query.bbox, query.crs);
        final String mime = query.format;
        final Dimension canvasDimension = query.size;
        final Hints hints = null;
        
        service.portray(layers, styles, sld, contextEnv, outputFile, mime, canvasDimension, hints);
        
        return outputFile;
    }
    
    
}
