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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class LayerLinkReader {

    
    public static final synchronized Map<String,List<String>> read(File linkFile) 
            throws ParserConfigurationException, SAXException, IOException{
        final Map<String,List<String>> links = new HashMap<String,List<String>>();
        if(linkFile.exists()){
            extract(links, linkFile);
        }
        return links;
    }
    
    private static final Map<String,List<String>> extract(Map<String,List<String>>links,File f) 
            throws ParserConfigurationException, SAXException, IOException{
        
        final DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
        final DocumentBuilder constructeur = fabrique.newDocumentBuilder();

        final Document document = constructeur.parse(f);
        
        final Element racine = document.getDocumentElement();
        final String tag = "Link";
        final NodeList liste = racine.getElementsByTagName(tag);
        for(int i=0, n=liste.getLength(); i<n; i++){
            final Element link = (Element)liste.item(i);
            if(link.hasAttribute("LayerName")){
                final String layer = link.getAttribute("LayerName");
                final String styles = link.getTextContent();
                if(layer != null && styles != null){
                    links.put(layer, parseStyles(styles));
                }
            }
                    
        }
        
        return links;
    }
    
    private static final List<String> parseStyles(String strStyles) {
        if(strStyles == null || strStyles.trim().isEmpty()){
            return Collections.emptyList();
        }
        List<String> styles = new ArrayList<String>();
        StringTokenizer token = new StringTokenizer(strStyles.trim(),";",false);
        while(token.hasMoreTokens()){
            styles.add(token.nextToken());
        }
        return styles;
    }

    public static final String getElevationModel(File f)
            throws ParserConfigurationException, SAXException, IOException{

        final DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
        final DocumentBuilder constructeur = fabrique.newDocumentBuilder();

        final Document document = constructeur.parse(f);

        final Element racine = document.getDocumentElement();

        final String tag = "ElevationModel";
        final NodeList liste = racine.getElementsByTagName(tag);
        for(int i=0, n=liste.getLength(); i<n; i++){
            final Element link = (Element)liste.item(i);
            if(link.hasAttribute("LayerName")){
                return link.getAttribute("LayerName");
            }
        }

        return null;
    }
    
}
