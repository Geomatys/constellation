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
package org.constellation.portrayal;

import java.awt.Color;
import java.awt.Dimension;
import java.util.List;

import org.constellation.provider.NamedLayerDP;
import org.constellation.provider.NamedStyleDP;

import org.geotools.display.service.DefaultPortrayalService;
import org.geotools.display.service.PortrayalException;
import org.geotools.factory.Hints;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.sld.MutableLayer;
import org.geotools.sld.MutableLayerStyle;
import org.geotools.sld.MutableNamedLayer;
import org.geotools.sld.MutableNamedStyle;
import org.geotools.sld.MutableStyledLayerDescriptor;
import org.geotools.style.MutableStyle;


/**
 * Portrayal service, extends the GT portrayal service by adding support
 * to reconize Named layers and styles from constellation data providers.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class CSTLPortrayalService extends DefaultPortrayalService{

    private final NamedLayerDP layerDPS = NamedLayerDP.getInstance();
    private final NamedStyleDP styleDPS = NamedStyleDP.getInstance();


    public CSTLPortrayalService(){}

    public void portray(List<String> layers, List<String> styles, Color background,
            MutableStyledLayerDescriptor sld,ReferencedEnvelope contextEnv, Object output,
            String mime, Dimension canvasDimension,Hints hints)
            throws PortrayalException{

        MapContext context = toMapContext(layers,styles,sld);

        this.portray(context, contextEnv, background, output, mime, canvasDimension, hints);
    }

    private MapContext toMapContext(List<String> layers, List<String> styles, MutableStyledLayerDescriptor sld) 
            throws PortrayalException {
        MapContext ctx = new DefaultMapContext(DefaultGeographicCRS.WGS84);

        int index = 0;
        for(String layerName : layers){
            
            MutableStyle style = null;
            
            if(sld != null){
                //try to use the provided SLD
                style = extractStyle(layerName,sld);
            } else if (styles.size() > index){
                //try to grab the style if provided
                //a style has been given for this layer, try to use it
                style = styleDPS.get(styles.get(index));
            } else {
                //no defined styles, use the favorite one
                List<String> favorites = layerDPS.getFavoriteStyles(layerName);
                if(!favorites.isEmpty()){
                    //take the first one
                    style = styleDPS.get(favorites.get(0));
                }
            }
            
            MapLayer layer = layerDPS.get(layerName,style);
            
            if(layer == null){
                throw new PortrayalException("Layer : "+layerName+" could not be created");
            }
            
            ctx.layers().add(layer);
            index++;
        }

        return ctx;
    }

    private MutableStyle extractStyle(String layerName,MutableStyledLayerDescriptor sld){
        if(sld == null){
            throw new NullPointerException("SLD should not be null");
        }

        List<MutableLayer> layers = sld.layers();
        for(MutableLayer layer : layers){

            if(layer instanceof MutableNamedLayer && layerName.equals(layer.getName()) ){
                //we can only extract style from a NamedLayer that has the same name
                final MutableNamedLayer mnl = (MutableNamedLayer) layer;
                final List<MutableLayerStyle> styles = mnl.styles();
                
                for(MutableLayerStyle mls : styles){
                    MutableStyle GTStyle = null;
                    if(mls instanceof MutableNamedStyle){
                        MutableNamedStyle mns = (MutableNamedStyle) mls;
                        GTStyle = styleDPS.get(mns.getName());
                    }else if(mls instanceof MutableStyle){
                        GTStyle = (MutableStyle) mls;
                    }
                    
                    if(GTStyle != null){
                        //we have found a valid style
                        return GTStyle;
                    }
                }
                
            }
        }

        //no valid style found
        return null;
    }


}
