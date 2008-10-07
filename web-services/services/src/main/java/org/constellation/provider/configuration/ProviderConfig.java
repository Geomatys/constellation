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
import java.util.Collections;
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
            config.sources.add(extract(sourceNode));
        }
        
        return config;
    }
    
    private static final ProviderSource extract(final Element element){
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
                
        //parse layers and styles
        for(int i=0, n=layerNodes.getLength(); i<n; i++){
            final Element layerNode = (Element)layerNodes.item(i);
            final String text = layerNode.getTextContent();
            if(layerNode.hasAttribute(ATT_NAME)){
                final String layerName = layerNode.getAttribute(ATT_NAME);
                source.layers.put(layerName, parseStyles(text));
            }
        }
        
        return source;
    }
    
    private static final List<String> parseStyles(String strStyles) {
        if(strStyles == null || strStyles.trim().isEmpty()){
            return Collections.emptyList();
        }
        final List<String> styles = new ArrayList<String>();
        final StringTokenizer token = new StringTokenizer(strStyles.trim(),";",false);
        while(token.hasMoreTokens()){
            styles.add(token.nextToken());
        }
        return styles;
    }
    
}
