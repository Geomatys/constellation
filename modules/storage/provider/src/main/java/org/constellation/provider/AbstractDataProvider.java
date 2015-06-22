/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.provider;

import org.constellation.api.ProviderType;
import org.constellation.provider.configuration.ProviderParameters;
import org.geotoolkit.gui.swing.tree.Trees;
import org.geotoolkit.map.ElevationModel;
import org.opengis.parameter.ParameterValueGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import static org.constellation.provider.configuration.ProviderParameters.LAYER_NAME_DESCRIPTOR;
import static org.constellation.provider.configuration.ProviderParameters.getLayers;
import org.geotoolkit.feature.type.NamesExt;
import static org.geotoolkit.parameter.Parameters.stringValue;
import org.opengis.util.GenericName;

/**
 * Abstract implementation of LayerProvider which only handle the
 * getByIdentifier(String key) method.
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractDataProvider extends AbstractProvider<GenericName,Data> implements DataProvider{

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
    public Class<GenericName> getKeyClass() {
        return GenericName.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class<Data> getValueClass() {
        return Data.class;
    }

    @Override
    public boolean contains(final GenericName key) {
        for(GenericName n : getKeys()){
            if(NamesExt.match(n, key)){
                return true;
            }
        }
        return false;
    }

    @Override
    public Data get(String key){
        final GenericName name = NamesExt.create(ProviderParameters.getNamespace(this), key);
        return get(name);
    }
    
    /**
     * Fill namespace on name is not present.
     */
    protected GenericName fullyQualified(final GenericName key){
        for(GenericName n : getKeys()){
            if(NamesExt.match(n, key)){
                return n;
            }
        }
        return key;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Data getByIdentifier(final GenericName key) {
        for(final GenericName n : getKeys()){
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
    public ElevationModel getElevationModel(final GenericName name) {
        return null;
    }

    /**
     * Provider should pass by this method to fill there index.
     * This method will only log miss configurations, loading the index
     * is part of the child class.
     */
    protected void visit(){
        final ParameterValueGroup config = getSource();
        final Set<GenericName> keys = getKeys();

        final List<String> missingLayers = new ArrayList<>();

        loop:
        for(final ParameterValueGroup declaredLayer : getLayers(config)){
            final String layerName = stringValue(LAYER_NAME_DESCRIPTOR, declaredLayer);
            for(GenericName n : keys){
                if(NamesExt.match(n, layerName)) continue loop;
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

    public static GenericName containsOnlyLocalPart(final Collection<GenericName> index, final GenericName layerName) {
        if (layerName != null) {
            if (NamesExt.getNamespace(layerName) == null) {
                for (GenericName name : index) {
                    if (name.tip().toString().equals(layerName.tip().toString())) {
                        return name;
                    }
                }
            }
        }
        return null;
    }

    public static GenericName containsWithNamespaceError(final Collection<GenericName> index, final GenericName layerName) {
        if (layerName != null) {
            for (GenericName name : index) {
                if (name.tip().toString().equals(layerName.tip().toString())) {
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
