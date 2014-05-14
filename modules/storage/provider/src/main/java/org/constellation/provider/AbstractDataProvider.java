/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.constellation.admin.dao.ProviderRecord.ProviderType;
import org.constellation.provider.configuration.ProviderParameters;

import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.gui.swing.tree.Trees;
import org.geotoolkit.map.ElevationModel;

import org.opengis.feature.type.Name;
import org.opengis.parameter.ParameterValueGroup;

import static org.constellation.provider.configuration.ProviderParameters.*;
import static org.geotoolkit.parameter.Parameters.*;

/**
 * Abstract implementation of LayerProvider which only handle the
 * getByIdentifier(String key) method.
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractDataProvider extends AbstractProvider<Name,Data> implements DataProvider{

    protected static final String DEFAULT_NAMESPACE = "http://geotoolkit.org";
    protected static final String NO_NAMESPACE = "no namespace";


    protected AbstractDataProvider(final String id, final ProviderFactory service,
            final ParameterValueGroup config){
        super(id, service,config);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class<Name> getKeyClass() {   
        return Name.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class<Data> getValueClass() {
        return Data.class;
    }

    @Override
    public boolean contains(final Name key) {
        for(Name n : getKeys()){
            if(DefaultName.match(n, key)){
                return true;
            }
        }
        return false;
    }

    @Override
    public Data get(String key){
        final Name name = new DefaultName(ProviderParameters.getNamespace(this), key);
        return get(name);
    }
    
    /**
     * Fill namespace on name is not present.
     */
    protected Name fullyQualified(final Name key){
        for(Name n : getKeys()){
            if(DefaultName.match(n, key)){
                return n;
            }
        }
        return key;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Data getByIdentifier(final Name key) {
        for(final Name n : getKeys()){
            if(n.equals(key)){
                return get(n);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ElevationModel getElevationModel(final Name name) {
        return null;
    }

    /**
     * Provider should pass by this method to fill there index.
     * This method will only log miss configurations, loading the index
     * is part of the child class.
     */
    protected void visit(){
        final ParameterValueGroup config = getSource();
        final Set<Name> keys = getKeys();

        final List<String> missingLayers = new ArrayList<>();

        loop:
        for(final ParameterValueGroup declaredLayer : getLayers(config)){
            final String layerName = stringValue(LAYER_NAME_DESCRIPTOR, declaredLayer);
            for(Name n : keys){
                if(DefaultName.match(n, layerName)) continue loop;
            }

            missingLayers.add(layerName);
        }

        if(!missingLayers.isEmpty()){
            //log list of missing layers
            final StringBuilder sb = new StringBuilder("Provider ");
            sb.append(getId()).append(" declares layers missing in the source\n");
            sb.append(Trees.toString("", missingLayers));
            getLogger().log(Level.WARNING, sb.toString());
        }
    }

    public static Name containsOnlyLocalPart(final Collection<Name> index, final Name layerName) {
        if (layerName != null) {
            if (layerName.getNamespaceURI() == null) {
                for (Name name : index) {
                    if (name.getLocalPart().equals(layerName.getLocalPart())) {
                        return name;
                    }
                }
            }
        }
        return null;
    }

    public static Name containsWithNamespaceError(final Collection<Name> index, final Name layerName) {
        if (layerName != null) {
            for (Name name : index) {
                if (name.getLocalPart().equals(layerName.getLocalPart())) {
                    return name;
                }
            }
        }
        return null;
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.LAYER;
    }
    
    @Override
    public boolean isSensorAffectable() {
        return false;
    }
}
