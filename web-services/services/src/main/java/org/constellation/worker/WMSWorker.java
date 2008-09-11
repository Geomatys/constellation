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
package org.constellation.worker;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.util.List;

import org.constellation.portrayal.CSTLPortrayalService;
import org.constellation.query.WMSQuery;

import org.geotools.display.service.PortrayalException;
import org.geotools.factory.Hints;
import org.geotools.geometry.jts.ReferencedEnvelope;

import org.geotools.sld.MutableStyledLayerDescriptor;

/**
 * @author Johann Sorel (Geomatys)
 */
public class WMSWorker{

    private final CSTLPortrayalService service = new CSTLPortrayalService();
    private final WMSQuery query;
    private final File output;
    
    
    public WMSWorker(WMSQuery query, File output){
        if(query == null || output == null){
            throw new NullPointerException("Query and outpur file can not be null");
        }
        this.query = query;
        this.output = output;
    }
        
    public File getMap() throws PortrayalException{
        
        final List<String> layers = query.layers;
        final List<String> styles = query.styles;
        final MutableStyledLayerDescriptor sld = query.sld;
        final ReferencedEnvelope contextEnv = new ReferencedEnvelope(query.bbox, query.crs);
        final String mime = query.format;
        final Dimension canvasDimension = query.size;
        final Hints hints = null;
        
        StringBuilder builder = new StringBuilder();
        builder.append("Layers => ");
        for(String layer : layers){
            builder.append(layer +",");
        }
        builder.append("\n");
        builder.append("Styles => ");
        for(String style : styles){
            builder.append(style +",");
        }
        builder.append("\n");
        builder.append("Context env => " + contextEnv.toString() + "\n");
        builder.append("Mime => " + mime.toString() + "\n");
        builder.append("Dimension => " + canvasDimension.toString() + "\n");
        builder.append("File => " + output.toString() + "\n");
        builder.append("BGColor => " + query.background + "\n");
        builder.append("Transparant => " + query.transparent + "\n");
        
        Color bgcolor = (query.transparent) ? null : query.background ;
        System.out.println("REAL COLOR => " + bgcolor);
        
        System.out.println(builder.toString());
        
        service.portray(layers, styles, bgcolor, sld, contextEnv, output, mime, canvasDimension, hints);
        
        return output;
    }

    
}
