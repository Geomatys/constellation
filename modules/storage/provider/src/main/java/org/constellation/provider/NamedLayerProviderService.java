/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.provider;

import java.util.Collection;
import org.opengis.feature.type.Name;

/**
 *
 * @author guilhem
 */
public interface NamedLayerProviderService extends ProviderService<Name,LayerDetails> {

    @Override
    public Collection<? extends NamedLayerProvider> getProviders();
}
