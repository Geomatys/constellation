

package org.constellation.provider;

import java.util.Collection;
import org.geotools.style.MutableStyle;


/**
 * @author Johann Sorel (Geomatys)
 */
public interface StyleProviderService extends ProviderService<String,MutableStyle>{

    @Override
    public Collection<? extends StyleProvider> getProviders();

}
