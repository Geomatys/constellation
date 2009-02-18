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
package org.constellation.provider.configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * XML configuration file used for data providers.
 *
 * @author Johann Sorel (Geomatys)
 */
public class ProviderConfig {

    private static final String TAG_SOURCE = "Source";
    private static final String TAG_PARAMETER = "Parameter";
    private static final String TAG_LAYER = "Layer";
    private static final String ATT_NAME = "name";
    
    private static final String PARAM_STYLE = "style";
    private static final String PARAM_PERIODE_START = "periode_start";
    private static final String PARAM_PERIODE_END = "periode_end";
    private static final String PARAM_ELEVATION_START = "elevation_start";
    private static final String PARAM_ELEVATION_END = "elevation_end";
    private static final String PARAM_ELEVATION_MODEL = "elevation_model";
    private static final String PARAM_IS_ELEVATION_MODEL = "is_elevation_model";
    
    public final List<ProviderSource> sources = new ArrayList<ProviderSource>();
    
    private ProviderConfig(){}
    
    public static final synchronized ProviderConfig read(final File configFile) 
            throws ParserConfigurationException, SAXException, IOException{
        final DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
        final DocumentBuilder constructeur = fabrique.newDocumentBuilder();
        final Document document = constructeur.parse(configFile);
        final NodeList nodes = document.getElementsByTagName(TAG_SOURCE);
        final ProviderConfig config = new ProviderConfig();
        
        for(int i=0, n=nodes.getLength(); i<n; i++){
            final Element sourceNode = (Element)nodes.item(i);
            config.sources.add( parseSource(sourceNode));
        }
        
        return config;
    }
    
    private static final ProviderSource parseSource(final Element element){
        final ProviderSource source = new ProviderSource();
        
        final NodeList paramNodes = element.getElementsByTagName(TAG_PARAMETER);
        final NodeList layerNodes = element.getElementsByTagName(TAG_LAYER);
        
        //parse parameters
        for(int i=0, n=paramNodes.getLength(); i<n; i++){
            final Element paramNode = (Element)paramNodes.item(i);
            final String text = paramNode.getTextContent();
            if(paramNode.hasAttribute(ATT_NAME) && text != null && !text.trim().isEmpty()){
                source.parameters.put(paramNode.getAttribute(ATT_NAME), text.trim());
            }
        }
                
        //parse layers
        for(int i=0, n=layerNodes.getLength(); i<n; i++){
            final Element layerNode = (Element)layerNodes.item(i);
            if(layerNode.hasAttribute(ATT_NAME)){
                source.layers.add(parseLayer(layerNode));
            }
        }
        
        return source;
    }
    
    private static final ProviderLayer parseLayer(final Element element){
        final String layerName = element.getAttribute(ATT_NAME);
        final List<String> layerStyles = new ArrayList<String>();
        String startDate = null;
        String endDate = null;
        String startElevation = null;
        String endElevation = null;
        String elevationModel = null;
        boolean isElevationModel = false;
        
        final NodeList paramNodes = element.getElementsByTagName(TAG_PARAMETER);
        for(int i=0, n=paramNodes.getLength(); i<n; i++){
            final Element paramNode = (Element)paramNodes.item(i);
            final String name = paramNode.getAttribute(ATT_NAME);
            final String text = paramNode.getTextContent();
            
            if(PARAM_STYLE.equalsIgnoreCase(name)){
                //there should be only one style node, but whatever, parse N node if there are N.
                parseStyles(text, layerStyles);
            }else if(PARAM_PERIODE_START.equalsIgnoreCase(name)){
                startDate = text;
            }else if(PARAM_PERIODE_END.equalsIgnoreCase(name)){
                endDate = text;
            }else if(PARAM_ELEVATION_START.equalsIgnoreCase(name)){
                startElevation = text;
            }else if(PARAM_ELEVATION_END.equalsIgnoreCase(name)){
                endElevation = text;
            }else if(PARAM_IS_ELEVATION_MODEL.equalsIgnoreCase(name)){
                isElevationModel = Boolean.valueOf(text);
            }else if(PARAM_ELEVATION_MODEL.equalsIgnoreCase(name)){
                elevationModel = text;
            }
            
        }
        return new ProviderLayer(layerName, layerStyles, startDate, endDate, 
                startElevation, endElevation,isElevationModel,elevationModel);
    }
    
    
    private static final void parseStyles(String strStyles, List<String> styles) {
        if(strStyles == null || strStyles.trim().isEmpty()){
            return;
        }
        final StringTokenizer token = new StringTokenizer(strStyles.trim(),";",false);
        while(token.hasMoreTokens()){
            styles.add(token.nextToken());
        }
    }
    
}
