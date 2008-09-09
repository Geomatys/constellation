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
package org.constellation.provider.sld;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.constellation.provider.DataProvider;
import org.constellation.provider.SoftHashMap;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.style.MutableStyle;
import org.geotools.style.RandomStyleFactory;
import org.geotools.style.StyleFactory;
import org.geotools.style.sld.Specification.SymbologyEncoding;
import org.geotools.style.sld.XMLUtilities;

/**
 * Style provider. index and cache MutableStyle whithin the given folder.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class SLDFileNamedLayerDP implements DataProvider<String,MutableStyle>{

    private static SLDFileNamedLayerDP instance = null;
    
    private static final StyleFactory STYLE_FACTORY = CommonFactoryFinder.getStyleFactory(null);
    private static final RandomStyleFactory RANDOM_FACTORY = new RandomStyleFactory();
    private static final String mask = ".xml";
    
    private final XMLUtilities sldParser = new XMLUtilities();
    private final File folder;
    private final Map<String,File> index = new HashMap<String,File>();
    private final SoftHashMap<String,MutableStyle> cache = new SoftHashMap<String, MutableStyle>(20);
    
    
    public SLDFileNamedLayerDP(File folder){
        if(folder == null || !folder.exists() || !folder.isDirectory()){
            throw new IllegalArgumentException("Provided File does not exits or is not a folder.");
        }
        
        this.folder = folder;
        visit(folder);
    }
    
    /**
     * {@inheritDoc }
     */
    public Class<String> getKeyClass() {
        return String.class;
    }

    /**
     * {@inheritDoc }
     */
    public Class<MutableStyle> getValueClass() {
        return MutableStyle.class;
    }

    /**
     * {@inheritDoc }
     */
    public Set<String> getKeys() {
        return index.keySet();
    }

    /**
     * {@inheritDoc }
     */
    public boolean contains(String key) {
        return index.containsKey(key);
    }

    /**
     * {@inheritDoc }
     */
    public MutableStyle get(String key) {
        MutableStyle style = null;
        
        style = cache.get(key);
        
        if(style == null){
            File f = index.get(key);
            if(f != null){
                try {
                    style = sldParser.readStyle(f, SymbologyEncoding.V_1_1_0);
                } catch (JAXBException ex) {
                    Logger.getLogger(SLDFileNamedLayerDP.class.getName()).log(Level.SEVERE, null, ex);
                }
                if(style != null){
                    //cache the style
                    cache.put(key, style);
                }
            }
        }
        
        return style;
    }

    /**
     * {@inheritDoc }
     */
    public void reload() {
        synchronized(this){
            index.clear();
            cache.clear();
            visit(folder);
        }
    }

    /**
     * {@inheritDoc }
     */
    public void dispose() {
        synchronized(this){
            index.clear();
            cache.clear();
        }
    }
    
    private void visit(File file) {

        if (file.isDirectory()) {
            File[] list = file.listFiles();
            if (list != null) {
                for (int i = 0; i < list.length; i++) {
                    visit(list[i]);
                }
            }
        }else{
            test(file);
        }
    }
    
    private void test(File candidate){
        if(candidate.isFile()){
            String fullName = candidate.getName();
            if(fullName.toLowerCase().endsWith(mask)){
                String name = fullName.substring(0, fullName.length()-4);
                index.put(name, candidate);
            }
        }
    }
     
}
