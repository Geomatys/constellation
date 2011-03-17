/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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

package org.constellation.bean;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import javax.faces.context.FacesContext;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class I18NBean {

    private final Map<String,ResourceBundle> bundles = new HashMap<String, ResourceBundle>();
    private final RBMap i18n = new RBMap();
    private Locale locale = Locale.getDefault();

    protected I18NBean(){        
    }

    public void addBundle(final String ... bundles){
        final Locale lc = locale;
        for(final String bundle : bundles){
            this.bundles.put(bundle, ResourceBundle.getBundle(bundle, lc));
        }
    }

    public void removeBundle(final String bundle){
        bundles.remove(bundle);
    }

    public void removeAllBundles(){
        bundles.clear();
    }

    private void reload(){
        //reload bundles
        final Set<String> keys = new HashSet<String>(bundles.keySet());
        removeAllBundles();
        addBundle(keys.toArray(new String[keys.size()]));
    }

    public Map<String,String> getI18n(){
        final FacesContext context = FacesContext.getCurrentInstance();
        final Locale currentLocal = context.getViewRoot().getLocale();
        if(!currentLocal.equals(locale)){
            //local changed, reload bundles
            locale = currentLocal;
            reload();
        }
        return i18n;
    }

    private class RBMap extends AbstractMap<String, String>{

        @Override
        public Set<Entry<String, String>> entrySet() {
            final Map<String,String> map = new HashMap<String, String>();
            for(ResourceBundle rb : bundles.values()){
                for(String key : rb.keySet()){
                    map.put(key, rb.getString(key));
                }
            }
            return map.entrySet();
        }

        @Override
        public String get(Object key) {
            final String strKey = (String) key;
            for(ResourceBundle rb : bundles.values()){
                if(rb.containsKey(strKey)){
                    return rb.getString(strKey);
                }
            }
            return strKey;
        }

    }

}
