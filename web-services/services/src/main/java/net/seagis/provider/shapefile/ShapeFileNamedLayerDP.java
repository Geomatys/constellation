
package net.seagis.provider.shapefile;

import net.seagis.provider.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapLayer;
import org.geotools.style.MutableStyle;
import org.geotools.style.RandomStyleFactory;
import org.geotools.style.StyleFactory;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Shapefile Data provider. index and cache Datastores for the shapefiles
 * whithin the given folder.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class ShapeFileNamedLayerDP implements DataProvider<String,MapLayer>{

    private static ShapeFileNamedLayerDP instance = null;
    
    private static final StyleFactory STYLE_FACTORY = CommonFactoryFinder.getStyleFactory(null);
    private static final RandomStyleFactory RANDOM_FACTORY = new RandomStyleFactory();
    private static final String mask = ".shp";
    
    private final File folder;
    private final Map<String,File> index = new HashMap<String,File>();
    private final SoftHashMap<String,DataStore> cache = new SoftHashMap<String, DataStore>(5);
    
    
    public ShapeFileNamedLayerDP(File folder){
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
    public Class<MapLayer> getValueClass() {
        return MapLayer.class;
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
    public MapLayer get(String key) {
        MapLayer layer = null;
        
        DataStore store = cache.get(key);
        
        if(store == null){
            File f = index.get(key);
            if(f != null){
                //we have this data source in the folder
                store = loadDataStore(f);
                if(store != null){
                    //cache the datastore
                    cache.put(key, store);
                }
            }
        }
        
        if(store != null){
            //DataStore is in cache, reuse it.
            layer = createMapLayer(store);
        }
        
        return layer;
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
    
    private MapLayer createMapLayer(DataStore store){
        MapLayer layer = null;
        
        FeatureSource<SimpleFeatureType,SimpleFeature> fs = null;
                
        try{
            fs = store.getFeatureSource(store.getTypeNames()[0]);
        }catch(IOException ex){
            //TODO log error
            ex.printStackTrace();
        }
        
        if(fs != null){
            MutableStyle style = RANDOM_FACTORY.createRandomVectorStyle(fs);
            layer = new DefaultMapLayer(fs, style);
        }else{
            System.err.println(ShapeFileNamedLayerDP.class +" Error : Could not create shapefile maplayer.");
            //TODO log error
        }
        
        return layer;
    }
    
    private DataStore loadDataStore(File f){
        DataStore store = null;
        
        if(f.exists()){
            Map<String,Object> params = new HashMap<String,Object>();
            
            try{
                params.put( "url", f.toURI().toURL() );
                store = DataStoreFinder.getDataStore(params);
            }catch(IOException ex){
                ex.printStackTrace();
                //TODO log error
            }
        }else{
            System.err.println(ShapeFileNamedLayerDP.class +" Error : Could not create shapefile datastore. File does not exits.");
            //TODO log error
        }
        
        return store;
    }
    
}
