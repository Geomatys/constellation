/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.provider;

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
    
    
}
