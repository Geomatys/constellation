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
package org.constellation.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import org.constellation.provider.styling.GO2NamedStyleDP;
import org.constellation.provider.styling.SLDNamedStyleDP;

import org.constellation.ws.rs.WebService;


/**
 * Main Data provider for styles objects. This class act as a proxy for
 * several SLD folder providers.
 *
 * @version $Id$
 * @author Johann Sorel (Geomatys)
 */
public class NamedStyleDP implements DataProvider<String,Object>{

    private static String KEY_SLD_DP = "sld_folder";
    
    private static NamedStyleDP instance = null;
    
    private final Collection<DataProvider<String,? extends Object>> dps = new ArrayList<DataProvider<String,? extends Object>>();
    
    private NamedStyleDP(){
        
        List<File> folders = getSLDFolders();
        
        dps.add(GO2NamedStyleDP.getDefault());
        
        for(File folder : folders){
            SLDNamedStyleDP sldDP = new SLDNamedStyleDP(folder);
            dps.add(sldDP);
        }
        
        
        
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
    public Class<Object> getValueClass() {
        return Object.class;
    }

    /**
     * {@inheritDoc }
     */
    public Set<String> getKeys() {
        Set<String> keys = new HashSet<String>();
        for(DataProvider<String,? extends Object> dp : dps){
            keys.addAll( dp.getKeys() );
        }
        return keys;
    }

    /**
     * {@inheritDoc }
     */
    public boolean contains(String key) {
        for(DataProvider<String,? extends Object> dp : dps){
            if(dp.contains(key)) return true;
        }
        return false;
    }

    /**
     * {@inheritDoc }
     */
    public Object get(String key) {
        Object style = null;
        for(DataProvider<String,? extends Object> dp : dps){
            style = dp.get(key);
            if(style != null) return style;
        }
        return null;
    }

    /**
     * {@inheritDoc }
     */
    public void reload() {
        for(DataProvider<String,? extends Object> dp : dps){
            dp.reload();
        }
    }

    /**
     * {@inheritDoc }
     */
    public void dispose() {
        for(DataProvider<String,? extends Object> dp : dps){
            dp.dispose();
        }
        dps.clear();
    }

    /**
     * 
     * @return List of folders holding sld files
     */
    private static List<File> getSLDFolders(){
        List<File> folders = new ArrayList<File>();
        
        String strFolders = "";
        try{
            strFolders = WebService.getPropertyValue(JNDI_GROUP,KEY_SLD_DP);
        }catch(NamingException ex){
            Logger.getLogger(NamedStyleDP.class.toString()).log(Level.WARNING, "Serveur property has not be set : "+JNDI_GROUP +" - "+ KEY_SLD_DP);
        }

        if (strFolders == null) {
            return Collections.emptyList();
        }
        StringTokenizer token = new StringTokenizer(strFolders,";");
        while(token.hasMoreElements()){
            String path = token.nextToken();
            File f = new File(path);
            if(f.exists() && f.isDirectory()){
                folders.add(f);
            }else{
                Logger.getLogger(NamedStyleDP.class.toString()).log(Level.WARNING, "SLD folder provided is unvalid : "+ path);
            }
        }
        
        return folders;
    }
    
    public static NamedStyleDP getInstance(){
        
        if(instance == null){
            instance = new NamedStyleDP();
        }
        
        return instance;
    }
    
}
