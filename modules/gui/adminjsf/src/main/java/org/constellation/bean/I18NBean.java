/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
        final Locale lc = getLocale();
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

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(final Locale local) {
        this.locale = local;
        
        //reload bundles
        final Set<String> keys = new HashSet<String>(bundles.keySet());
        removeAllBundles();
        addBundle(keys.toArray(new String[keys.size()]));
    }

    public Map<String,String> getI18n(){
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
