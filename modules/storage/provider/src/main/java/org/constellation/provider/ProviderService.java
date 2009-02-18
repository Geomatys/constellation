
package org.constellation.provider;

import java.io.File;
import java.util.Collection;

/**
 * @author Johann Sorel (Geomatys)
 */
public interface ProviderService<K,V> {

    public String getName();

    public void init(File config);

    public Collection<? extends Provider<K,V>> getProviders();

}
