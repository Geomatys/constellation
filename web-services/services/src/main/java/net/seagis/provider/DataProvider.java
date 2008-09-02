

package net.seagis.provider;

import java.util.Set;

/**
 * A dataprovider is basicly a index class  
 * 
 * @author Johann Sorel (Geomatys)
 */
public interface DataProvider<K,V> {

    public final String JNDI_GROUP = "Data Provider Properties";
    
    /**
     * @return the Key class
     */
    Class<K> getKeyClass();
    
    /**
     * @return the Value class.
     */
    Class<V> getValueClass();
    
    
    /**
     * Use this method if you need the complete list of entries in this data provider.
     * If you are just searching if a special key exists than you should use the contains method.
     */
    Set<K> getKeys();
    
    /**
     * If you want to intend to get the related data, you should use the
     * get method directly and test if the result is not null.
     * 
     * @return true if the given key data is in this data provider .
     */
    boolean contains(K key);
    
    /**
     * Get the data related to the given key.
     * @return V object if it is in the dataprovider, or null if not.
     */
    V get(K key);
    
    /**
     * Reload data provider. this may be usefull if new entries on disk have been
     * added after creation.
     */
    void reload();
    
    /**
     * Clear every caches, this dataprovider should not be used after a call
     * to this method.
     */
    void dispose();
    
}
