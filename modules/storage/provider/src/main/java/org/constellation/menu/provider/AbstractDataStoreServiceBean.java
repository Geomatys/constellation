/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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

package org.constellation.menu.provider;

import org.constellation.provider.configuration.ProviderParameters;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import org.constellation.provider.AbstractDataStoreProvider;
import org.constellation.provider.AbstractProviderProxy;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.Provider;
import org.geotoolkit.data.memory.ExtendedDataStore;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.parameter.Parameters;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.util.WeakPropertyChangeListener;
import org.geotoolkit.util.logging.Logging;
import org.opengis.feature.type.Name;
import org.mapfaces.i18n.I18NBean;
import org.opengis.parameter.ParameterValueGroup;

import static org.constellation.provider.configuration.ProviderParameters.*;

/**
 * Abstract Datastore service configuration bean.
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractDataStoreServiceBean extends I18NBean implements PropertyChangeListener{

    private static final Logger LOGGER = Logging.getLogger(AbstractDataStoreServiceBean.class);

    private TreeModel layersModel = null;

    public AbstractDataStoreServiceBean(){
        addBundle("org.constellation.menu.provider.overview");
        new WeakPropertyChangeListener(LayerProviderProxy.getInstance(), this);
    }

    protected abstract Class getProviderClass();

    /**
     * Build a tree model representation of all available layers.
     */
    public synchronized TreeModel getLayerModel(){
        if(layersModel == null){
            layersModel = buildModel(LayerProviderProxy.getInstance(),false);
        }
        return layersModel;
    }

    private TreeModel buildModel(final AbstractProviderProxy proxy, final boolean onlyKeys){
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode("");
        final Class providerClazz = getProviderClass();

        final Collection<Provider> providers = proxy.getProviders();
        for(final Provider provider : providers){
            if(providerClazz.isInstance(provider)){
                root.add(buildNode((AbstractDataStoreProvider) provider));
            }
        }

        return new DefaultTreeModel(root);
    }

    private DefaultMutableTreeNode buildNode(final AbstractDataStoreProvider provider){
        final DataStoreSourceNode node = new DataStoreSourceNode(provider);


        final List<String> names = new ArrayList<String>();

        //add all names from the datastore
        try {
            final ExtendedDataStore store = provider.getDataStore();
            for (String n : store.getTypeNames()) {
                if(!names.contains(n)){
                    names.add(n);
                }
            }
        } catch (DataStoreException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
        }

        //add all names from the configuration files
        final ParameterValueGroup config = provider.getSource();
        for(ParameterValueGroup layer : getLayers(config)){
            final String layerName = Parameters.stringValue(LAYER_NAME_DESCRIPTOR, layer);
            if(!names.contains(layerName)){
                names.add(layerName);
            }
        }

        //sort them
        Collections.sort(names);

        for(String name : names){
            final  TypeNode n = new TypeNode(provider,DefaultName.valueOf(name));
            node.add(n);
        }

        return node;
    }

    private void saveSettings(){
        //TODO, rely on parameters
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        layersModel = null;
    }

    public final class DataStoreSourceNode extends DefaultMutableTreeNode{

        private final AbstractDataStoreProvider provider;
        private final ParameterValueGroup config;

        public DataStoreSourceNode(final AbstractDataStoreProvider provider) {
            super(provider);
            this.provider = provider;
            this.config = provider.getSource();
        }

        public boolean isLoadAll(){
            return ProviderParameters.isLoadAll(config);
        }

        public void setLoadAll(final boolean loadAll){
            if(loadAll != isLoadAll()){
                config.parameter(SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(loadAll);
                saveSettings();
            }
        }

        public void delete(){
            //TODO
        }

        public void reload(){
            provider.reload();
        }

    }

    public final class TypeNode extends DefaultMutableTreeNode{

        private final AbstractDataStoreProvider provider;
        private final Name name;

        public TypeNode(final AbstractDataStoreProvider provider, final Name name) {
            super(name);
            this.provider = provider;
            this.name = name;
        }

        /**
         * @return true if the type is visible in the provider index.
         */
        public boolean isVisible(){
            final boolean exist = isExist();
            if(!exist){
                //type must exist to be visible
                return false;
            }

            final boolean loadAll = ProviderParameters.isLoadAll(provider.getSource());
            if(loadAll){
                //provider load everything to this one is visible
                return true;
            }

            //last case, check it is declared
            return ProviderParameters.containLayer(provider.getSource(),name.getLocalPart());
        }

        /**
         * Make the layer visible/unvisible.
         */
        public void setVisible(boolean visible){
            //TODO
        }

        /**
         * @return true if this configured layer is in the datastore
         */
        public boolean isExist(){
            try {
                final String[] types = provider.getDataStore().getTypeNames();
                for(String str : types){
                    if(DefaultName.match(name, DefaultName.valueOf(str))){
                        return true;
                    }
                }
            } catch (DataStoreException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;
        }

        public void delete(){
            //TODO
        }

    }

}
