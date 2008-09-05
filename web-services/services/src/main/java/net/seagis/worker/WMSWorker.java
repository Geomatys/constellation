/*
 *    Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 *    (C) 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.seagis.worker;

import java.awt.Dimension;
import java.io.IOException;
import java.util.List;

import net.seagis.portrayal.CSTLPortrayalService;
import net.seagis.query.WMSQuery;

import org.geotools.factory.Hints;
import org.geotools.geometry.jts.ReferencedEnvelope;

import org.geotools.sld.MutableStyledLayerDescriptor;
import org.opengis.referencing.operation.TransformException;

/**
 * @author Johann Sorel (Geomatys)
 */
public class WMSWorker {

    private CSTLPortrayalService service = new CSTLPortrayalService();
    private WMSQuery query = null;
    
    
    public WMSWorker(){
        
    }
    
    public void setQuery(WMSQuery query){
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
