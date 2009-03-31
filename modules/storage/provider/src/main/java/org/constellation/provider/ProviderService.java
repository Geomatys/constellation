
package org.constellation.provider;

import java.io.File;
import java.util.Collection;
import org.constellation.provider.configuration.ProviderConfig;

/**
 * @author Johann Sorel (Geomatys)
 */
public interface ProviderService<K,V> {

    public String getName();

    public void init(File config);

    public void init(ProviderConfig props);

    public Collection<? extends Provider<K,V>> getProviders();

}
