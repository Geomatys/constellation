
package net.seagis.provider;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;


public class SoftHashMap<K,V> extends AbstractMap<K,V> {
      
    /** 
     * The internal HashMap that will hold the SoftReference. 
     */
    private final Map<K,SoftValue<V>> hash = new HashMap<K,SoftValue<V>>();
    
    /** 
     * The number of "hard" references to hold internally. 
     */
    private final int HARD_SIZE;
    
    /** 
     * The FIFO list of hard references, order of last access. 
     */
    private final LinkedList hardCache = new LinkedList();
    
    /** 
     * Reference queue for cleared SoftReference objects. 
     */
    private final ReferenceQueue queue = new ReferenceQueue();
    
    public SoftHashMap(int hardSize) { HARD_SIZE = hardSize; }

    @Override
    public V get(Object key) {
      V result = null;
      // We get the SoftReference represented by that key
      SoftReference<V> soft_ref = (SoftReference<V>)hash.get(key);
      if (soft_ref != null) {
	// From the SoftReference we get the value, which can be
	// null if it was not in the map, or it was removed in
	// the processQueue() method defined below
	result = soft_ref.get();
	if (result == null) {
	  // If the value has been garbage collected, remove the
	  // entry from the HashMap.
	  hash.remove(key);
	} else {
	  // We now add this object to the beginning of the hard
	  // reference queue.  One reference can occur more than
	  // once, because lookups of the FIFO queue are slow, so
	  // we don't want to search through it each time to remove
	  // duplicates.
	  hardCache.addFirst(result);
	  if (hardCache.size() > HARD_SIZE) {
	    // Remove the last entry if list longer than HARD_SIZE
	    hardCache.removeLast();
	  }
	}
      }
      return result;
    }

    /** 
     * We define our own subclass of SoftReference which contains
     * not only the value but also the key to make it easier to find
     * the entry in the HashMap after it's been garbage collected. 
     */
    private static class SoftValue<V> extends SoftReference<V> {
      private final Object key; // always make data member final
      /** 
       * Did you know that an outer class can access private data
       * members and methods of an inner class?  I didn't know that!
       * I thought it was only the inner class who could access the
       * outer class's private information.  An outer class can also
       * access private members of an inner class inside its inner
       * class. 
       */
      private SoftValue(V val, Object key, ReferenceQueue q) {
	super(val, q);
	this.key = key;
      }
    }

    /** 
     * Here we go through the ReferenceQueue and remove garbage
     * collected SoftValue objects from the HashMap by looking them
     * up using the SoftValue.key data member. 
     */
    private void processQueue() {
      SoftValue sv;
      while ((sv = (SoftValue)queue.poll()) != null) {
	hash.remove(sv.key); // we can access private data!
      }
    }
    
    /** 
     * Here we put the key, value pair into the HashMap using
     * a SoftValue object. 
     */
    @Override
    public V put(K key, V value) {
      processQueue(); // throw out garbage collected values first
      hash.put(key, new SoftValue(value, key, queue));
      return value;
    }
    
    
    @Override
    public V remove(Object key) {
      processQueue(); // throw out garbage collected values first
      return hash.remove(key).get();
    }
    
    @Override
    public void clear() {
      hardCache.clear();
      processQueue(); // throw out garbage collected values
      hash.clear();
    }
    
    @Override
    public int size() {
      processQueue(); // throw out garbage collected values first
      return hash.size();
    }
    
    public Set entrySet() {
      // no, no, you may NOT do that!!! GRRR
      throw new UnsupportedOperationException();
    }
  }
