/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2014, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.map.ElevationModel;
import org.opengis.feature.type.Name;

/**
 * Main data provider for MapLayer objects. This class act as a proxy for
 * different kind of data sources, postgrid, shapefile ...
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 */
public final class LayerProviderProxy extends AbstractProviderProxy<Name,LayerDetails,LayerProvider,LayerProviderService> {

    private static final LayerProviderProxy INSTANCE = new LayerProviderProxy();
    //all providers factories, unmodifiable
    private static final Collection<LayerProviderService> SERVICES;

    static {
        final List<LayerProviderService> cache = new ArrayList<>();
        final ServiceLoader<LayerProviderService> loader = ServiceLoader.load(LayerProviderService.class);
        for(final LayerProviderService service : loader){
            cache.add(service);
        }
        SERVICES = Collections.unmodifiableCollection(cache);
    }

    private LayerProviderProxy(){
        super(Name.class, LayerDetails.class);
    }

    /**
     * {@inheritDoc }
     */
    public ElevationModel getElevationModel(final Name name) {
        for(final LayerProvider provider : getProviders()){
            final ElevationModel model = provider.getElevationModel(name);
            if(model != null) return model;
        }
        return null;
    }

    public LayerDetails get(final Name key, final Date version) {
        final List<LayerDetails> candidates = new ArrayList<>();

        for(final LayerProvider provider : getProviders()){
            final LayerDetails layer = provider.get(key, version);
            if(layer != null) {
                candidates.add(layer);
            }
        }

        if(candidates.size() == 1){
            return candidates.get(0);
        }else if(candidates.size()>1){
            if(LayerDetails.class.isAssignableFrom(valClass)){
                //make a more accurate search testing both namespace and local part are the same.
                final Name nk = (Name) key;
                for(int i=0;i<candidates.size();i++){
                    final LayerDetails ld = candidates.get(i);
                    if(Objects.equals(ld.getName().getNamespaceURI(), nk.getNamespaceURI())
                            && Objects.equals(ld.getName().getLocalPart(), nk.getLocalPart())){
                        return ld;
                    }
                }

                //we could not find one more accurate then another
                return candidates.get(0);
            }else{
                return candidates.get(0);
            }
        }

        return null;
    }

    public LayerDetails get(final Name key, final String providerID, final Date version) {
        final LayerProvider provider = getProvider(providerID);
        if (provider == null) {
            return null;
        }
        if (version != null) {
            return provider.get(key, version);
        }
        return provider.get(key);
    }

    @Override
    public Collection<LayerProviderService> getServices() {
        return SERVICES;
    }

    public LayerProviderService getService(final String serviceID) {
        for (LayerProviderService serv : SERVICES) {
            if (serv.getName().equals(serviceID)) {
                return serv;
            }
        }
        return null;
    }

    public LayerDetails getByIdentifier(Name key) {
        LayerDetails result = null;
        for(final Name n : getKeys()){
            if(n.equals(key)){
                return get(n);
            } else if (DefaultName.match(n, key)) {
                result = get(n);
            }
        }
        return result;
    }

    /**
     * Returns the current instance of {@link LayerProviderProxy}.
     */
    public static LayerProviderProxy getInstance(){
        return INSTANCE;
    }
    
}
