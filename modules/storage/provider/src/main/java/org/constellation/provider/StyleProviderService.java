

package org.constellation.provider;

import java.util.Collection;


/**
 * @author Johann Sorel (Geomatys)
 */
public interface StyleProviderService extends ProviderService<String,Object>{

    @Override
    public Collection<? extends StyleProvider> getProviders();

}
