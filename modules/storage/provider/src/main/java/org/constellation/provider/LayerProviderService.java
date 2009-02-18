

package org.constellation.provider;

import java.util.Collection;


/**
 * @author Johann Sorel (Geomatys)
 */
public interface LayerProviderService extends ProviderService<String,LayerDetails>{

    @Override
    public Collection<? extends LayerProvider> getProviders();

}
