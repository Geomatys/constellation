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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.el.ELContext;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import org.constellation.bean.HighLightRowStyler;
import org.constellation.provider.AbstractLayerProvider;
import org.constellation.provider.AbstractProviderProxy;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.Provider;
import org.constellation.provider.ProviderService;
import org.constellation.provider.StyleProvider;
import org.constellation.provider.StyleProviderService;
import org.geotoolkit.display.exception.PortrayalException;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.parameter.Parameters;
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
public abstract class AbstractDataStoreServiceBean extends I18NBean implements PropertyChangeListener{
    
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
    private final String sourceConfigPage;
    private final String layerConfigPage;
    private final String mainPage;
    private TreeModel layersModel = null;
    private DataStoreSourceNode configuredInstance = null;
    private ParameterValueGroup configuredParams = null;
    private ParameterValueGroup layerParams = null;
    private String newSourceName = "default";

    public AbstractDataStoreServiceBean(final ProviderService service, 
            final String mainPage, final String configPage, final String layerConfigPage){
        addBundle("provider.overview");

        this.service = service;
        this.mainPage = MenuBean.toApplicationPath(mainPage);
        this.sourceConfigPage = (configPage != null) ? MenuBean.toApplicationPath(configPage) : null;
        this.layerConfigPage = (layerConfigPage != null) ? MenuBean.toApplicationPath(layerConfigPage) : null;

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
        
        final DefaultMutableTreeNode root = buildNode((AbstractLayerProvider)configuredInstance.provider,true);
        return new DefaultTreeModel(root);
    }

    private TreeModel buildModel(final AbstractProviderProxy proxy, final boolean onlyKeys){
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode("");
        final Class providerClazz = getProviderClass();

        final Collection<Provider> providers = proxy.getProviders();
        for(final Provider provider : providers){
            if(providerClazz.isInstance(provider)){
                root.add(buildNode((AbstractLayerProvider)provider,false));
            }
        }

        return new DefaultTreeModel(root);
    }

    private DefaultMutableTreeNode buildNode(final AbstractLayerProvider provider, boolean buildChildren){
        final DataStoreSourceNode root = new DataStoreSourceNode(provider);

        if(!buildChildren){
            return root;
        }

        final List<String> names = new ArrayList<String>();

        //add all names from the datastore
        for (Name n : provider.getKeys()) {
            names.add(n.getLocalPart());
        }

        //add all names from the configuration files
        final ParameterValueGroup config = provider.getSource();
        for(ParameterValueGroup layer : ProviderParameters.getLayers(config)){
            final String layerName = Parameters.stringValue(ProviderParameters.LAYER_NAME_DESCRIPTOR, layer);
            if(!names.contains(layerName)){
                names.add(layerName);
            }
        }

        //sort them
        Collections.sort(names);

        for(String name : names){
            final TypeNode n = new TypeNode(provider,DefaultName.valueOf(name));
            root.add(n);
        }

        return root;
        
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
    
    /**
     * 
     * @return the layer parameters
     */
    public GeneralParameterValue getLayerConfiguredParameters(){
        return layerParams;
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

            if(sourceConfigPage != null){
                final ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
                try {
                    context.redirect(sourceConfigPage);
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
            }
        }

    }

    public final class TypeNode extends DefaultMutableTreeNode{

        private final AbstractLayerProvider provider;
        private final Name name;

        public TypeNode(final AbstractLayerProvider provider, final Name name) {
            super(name);
            this.provider = provider;
            this.name = name;
        }

        public String getStatusIcon(){
            if(isExist()){
                return "provider.smallgreen.png.mfRes";
            }else{
                return "provider.smallred.png.mfRes";
            }
        }
        
        /**
         * @return true if this configured layer is in the datastore
         */
        public boolean isExist(){
            for(Name str : provider.getKeys()){
                if(DefaultName.match(name, str)){
                    return true;
                }
            }
            return false;
        }

        public void config(){
            configuredInstance = new DataStoreSourceNode(provider);
            configuredParams = provider.getSource().clone();

            layerParams = null;
            for(ParameterValueGroup layer : ProviderParameters.getLayers(configuredParams)){
                final String layerName = Parameters.stringValue(ProviderParameters.LAYER_NAME_DESCRIPTOR, layer);
                if(DefaultName.match(name, layerName)){
                    //we have found the layer
                    layerParams = layer;                
                    break;
                }
            }
            
            if(layerParams == null){
                //config does not exist, create it
                layerParams = configuredParams.addGroup(
                        ProviderParameters.LAYER_DESCRIPTOR.getName().getCode());
                layerParams.parameter(ProviderParameters.LAYER_NAME_DESCRIPTOR.getName().getCode())
                        .setValue(name.getLocalPart());
            }
            
            
            if(layerConfigPage != null){
                final ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
                try {
                    context.redirect(layerConfigPage);
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
            }
        }
        
        public void delete(){
            //add all names from the configuration files
            final ParameterValueGroup config = configuredParams;
            for(ParameterValueGroup layer : ProviderParameters.getLayers(config)){
                final String layerName = Parameters.stringValue(ProviderParameters.LAYER_NAME_DESCRIPTOR, layer);
                if(DefaultName.match(name, layerName)){
                    //we have found the layer to remove
                    config.values().remove(layer);                    
                    break;
                }
            }
            
            saveConfiguration();
        }

        public void show(){

            final ELContext elContext = FacesContext.getCurrentInstance().getELContext();
            ProviderBean providerBean = (ProviderBean) FacesContext.getCurrentInstance().getApplication()
                .getExpressionFactory().createValueExpression(elContext, "#{providerBean}", ProviderBean.class).getValue(elContext);
            
            if(providerBean == null){
                LOGGER.log(Level.WARNING, "ProviderBean not found.");
                return;
            }
            
            //find the exact name
            Name layerName = null;
            for(Name str : provider.getKeys()){
                if(DefaultName.match(name, str)){
                    layerName = str;
                }
            }
            
            if(layerName == null){
                LOGGER.log(Level.WARNING, "Layer not found : {0}", name);
                return;
            }
            
            final LayerDetails details = provider.getByIdentifier(layerName);
            
            try {
                providerBean.getMapContext().layers().add(details.getMapLayer(null, null));
            } catch (PortrayalException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } 
                        
        }
        
    }

}
