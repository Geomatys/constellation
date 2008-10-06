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
    private static final String TAG_STYLE = "Style";
    private static final String TAG_IGNORE = "Ignore";
    private static final String ATT_PARAMETER_NAME = "ParameterName";
    private static final String ATT_LAYER_NAME = "LayerName";
    
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
        final NodeList ignoreNodes = element.getElementsByTagName(TAG_IGNORE);
        final NodeList styleNodes = element.getElementsByTagName(TAG_STYLE);
        
        //parse parameters
        for(int i=0, n=paramNodes.getLength(); i<n; i++){
            final Element paramNode = (Element)paramNodes.item(i);
            final String text = paramNode.getTextContent();
            if(paramNode.hasAttribute(ATT_PARAMETER_NAME) && text != null && !text.trim().isEmpty()){
                source.parameters.put(paramNode.getAttribute(ATT_PARAMETER_NAME), text.trim());
            }
        }
        
        //parse ignores
        for(int i=0, n=ignoreNodes.getLength(); i<n; i++){
            final Element ignoreNode = (Element)ignoreNodes.item(i);
            if(ignoreNode.hasAttribute(ATT_LAYER_NAME)){
                source.ignores.add(ignoreNode.getAttribute(ATT_LAYER_NAME));
            }
        }
        
        //parse styles
        for(int i=0, n=styleNodes.getLength(); i<n; i++){
            final Element styleNode = (Element)styleNodes.item(i);
            final String text = styleNode.getTextContent();
            if(styleNode.hasAttribute(ATT_LAYER_NAME) && text != null && !text.trim().isEmpty()){
                final String layerName = styleNode.getAttribute(ATT_LAYER_NAME);
                if(!source.ignores.contains(layerName)){
                    source.styleLinks.put(layerName, parseStyles(text.trim()));
                }
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
