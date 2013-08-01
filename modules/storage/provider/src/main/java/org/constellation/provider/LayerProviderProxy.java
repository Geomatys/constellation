/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2010, Geomatys
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

import java.util.*;

import org.geotoolkit.feature.DefaultName;

import org.geotoolkit.map.ElevationModel;

import org.geotoolkit.util.Utilities;
import org.opengis.feature.type.Name;

/**
 * Main data provider for MapLayer objects. This class act as a proxy for
 * different kind of data sources, postgrid, shapefile ...
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 */
public final class LayerProviderProxy extends AbstractProviderProxy<Name,LayerDetails,LayerProvider,LayerProviderService>
        implements LayerProvider{

    private static final LayerProviderProxy INSTANCE = new LayerProviderProxy();
    //all providers factories, unmodifiable
    private static final Collection<LayerProviderService> SERVICES;

    static {
        final List<LayerProviderService> cache = new ArrayList<LayerProviderService>();
        final ServiceLoader<LayerProviderService> loader = ServiceLoader.load(LayerProviderService.class);
        for(final LayerProviderService service : loader){
            cache.add(service);
        }
        SERVICES = Collections.unmodifiableCollection(cache);
    }


    private LayerProviderProxy(){}

    @Override
    public Class<Name> getKeyClass() {
        return Name.class;
    }

    @Override
    public Class<LayerDetails> getValueClass() {
        return LayerDetails.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ElevationModel getElevationModel(Name name) {
        for(final LayerProvider provider : getProviders()){
            final ElevationModel model = provider.getElevationModel(name);
            if(model != null) return model;
        }
        return null;
    }

    @Override
    public LayerDetails get(Name key, Date version) {
        final List<LayerDetails> candidates = new ArrayList<LayerDetails>();

        for(final LayerProvider provider : getProviders()){
            final LayerDetails layer = provider.get(key, version);
            if(layer != null) {
                candidates.add(layer);
            }
        }

        if(candidates.size() == 1){
            return candidates.get(0);
        }else if(candidates.size()>1){
            if(LayerDetails.class.isAssignableFrom(getValueClass())){
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

    public LayerDetails get(Name key, final String providerID, Date version) {
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

    @Override
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
     * @return null, this provider does not have a service.
     */
    @Override
    public ProviderService<Name, LayerDetails, Provider<Name, LayerDetails>> getService() {
        return null;
    }

    /**
     * Returns the current instance of {@link LayerProviderProxy}.
     */
    public static LayerProviderProxy getInstance(){
        return INSTANCE;
    }

}
