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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import org.constellation.bean.HighLightRowStyler;
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
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.util.logging.Logging;
import org.mapfaces.facelet.parametereditor.ParameterModelAdaptor;
import org.mapfaces.facelet.parametereditor.ParameterTreeModel;
import org.opengis.feature.type.Name;
import org.mapfaces.i18n.I18NBean;
import org.mapfaces.renderkit.html.outline.OutlineRowStyler;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;


/**
 * Abstract Datastore service configuration bean.
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractStyleServiceBean extends I18NBean implements PropertyChangeListener{
    
    /**
     * Model adaptor, only display the layers parameters
     */
    public static final ParameterModelAdaptor LAYERS_ADAPTOR = new ParameterModelAdaptor(){
        @Override
        public ParameterTreeModel convert(final GeneralParameterValue s) throws NonconvertibleObjectException {
            return new ParameterTreeModel(s,ProviderParameters.LAYER_DESCRIPTOR);
        }
    };
    
    private static final Logger LOGGER = Logging.getLogger(AbstractDataStoreServiceBean.class);

    private final OutlineRowStyler ROW_STYLER = new HighLightRowStyler() {
        
        @Override
        public String getRowClass(TreeNode node) {
            String candidate = super.getRowClass(node);
       
            if(node.equals(configuredInstance)){
                candidate += " active";
            }
            
            return candidate;
        }
    };
    
    private final ProviderService service;
    private final String configPage;
    private final String mainPage;
    private TreeModel layersModel = null;
    private DataStoreSourceNode configuredInstance = null;
    private ParameterValueGroup configuredParams = null;
    private String newSourceName = "default";

    public AbstractStyleServiceBean(final ProviderService service, final String mainPage, final String configPage){
        addBundle("provider.overview");

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

    protected abstract GeneralParameterDescriptor getSourceDescriptor();
        
    /**
     * Build a tree model representation of all available layers.
     */
    public synchronized TreeModel getInstanceModel(){
        if(layersModel == null){

            if(service instanceof LayerProviderService){
                layersModel = buildModel(LayerProviderProxy.getInstance(),false);
            }else if(service instanceof StyleProviderService){
                layersModel = buildModel(StyleProviderProxy.getInstance(),false);
            }

        }
        return layersModel;
    }
    
    public synchronized TreeModel getLayerModel(){
        if(configuredInstance == null){
            return new DefaultTreeModel(new DefaultMutableTreeNode());
        }
        
        final DefaultMutableTreeNode root = buildNode(configuredInstance.provider,true);
        return new DefaultTreeModel(root);
    }

    private TreeModel buildModel(final AbstractProviderProxy proxy, final boolean onlyKeys){
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode("");
        final Class providerClazz = getProviderClass();

        final Collection<Provider> providers = proxy.getProviders();
        for(final Provider provider : providers){
            if(providerClazz.isInstance(provider)){
                root.add(buildNode(provider,false));
            }
        }

        return new DefaultTreeModel(root);
    }

    private DefaultMutableTreeNode buildNode(final Provider provider, boolean buildChildren){
        final DataStoreSourceNode node = new DataStoreSourceNode(provider);

        if(buildChildren){
            final Set keys = provider.getKeys();

            for(Object key : keys){
                final  DefaultMutableTreeNode n = new DefaultMutableTreeNode(key);
                node.add(n);
            }
        }

        return node;        
    }

    public OutlineRowStyler getInstanceRowStyler() {
        return ROW_STYLER;
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

    /**
     * @return complete source configuration
     */
    public ParameterValueGroup getConfiguredParameters(){
        return configuredParams;
    }
    
    /**
     * @return the source id parameter
     */
    public GeneralParameterValue getIdParameter(){
        return configuredParams.parameter(ProviderParameters.SOURCE_ID_DESCRIPTOR.getName().getCode());
    }
    
    /**
     * @return the source store parameters
     */
    public GeneralParameterValue getSourceParameters(){
        for(GeneralParameterValue val : configuredParams.values()){
            if(val.getDescriptor().equals(getSourceDescriptor())){
                return val;
            }
        }
        return null;
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
            
            if(configuredInstance == DataStoreSourceNode.this){
                configuredInstance = null;
                configuredParams = null;
            }
            
        }

        public void reload(){
            provider.reload();
            layersModel = null;
        }

        /**
         * Select this source to display layers
         */
        public void select(){
            configuredInstance = this;
            configuredParams = provider.getSource().clone();
        }
        
        /**
         * Set this instance as the currently configured one in for the property dialog.
         */
        public void config(){
            select();

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

}
