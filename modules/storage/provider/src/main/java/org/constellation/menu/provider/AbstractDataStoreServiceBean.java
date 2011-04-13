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

import java.util.Set;
import org.constellation.provider.StyleProviderProxy;
import org.constellation.bean.MenuBean;
import org.constellation.provider.LayerProviderService;
import java.io.IOException;
import javax.faces.context.FacesContext;
import javax.faces.context.ExternalContext;
import org.constellation.provider.configuration.ProviderParameters;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import org.constellation.provider.AbstractDataStoreProvider;
import org.constellation.provider.AbstractProviderProxy;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.Provider;
import org.constellation.provider.ProviderService;
import org.constellation.provider.StyleProvider;
import org.constellation.provider.StyleProviderService;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.util.WeakPropertyChangeListener;
import org.geotoolkit.util.logging.Logging;
import org.opengis.feature.type.Name;
import org.mapfaces.i18n.I18NBean;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;


/**
 * Abstract Datastore service configuration bean.
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractDataStoreServiceBean extends I18NBean implements PropertyChangeListener{

    private static final Logger LOGGER = Logging.getLogger(AbstractDataStoreServiceBean.class);

    private final ProviderService service;
    private final String configPage;
    private final String mainPage;
    private TreeModel layersModel = null;
    private DataStoreSourceNode configuredInstance = null;
    private ParameterValueGroup configuredParams = null;
    private String newSourceName = "default";

    public AbstractDataStoreServiceBean(final ProviderService service, final String mainPage, final String configPage){
        addBundle("org.constellation.menu.provider.overview");

        this.service = service;
        this.mainPage = MenuBean.toApplicationPath(mainPage);
        this.configPage = (configPage != null) ? MenuBean.toApplicationPath(configPage) : null;

        if(service instanceof LayerProviderService){
            new WeakPropertyChangeListener(LayerProviderProxy.getInstance(), this);
        }else if(service instanceof StyleProviderService){
            new WeakPropertyChangeListener(StyleProviderProxy.getInstance(), this);
        }
    }

    protected abstract Class getProviderClass();

    /**
     * Build a tree model representation of all available layers.
     */
    public synchronized TreeModel getLayerModel(){
        if(layersModel == null){

            if(service instanceof LayerProviderService){
                layersModel = buildModel(LayerProviderProxy.getInstance(),false);
            }else if(service instanceof StyleProviderService){
                layersModel = buildModel(StyleProviderProxy.getInstance(),false);
            }

        }
        return layersModel;
    }

    private TreeModel buildModel(final AbstractProviderProxy proxy, final boolean onlyKeys){
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode("");
        final Class providerClazz = getProviderClass();

        final Collection<Provider> providers = proxy.getProviders();
        for(final Provider provider : providers){
            if(providerClazz.isInstance(provider)){
                root.add(buildNode(provider));
            }
        }

        return new DefaultTreeModel(root);
    }

    private DefaultMutableTreeNode buildNode(final Provider provider){
        final DataStoreSourceNode node = new DataStoreSourceNode(provider);

        final Set keys = provider.getKeys();

        for(Object key : keys){
            final  DefaultMutableTreeNode n = new DefaultMutableTreeNode(key);
            node.add(n);
        }

        return node;


//        final List<String> names = new ArrayList<String>();
//
//        //add all names from the datastore
//        try {
//            final ExtendedDataStore store = provider.getDataStore();
//            for (String n : store.getTypeNames()) {
//                if(!names.contains(n)){
//                    names.add(n);
//                }
//            }
//        } catch (DataStoreException ex) {
//            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
//        }
//
//        //add all names from the configuration files
//        final ParameterValueGroup config = provider.getSource();
//        for(ParameterValueGroup layer : getLayers(config)){
//            final String layerName = Parameters.stringValue(LAYER_NAME_DESCRIPTOR, layer);
//            if(!names.contains(layerName)){
//                names.add(layerName);
//            }
//        }
//
//        //sort them
//        Collections.sort(names);
//
//        for(String name : names){
//            final  TypeNode n = new TypeNode(provider,DefaultName.valueOf(name));
//            node.add(n);
//        }
//
//        return node;
    }

    ////////////////////////////////////////////////////////////////////////////
    // CREATING NEW INSTANCE ///////////////////////////////////////////////////

    /**
     * @return String : name of the new service name to create.
     */
    public String getNewSourceName() {
        return newSourceName;
    }

    /**
     * Set the name of the new service to create.
     */
    public void setNewSourceName(final String newSourceName) {
        this.newSourceName = newSourceName;
    }

    /**
     * Create a new instance of this service.
     */
    public void createSource(){
        if(newSourceName == null || newSourceName.isEmpty()){
            //unvalid name
            return;
        }

        final ParameterDescriptorGroup desc = (ParameterDescriptorGroup) service
                .getDescriptor().descriptor(ProviderParameters.SOURCE_DESCRIPTOR_NAME);
        final ParameterValueGroup params = desc.createValue();
        params.parameter(ProviderParameters.SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue(newSourceName);

        if(service instanceof LayerProviderService){
            LayerProviderProxy.getInstance().createProvider((LayerProviderService)service, params);
        }else if(service instanceof StyleProviderService){
            StyleProviderProxy.getInstance().createProvider((StyleProviderService)service, params);
        }
        
    }


    ////////////////////////////////////////////////////////////////////////////
    // CONFIGURE CURRENT INSTANCE //////////////////////////////////////////////

    /**
     * @return the main service page.
     * this is used to return from the configuration page.
     */
    public String getMainPage(){
        return mainPage;
    }

    /**
     * @return the currently configured instance.
     */
    public DataStoreSourceNode getConfiguredInstance(){
        return configuredInstance;
    }

    public ParameterValueGroup getConfiguredParameters(){
        return configuredParams;
    }

    public void saveConfiguration(){
        configuredInstance.provider.updateSource(configuredParams);
    }


    ////////////////////////////////////////////////////////////////////////////
    // EVENTS AND SUBCLASSES ///////////////////////////////////////////////////

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        layersModel = null;
    }

    public final class DataStoreSourceNode extends DefaultMutableTreeNode{

        private final Provider provider;

        public DataStoreSourceNode(final Provider provider) {
            super(provider);
            this.provider = provider;
        }

        public void delete(){
            if(provider instanceof LayerProvider){
                LayerProviderProxy.getInstance().removeProvider((LayerProvider)provider);
            }else if(provider instanceof StyleProvider){
                StyleProviderProxy.getInstance().removeProvider((StyleProvider)provider);
            }else{
                LOGGER.log(Level.WARNING, "Unexpected provider class : {0}", provider.getClass());
            }
            
        }

        public void reload(){
            provider.reload();
            layersModel = null;
        }

        /**
         * Set this instance as the currently configured one in for the property dialog.
         */
        public void config(){
            configuredInstance = this;
            configuredParams = provider.getSource().clone();

            if(configPage != null){
                final ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
                try {
                    //the session is not logged, redirect him to the authentication page
                    context.redirect(configPage);
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
            }
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
