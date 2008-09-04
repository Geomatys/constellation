
package net.seagis.query;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.util.Date;
import java.util.List;
import org.geotools.sld.MutableStyledLayerDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * WMS Query version 1.1.1
 * 
 * @author Johann Sorel (Geomatys)
 */
public class WMSQuery implements Query{
    
    
    public final Rectangle2D bbox;
    public final CoordinateReferenceSystem crs;
    public final String format;
    public final List<String> layers;
    public final List<String> styles;
    public final Double elevation;
    public final Date date;
    public final Dimension size;
    public final Color background;
    public final Boolean transparent;
    public final MutableStyledLayerDescriptor sld;
    
    public WMSQuery(Rectangle2D bbox, CoordinateReferenceSystem crs, String format,
            List<String> layers, List<String> styles, MutableStyledLayerDescriptor sld, Double elevation, Date date,
            Dimension size, Color background, Boolean transparent){
        this.bbox = bbox;
        this.crs =  crs;
        this.format = format;
        this.layers = layers;
        this.styles = styles;
        this.sld = sld;
        this.elevation = elevation;
        this.date = date;
        this.size = size;
        this.background = background;
        this.transparent = transparent;
    }
    
}
