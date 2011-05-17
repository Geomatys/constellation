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

package org.constellation.menu.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.constellation.ServiceDef.Specification;
import org.constellation.admin.service.ServiceAdministrator;
import org.constellation.bean.MenuBean;
import org.constellation.configuration.Instance;
import org.constellation.configuration.InstanceReport;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.Source;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.configuration.ProviderParameters;
import org.geotoolkit.parameter.Parameters;
import org.geotoolkit.util.ArgumentChecks;
import org.geotoolkit.util.logging.Logging;
import org.mapfaces.event.CloseEvent;
import org.mapfaces.i18n.I18NBean;
import org.mapfaces.renderkit.html.outline.OutlineCellStyler;
import org.mapfaces.renderkit.html.outline.OutlineDataModel;
import org.mapfaces.renderkit.html.outline.OutlineRowStyler;

/**
 * Abstract JSF Bean for service administration interface.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class AbstractServiceBean extends I18NBean{

    public static final OutlineRowStyler ROW_STYLER = new OutlineRowStyler() {

        @Override
        public String getRowStyle(TreeNode node) {
            final DefaultMutableTreeNode mn = (DefaultMutableTreeNode) node;
            final Object obj = mn.getUserObject();
            if(obj instanceof Source){
                return "height:34px;";
            }else if(obj instanceof SourceElement){
                int index = mn.getParent().getIndex(node);
                if(index % 2 == 0){
                    return "background-color:#DDEEFF";
                }else{
                    return "";
                }
            }else{
                return "";
            }
        }

        @Override
        public String getRowClass(TreeNode node) {
            return "";
        }
    };
    
    public static final OutlineCellStyler CELL_STYLER = new OutlineCellStyler() {

        @Override
        public String getCellStyle(TreeNode node, int rowIndex, int columnIndex) {
            if(columnIndex > 0){
                return "border-left: 1px solid #A2ACB6;";
            }else{
                return "";
            }
        }

        @Override
        public String getCellClass(TreeNode node, int rowIndex, int columnIndex) {
            return "";
        }
    };
    
    
    /**
     * When user is log in, a ServiceAdministrator object is added in the session map.
     */
    public static final String SERVICE_ADMIN_KEY = "serviceAdmin";
    
    private static final Logger LOGGER = Logging.getLogger("org.constellation.bean");

    private final Specification specification;
    private final String configPage;
    private final String mainPage;
    private String newServiceName = "default";
    private ServiceInstance configuredInstance = null;
    private Object configurationObject = null;
    private LayerContextTreeModel treemodel = null;
    private String selectedPotentialSource = null;

    public AbstractServiceBean(final Specification specification, final String mainPage, final String configPage) {
        ArgumentChecks.ensureNonNull("specification", specification);
        ArgumentChecks.ensureNonNull("main page", mainPage);
        this.specification = specification;
        this.mainPage = MenuBean.toApplicationPath(mainPage);
        this.configPage = (configPage != null) ? MenuBean.toApplicationPath(configPage) : null;
        addBundle("service.service");
    }

    public final String getSpecificationName(){
        return specification.name();
    }

    private ServiceAdministrator getServiceAdministrator(){
        return (ServiceAdministrator) FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get(SERVICE_ADMIN_KEY);
    }
    
    /**
     * @return List of all service instance of this specification.
     *      This list include both started and stopped instances.
     */
    public final TreeModel getInstances(){
        final InstanceReport report = getServiceAdministrator().listInstance(getSpecificationName());
        final List<ServiceInstance> instances = new ArrayList<ServiceInstance>();
        for(Instance instance : report.getInstances()){
            instances.add(new ServiceInstance(instance));
        }
        Collections.sort(instances);
        return new ListTreeModel(instances);
    }

    /**
     * Subclass may override this method to extend the ServiceInstance object.
     */
    protected ServiceInstance toServiceInstance(Instance inst){
        return new ServiceInstance(inst);
    }
       
    public OutlineCellStyler getCellStyler(){
        return CELL_STYLER;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // CREATING NEW INSTANCE ///////////////////////////////////////////////////

    /**
     * @return String : name of the new service name to create.
     */
    public String getNewServiceName() {
        return newServiceName;
    }

    /**
     * Set the name of the new service to create.
     */
    public void setNewServiceName(final String newServiceName) {
        this.newServiceName = newServiceName;
    }

    /**
     * Create a new instance of this service.
     */
    public void createInstance(){
        if(newServiceName == null || newServiceName.isEmpty()){
            //unvalid name
            return;
        }

        final InstanceReport report = getServiceAdministrator().listInstance(getSpecificationName());
        for(Instance instance : report.getInstances()){
            if(newServiceName.equals(instance.getName())){
                //an instance with this already exist
                return;
            }
        }

        getServiceAdministrator().newInstance(getSpecificationName(), newServiceName);
    }

    ////////////////////////////////////////////////////////////////////////////
    // ACTION TO ADD NEW SOURCE IN LAYER CONTEXT ///////////////////////////////
    
    public List<SelectItem> getPotentialSources(){
        final List<SelectItem> items = new ArrayList<SelectItem>();
        for(LayerProvider provider : LayerProviderProxy.getInstance().getProviders()){
            final String name = Parameters.stringValue(ProviderParameters.SOURCE_ID_DESCRIPTOR, provider.getSource());
            items.add(new SelectItem(name, name));
        }
        return items;
    }
    
    public String getSelectedPotentialSource(){
        return selectedPotentialSource;
    }
    
    public void setSelectedPotentialSource(String selected){
        selectedPotentialSource = selected;
    }
    
    public void addSource(){
        if(!(configurationObject instanceof LayerContext) ){
            return;
        }
        
        final LayerContext ctx = (LayerContext) configurationObject;
        final Source src = new Source();
        src.setId((selectedPotentialSource == null) ? "" : selectedPotentialSource );
        ctx.getLayers().add(src);
        
        if(configurationObject instanceof LayerContext){
            treemodel = new LayerContextTreeModel((LayerContext)configurationObject);
        }else{
            treemodel = null;
        }
        
    }
    
    public void removeSource(){
        if(!(configurationObject instanceof LayerContext) ){
            return;
        }
                
        final FacesContext context = FacesContext.getCurrentInstance();
        final ExternalContext ext = context.getExternalContext();
        final Integer index = Integer.valueOf(ext.getRequestParameterMap().get("NodeId"));
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) 
                OutlineDataModel.getNode((TreeNode)getLayerModel().getRoot(), index);
        
        getLayerModel().removeProperty(new TreePath(OutlineDataModel.getTreePath(node)));
    }
    
    
    public void changeSourceLoadAll(){
        final FacesContext context = FacesContext.getCurrentInstance();
        final ExternalContext ext = context.getExternalContext();
        final Integer index = Integer.valueOf(ext.getRequestParameterMap().get("NodeId"));
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) 
                OutlineDataModel.getNode((TreeNode)getLayerModel().getRoot(), index);
        
        final Source src = (Source) node.getUserObject();
        src.setLoadAll( !(Boolean.TRUE.equals(src.getLoadAll())) ); //flip state
        
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
    public ServiceInstance getConfiguredInstance(){
        return configuredInstance;
    }

    /**
     * @return the configuration object of the edited instance.
     * Subclass should override this method to ensure this object is never null.
     */
    public Object getConfigurationObject(){
        return configurationObject;
    }

    /**
     * Called when the configuration dialog is closed.
     */
    public void configurationClosed(final CloseEvent event){
        //reset configured instance
        configuredInstance = null;
        configurationObject = null;
    }
    
    public LayerContextTreeModel getLayerModel(){
        return treemodel;
    }
    
    
    /**
     * Save the currently edited instance.
     * Subclass should override this method to make the proper save.
     */
    public void saveConfiguration(){
        if(configuredInstance != null){
            getServiceAdministrator().configureInstance(getSpecificationName(), configuredInstance.getName(), configurationObject);
            configuredInstance.restart();
        }
    }

    public class ServiceInstance implements Comparable<ServiceInstance>{

        protected Instance instance;

        public ServiceInstance(final Instance instance) {
            this.instance = instance;
        }

        public String getName(){
            return instance.getName();
        }

        /**
         * @return URL path to the running service.
         */
        public String getPath(){
            return getServiceAdministrator().getInstanceURL(getSpecificationName(), instance.getName());
        }

        public String getStatusIcon(){
            switch(instance.getStatus()){
                case WORKING:   return "provider.smallgreen.png.mfRes";
                case ERROR:     return "provider.smallred.png.mfRes";
                default:        return "provider.smallgray.png.mfRes";
            }
        }

        /**
         * Set this instance as the currently configured one in for the property dialog.
         */
        public void config(){
            configuredInstance = this;
            configurationObject = getServiceAdministrator().getInstanceconfiguration(getSpecificationName(), instance.getName());

            if(configurationObject instanceof LayerContext){
                treemodel = new LayerContextTreeModel((LayerContext)configurationObject);
            }else{
                treemodel = null;
            }
            
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

        public void start(){
            getServiceAdministrator().startInstance(getSpecificationName(), instance.getName());
            refresh();
        }
        public void stop(){
            getServiceAdministrator().stopInstance(getSpecificationName(), instance.getName());
            refresh();
        }

        public void delete(){
            getServiceAdministrator().deleteInstance(getSpecificationName(), instance.getName());
            refresh();
        }

        public void restart(){
            getServiceAdministrator().restartInstance(getSpecificationName(), instance.getName());
            refresh();
        }

        /**
         * Refresh this instance.
         */
        private void refresh(){
            final InstanceReport report = getServiceAdministrator().listInstance(getSpecificationName());
            for(final Instance inst : report.getInstances()){
                if(instance.getName().equals(inst.getName())){
                    instance = inst;
                    return;
                }
            }
        }

        @Override
        public int compareTo(final ServiceInstance other) {
            return getName().compareTo(other.getName());
        }

    }

}
