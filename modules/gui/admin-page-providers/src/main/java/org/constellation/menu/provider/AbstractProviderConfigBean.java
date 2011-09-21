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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.constellation.admin.service.ConstellationServer;
import org.constellation.bean.HighLightRowStyler;
import org.constellation.bean.MenuBean;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ProviderReport;
import org.constellation.configuration.ProviderServiceReport;
import org.constellation.configuration.ProvidersReport;
import org.constellation.provider.configuration.ProviderParameters;

import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.parameter.Parameters;
import org.geotoolkit.referencing.IdentifiedObjects;
import org.geotoolkit.util.StringUtilities;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.util.logging.Logging;

import org.mapfaces.facelet.parametereditor.ParameterModelAdaptor;
import org.mapfaces.facelet.parametereditor.ParameterTreeModel;
import org.mapfaces.i18n.I18NBean;
import org.mapfaces.renderkit.html.outline.OutlineRowStyler;

import org.opengis.feature.type.Name;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;


/**
 * Abstract Datastore service configuration bean.
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractProviderConfigBean extends I18NBean {
    
    /**
     * When user is log in, a ServiceAdministrator object is added in the session map.
     */
    public static final String SERVICE_ADMIN_KEY = "serviceAdmin";
    
    /**
     * Model adaptor, only display the layers parameters
     */
    public static final ParameterModelAdaptor LAYERS_ADAPTOR = new ParameterModelAdaptor(){
        @Override
        public ParameterTreeModel convert(final GeneralParameterValue s) throws NonconvertibleObjectException {
            return new ParameterTreeModel(s,ProviderParameters.LAYER_DESCRIPTOR);
        }
    };
    
    protected static final Logger LOGGER = Logging.getLogger(AbstractProviderConfigBean.class);

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
    
    /**
     * Store a list of all used ids.
     * Must be refresh when a providerReport is available using method
     * refreshIds(reports);
     */
    protected final Set<String> usedIds = new HashSet<String>();
    protected final String serviceName;
    protected final String sourceConfigPage;
    protected final String itemConfigPage;
    protected final String mainPage;
    protected TreeModel layersModel = null;
    protected ProviderNode configuredInstance = null;
    private ParameterValueGroup configuredParams = null;
    private ParameterValueGroup layerParams = null;
    
    protected boolean creatingFlag = false;

    public AbstractProviderConfigBean(final String serviceName, 
            final String mainPage, final String configPage, final String layerConfigPage){
        addBundle("provider.overview");

        this.serviceName      = serviceName;
        this.mainPage         = mainPage;
        this.sourceConfigPage = configPage;
        this.itemConfigPage   = layerConfigPage;

    }

    public String getUsedIds() {
        return StringUtilities.toCommaSeparatedValues(usedIds);
    }

    protected void refreshUsedIds(final ProvidersReport reports){
        usedIds.clear();
        
        if(reports == null){
            return;
        }
        
        for(ProviderServiceReport sr : reports.getProviderServices()){
            for(ProviderReport r : sr.getProviders()){
                usedIds.add(r.getId());
            }
        }
        
    } 
    
    private GeneralParameterDescriptor getSourceDescriptor(){
        final ConstellationServer server = getServer();
        if (server != null) {
            return server.providers.getSourceDescriptor(serviceName);
        }
        return null;
    }
        
    /**
     * Build a tree model representation of all available layers.
     */
    public synchronized TreeModel getInstanceModel(){
        
        //TODO, changing constellation config path makes this cache obsolete
        //but we don't have any event system yet to handle this, so we make the query each time
        //for know.
        //if(layersModel == null){
        final ConstellationServer server = getServer();
        if (server != null) {
            final ProvidersReport report = server.providers.listProviders();
            refreshUsedIds(report);
            if (report != null) {
                layersModel = buildModel(report,false);
            } else {
                LOGGER.warning("Unable to get the provider service list.");
            }
        }
        //}
        return layersModel;
    }
    
    protected ConstellationServer getServer(){
        return (ConstellationServer) FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get(SERVICE_ADMIN_KEY);
    }
    
    public synchronized TreeModel getLayerModel(){
        if(configuredInstance == null){
            return new DefaultTreeModel(new DefaultMutableTreeNode());
        }
        
        final DefaultMutableTreeNode root = buildProviderNode(configuredInstance.provider,true);
        return new DefaultTreeModel(root);
    }

    private TreeModel buildModel(final ProvidersReport proxy, final boolean onlyKeys){
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode("");
        
        if(proxy != null){
            final ProviderServiceReport serviceReport = proxy.getProviderService(serviceName);
            if(serviceReport != null && serviceReport.getProviders() != null){
                for(final ProviderReport provider : serviceReport.getProviders()){
                    root.add(buildProviderNode(provider,false));
                }
            }
        }

        return new DefaultTreeModel(root);
    }

    private DefaultMutableTreeNode buildProviderNode(final ProviderReport provider, boolean buildChildren){
        final ProviderNode root = new ProviderNode(provider);

        if(!buildChildren){
            return root;
        }

        final ConstellationServer server = getServer();
        
        final List<String> names = new ArrayList<String>();
        if(provider != null){
            for(String str : provider.getItems()){
                names.add(DefaultName.valueOf(str).getLocalPart());
            }            
        }
        
        final List<String> sourceNames = new ArrayList<String>(names);
        
        //add all names from the configuration files
        if (server != null) {
            final ParameterDescriptorGroup serviceDesc = (ParameterDescriptorGroup)
                    server.providers.getServiceDescriptor(serviceName);        
            final ParameterDescriptorGroup sourceDesc = (ParameterDescriptorGroup)
                    serviceDesc.descriptor(ProviderParameters.SOURCE_DESCRIPTOR_NAME);
            final ParameterValueGroup config = (ParameterValueGroup)
                    server.providers.getProviderConfiguration(provider.getId(), sourceDesc);

            if (config != null) {
                for (ParameterValueGroup layer : ProviderParameters.getLayers(config)) {
                    final String layerName = Parameters.stringValue(ProviderParameters.LAYER_NAME_DESCRIPTOR, layer);
                    if (!names.contains(layerName)) {
                        names.add(layerName);
                    }
                }
            }
        }
        
        //sort them
        Collections.sort(names);

        for (String name : names) {
            final DefaultMutableTreeNode n = buildItemNode(provider, name, sourceNames);
            root.add(n);
        }
        return root;        
    }
    
    protected DefaultMutableTreeNode buildItemNode(final ProviderReport provider, 
            final String name, final List<String> sourceNames){
        final TypeNode n = new TypeNode(provider,DefaultName.valueOf(name),
                    sourceNames.contains(name));
        return n;
    }

    public OutlineRowStyler getInstanceRowStyler() {
        return ROW_STYLER;
    }

    ////////////////////////////////////////////////////////////////////////////
    // CREATING NEW INSTANCE ///////////////////////////////////////////////////

    /**
     * Create a new instance of this service.
     */
    public void createSource(){
        
        final ConstellationServer server = getServer();
        if (server != null) {
            
            final ProvidersReport report              = server.providers.listProviders();
            final ProviderServiceReport serviceReport = report.getProviderService(serviceName);
            String newSourceName = "default";
            int i = 1;
            boolean freeName = false;
            while (!freeName) {
                freeName = true;
                if (serviceReport != null && serviceReport.getProviders() != null) {
                    for (final ProviderReport p : serviceReport.getProviders()) {
                        if (p.getId().equals(newSourceName)) {
                            //an instance with this already exist
                            freeName = false;
                            newSourceName = "default" + i;
                            i++;
                            break;
                        }
                    }
                }
            }
        
            final ParameterDescriptorGroup serviceDesc = (ParameterDescriptorGroup)
                    server.providers.getServiceDescriptor(serviceName);

            final ParameterDescriptorGroup sourceDesc = (ParameterDescriptorGroup)
                    serviceDesc.descriptor(ProviderParameters.SOURCE_DESCRIPTOR_NAME);
            final ParameterValueGroup params = sourceDesc.createValue();
            params.parameter(ProviderParameters.SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue(newSourceName);

            AcknowlegementType type = server.providers.createProvider(serviceName, params);

            if (type != null) {
                FacesContext.getCurrentInstance().addMessage("Error", 
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, type.getMessage(), ""));
            }
            
            layersModel = null;
           
            configuredInstance = new ProviderNode(new ProviderReport(newSourceName, null));
            configuredParams   = params;

            if (sourceConfigPage != null) {
                creatingFlag = true;
                final MenuBean bean = getMenuBean();
                if (bean != null) {
                    bean.addToNavigationStack(newSourceName);
                }
                FacesContext.getCurrentInstance().getViewRoot().setViewId(sourceConfigPage);
            }
            
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
    
    public void goMainPage(){
        if (creatingFlag && configuredInstance != null) {
            creatingFlag = false;
            final ConstellationServer server = getServer();
            if (server != null) {
                server.providers.deleteProvider(configuredInstance.provider.getId());
            }
        }
        if (mainPage != null) {
            final MenuBean bean = getMenuBean();
            if (bean != null) {
                bean.backNavigationStack();
            }
            FacesContext.getCurrentInstance().getViewRoot().setViewId(mainPage);
        }
    }
    
    public void goMainPageFromLayer(){
        if (mainPage != null) {
            final MenuBean bean = getMenuBean();
            if (bean != null) {
                bean.backNavigationStack();
                bean.backNavigationStack();
            }
            FacesContext.getCurrentInstance().getViewRoot().setViewId(mainPage);
        }
    }

    /**
     * @return the currently configured instance.
     */
    public ProviderNode getConfiguredInstance(){
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
     * @return the source id parameter
     */
    public GeneralParameterValue getLayerNameParameter(){
        return layerParams.parameter(ProviderParameters.LAYER_NAME_DESCRIPTOR.getName().getCode());
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
    public ParameterTreeModel getLayerConfiguredParameters(){
        final List<GeneralParameterDescriptor> restrictions = new ArrayList<GeneralParameterDescriptor>();
        for (GeneralParameterDescriptor desc : layerParams.getDescriptor().descriptors()) {
            if (!desc.equals(ProviderParameters.LAYER_NAME_DESCRIPTOR)) {
                restrictions.add(desc);
            }
        }
        final ParameterTreeModel model = new ParameterTreeModel(layerParams, restrictions.toArray(new GeneralParameterDescriptor[restrictions.size()]));
        return model;
    }
    
    public void saveConfiguration(){
        final ConstellationServer server = getServer();
        if (server!= null) {
            server.providers.updateProvider(serviceName, configuredInstance.provider.getId(), configuredParams);
        }
        creatingFlag = false;
        goMainPage();
    }
    
    public void saveConfigurationFromLayer(){
        final ConstellationServer server = getServer();
        if (server!= null) {
            server.providers.updateProvider(serviceName, configuredInstance.provider.getId(), configuredParams);
        }
        goMainPageFromLayer();
    }

    public MenuBean getMenuBean() {
        final FacesContext context = FacesContext.getCurrentInstance();
        return (MenuBean) context.getApplication().evaluateExpressionGet(context, "#{menuBean}", MenuBean.class);
    }
    
    public void deleteLayer() {
        final ParameterValueGroup config = configuredParams;
        final String name = Parameters.value(ProviderParameters.LAYER_NAME_DESCRIPTOR, layerParams);
        for (GeneralParameterValue groups : config.values()) {
            if (IdentifiedObjects.nameMatches(groups.getDescriptor(), ProviderParameters.LAYER_DESCRIPTOR)) {
                final String layerName = Parameters.stringValue(ProviderParameters.LAYER_NAME_DESCRIPTOR, (ParameterValueGroup)groups);
                if (name.equals(layerName)) {
                    //we have found the layer to remove
                    config.values().remove(groups);
                    break;
                }
            }
        }
        saveConfigurationFromLayer();
    }

    ////////////////////////////////////////////////////////////////////////////
    // SUBCLASSES //////////////////////////////////////////////////////////////


    public final class ProviderNode extends DefaultMutableTreeNode{

        protected final ProviderReport provider;

        public ProviderNode(final ProviderReport provider) {
            super(provider);
            this.provider = provider;
        }
        
        public void delete(){
            final ConstellationServer server = getServer();
            if (server != null) {
                server.providers.deleteProvider(provider.getId());
            }
            layersModel = null;
            configuredInstance = null;
            configuredParams = null;
            goMainPage();
        }

        public void reload(){
            final ConstellationServer server = getServer();
            if (server != null) {
                server.providers.restartProvider(provider.getId());
            }
            layersModel = null;
            configuredInstance = null;
            configuredParams = null;
        }

        /**
         * Select this source to display layers
         */
        public void select(){
            layersModel = null;
            configuredInstance = this;
            final ConstellationServer server = getServer();
            if (server != null) {
                configuredParams = (ParameterValueGroup)server.providers.getProviderConfiguration(provider.getId(),
                    (ParameterDescriptorGroup)((ParameterDescriptorGroup)server.providers.getServiceDescriptor(serviceName))
                    .descriptor(ProviderParameters.SOURCE_DESCRIPTOR_NAME));
            }
        }
        
        /**
         * Set this instance as the currently configured one in for the property dialog.
         */
        public void config(){
            select();
            if (sourceConfigPage != null) {
                final MenuBean bean = getMenuBean();
                if (bean != null) {
                    bean.addToNavigationStack(configuredInstance.provider.getId());
                }
                FacesContext.getCurrentInstance().getViewRoot().setViewId(sourceConfigPage);
            }
        }
    }

    public final class TypeNode extends DefaultMutableTreeNode{

        private final ProviderReport provider;
        private final Name name;
        private final boolean exist;

        public TypeNode(final ProviderReport provider, final Name name, final boolean exist) {
            super(name);
            this.provider = provider;
            this.name = name;
            this.exist = exist;
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
            return exist;
        }

        public void config(){
            configuredInstance = new ProviderNode(provider);
            configuredInstance.select();

            layerParams = null;
            for (GeneralParameterValue groups : configuredParams.values()) {
                if (IdentifiedObjects.nameMatches(groups.getDescriptor(), ProviderParameters.LAYER_DESCRIPTOR)) {
                    final String layerName = Parameters.stringValue(ProviderParameters.LAYER_NAME_DESCRIPTOR, (ParameterValueGroup) groups);
                    if (DefaultName.match(name, layerName)) {
                       //we have found the layer
                        layerParams = (ParameterValueGroup) groups;   
                        break;
                    }
                }
            }
            
            if (layerParams == null) {
                //config does not exist, create it
                layerParams = configuredParams.addGroup(
                        ProviderParameters.LAYER_DESCRIPTOR.getName().getCode());
                layerParams.parameter(ProviderParameters.LAYER_NAME_DESCRIPTOR.getName().getCode())
                        .setValue(name.getLocalPart());
            }
            
            
            if (itemConfigPage != null) {
                final MenuBean bean = getMenuBean();
                if (bean != null) {
                    bean.addToNavigationStack(provider.getId());
                    bean.addToNavigationStack(name.getLocalPart());
                }
                FacesContext.getCurrentInstance().getViewRoot().setViewId(itemConfigPage);
            }
        }
        
        public void delete(){
            //add all names from the configuration files
            final ParameterValueGroup config = configuredParams;
            for (GeneralParameterValue groups : config.values()) {
            if (IdentifiedObjects.nameMatches(groups.getDescriptor(), ProviderParameters.LAYER_DESCRIPTOR)) {
                final String layerName = Parameters.stringValue(ProviderParameters.LAYER_NAME_DESCRIPTOR, (ParameterValueGroup)groups);
                if (DefaultName.match(name, layerName)) {
                    //we have found the layer to remove
                    config.values().remove(groups);
                    break;
                }
            }
        }
            
            saveConfiguration();
        }

        public void show(){

//            final ELContext elContext = FacesContext.getCurrentInstance().getELContext();
//            ProviderBean providerBean = (ProviderBean) FacesContext.getCurrentInstance().getApplication()
//                .getExpressionFactory().createValueExpression(elContext, "#{providerBean}", ProviderBean.class).getValue(elContext);
//            
//            if(providerBean == null){
//                LOGGER.log(Level.WARNING, "ProviderBean not found.");
//                return;
//            }
//            
//            //find the exact name
//            Name layerName = null;
//            for(Name str : provider.getKeys()){
//                if(DefaultName.match(name, str)){
//                    layerName = str;
//                }
//            }
//            
//            if(layerName == null){
//                LOGGER.log(Level.WARNING, "Layer not found : {0}", name);
//                return;
//            }
//            
//            final LayerDetails details = provider.getByIdentifier(layerName);
//            
//            try {
//                providerBean.getMapContext().layers().add(details.getMapLayer(null, null));
//            } catch (PortrayalException ex) {
//                LOGGER.log(Level.SEVERE, null, ex);
//            } 
                        
        }
        
    }

}
