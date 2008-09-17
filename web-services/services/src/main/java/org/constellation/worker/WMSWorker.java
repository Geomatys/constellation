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
import org.constellation.query.wms.GetMap;
import org.constellation.query.wms.WMSQuery;

import org.geotools.display.service.PortrayalException;
import org.geotools.factory.Hints;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.sld.MutableStyledLayerDescriptor;
import org.opengis.geometry.Envelope;


/**
 * 
 * @version $Id$
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
        if (!(query instanceof GetMap)) {
            throw new PortrayalException("The request defined is not a GetMap");
        }
        final GetMap getMap = (GetMap) query;
        final List<String> layers = getMap.getLayers();
        final List<String> styles = getMap.getStyles();
        final MutableStyledLayerDescriptor sld = getMap.getSld();
        final Envelope contextEnv = getMap.getEnvelope();
        final String mime = getMap.getFormat();
        final Dimension canvasDimension = getMap.getSize();
        final Hints hints = null;
        
        final StringBuilder builder = new StringBuilder();
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
        builder.append("BGColor => " + getMap.getBackground() + "\n");
        builder.append("Transparent => " + getMap.getTransparent() + "\n");
        
        Color bgcolor = (getMap.getTransparent()) ? null : getMap.getBackground();
        System.out.println("REAL COLOR => " + bgcolor);
        
        System.out.println(builder.toString());
        final ReferencedEnvelope refEnv = new ReferencedEnvelope(contextEnv);
        service.portray(layers, styles, bgcolor, sld, refEnv, output, mime, canvasDimension, hints);
        
        return output;
    }

    
}
