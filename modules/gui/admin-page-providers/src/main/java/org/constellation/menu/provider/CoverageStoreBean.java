/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
import java.util.List;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import org.constellation.admin.service.ConstellationServer;
import org.constellation.bean.MenuBean;
import org.constellation.configuration.ProviderReport;
import org.constellation.configuration.ProviderServiceReport;
import org.constellation.configuration.ProvidersReport;
import org.mapfaces.i18n.I18NBean;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Coverage-Store configuration bean.
 *
 * @author Johann Sorel (Geomatys)
 */
public class CoverageStoreBean extends I18NBean{

    public static final String SERVICE_NAME = "coverage-store";
        
    private final String mainPage;
    private final String sourceConfigPage;
        
    private final EditedProvider current = new EditedProvider();
    
    public CoverageStoreBean(){
        addBundle("provider.overview");
        addBundle("provider.coverageStore");
        this.mainPage         = "/provider/coverageStore.xhtml";
        this.sourceConfigPage = "/provider/coverageStoreConfig.xhtml";
    }

    public EditedProvider getEdited() {
        return current;
    }
    
    public List<ChoiceType> getChoices(){
        final ConstellationServer server = getServer();
        final ProvidersReport report = server.providers.listProviders();
        final ProviderServiceReport service = report.getProviderService(SERVICE_NAME);
        final List<ProviderReport> providers = service.getProviders();
        
        final ParameterDescriptorGroup servicedesc = (ParameterDescriptorGroup) server.providers.getServiceDescriptor(SERVICE_NAME);
        final ParameterDescriptorGroup sourcedesc = (ParameterDescriptorGroup)servicedesc.descriptor("source");
        final ParameterDescriptorGroup choice = (ParameterDescriptorGroup) sourcedesc.descriptor("choice");
        
            
        final List<ChoiceType> choices = new ArrayList<ChoiceType>();
        for(final GeneralParameterDescriptor c : choice.descriptors()){
            final ChoiceType group = new ChoiceType();
            group.sourcedesc = sourcedesc;
            group.choicedesc = (ParameterDescriptorGroup)c;
            group.name = c.getName().getCode();
            
            final DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
            final DefaultTreeModel model = new DefaultTreeModel(root);
            group.model = model;
            
            //find providers of this type
            providerloop:
            for(final ProviderReport provider : providers){
                final String id = provider.getId();
                final ParameterValueGroup param = (ParameterValueGroup)
                        server.providers.getProviderConfiguration(id, sourcedesc);
                final ParameterValueGroup choiceparam = param.groups("choice").get(0);
                for(GeneralParameterValue candidate : choiceparam.values()){
                    if(group.name.equals(candidate.getDescriptor().getName().getCode())){
                        final DefaultMutableTreeNode node = new ProviderNode(param);
                        root.add(node);
                        continue providerloop;
                    }
                }
            }
            choices.add(group);
        }
        
        return choices;
    }
    
    public ConstellationServer getServer(){
        return (ConstellationServer) FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get(AbstractProviderConfigBean.SERVICE_ADMIN_KEY);
    }
    
    public MenuBean getMenuBean() {
        final FacesContext context = FacesContext.getCurrentInstance();
        return (MenuBean) context.getApplication().evaluateExpressionGet(context, "#{menuBean}", MenuBean.class);
    }
    
    public void gotoMainPage(){
        if (mainPage != null) {
            FacesContext.getCurrentInstance().getViewRoot().setViewId(mainPage);
        }
    }
    
    public void gotoEditPage(){
        if (sourceConfigPage != null) {
            FacesContext.getCurrentInstance().getViewRoot().setViewId(sourceConfigPage);
        }
    }
    
    public void update(){
        final FacesContext context = FacesContext.getCurrentInstance();
        final ExternalContext ext = context.getExternalContext();
        final String id = ext.getRequestParameterMap().get("PROVIDER_ID");
        final ConstellationServer server = getServer();
        final ParameterDescriptorGroup desc = (ParameterDescriptorGroup) server.providers.getSourceDescriptor(SERVICE_NAME);
        final ParameterValueGroup param = (ParameterValueGroup) server.providers.getProviderConfiguration(id, desc);
        
        current.setToUpdate(param);
        gotoEditPage();
    }
    
    public void delete(){
        final FacesContext context = FacesContext.getCurrentInstance();
        final ExternalContext ext = context.getExternalContext();
        final String id = ext.getRequestParameterMap().get("PROVIDER_ID");
        
        final ConstellationServer server = getServer();
        server.providers.deleteProvider(id);
    }

    public void reload(){
        final FacesContext context = FacesContext.getCurrentInstance();
        final ExternalContext ext = context.getExternalContext();
        final String id = ext.getRequestParameterMap().get("PROVIDER_ID");
        
        final ConstellationServer server = getServer();
        server.providers.restartProvider(id);
    }
    
    
    public class ChoiceType{
        
        private String name;
        private ParameterDescriptorGroup sourcedesc;
        private ParameterDescriptorGroup choicedesc;
        private DefaultTreeModel model;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public DefaultTreeModel getModel() {
            return model;
        }
        
        public void newInstance(){
            current.setToNew(sourcedesc,choicedesc);
            gotoEditPage();
        }
                
    }
    
    public class ProviderNode extends DefaultMutableTreeNode{
        
        public ProviderNode(ParameterValueGroup param){
            super(param);
        }
        
        public String getId(){
            return getIdParameter().stringValue();
        }
        
        public ParameterValue getIdParameter(){
            final ParameterValueGroup param = getUserObject();
            return param.parameter("id");
        }
                
        @Override
        public ParameterValueGroup getUserObject() {
            return (ParameterValueGroup) super.getUserObject();
        }
        
    }
    
    public class EditedProvider{
        
        private boolean newInstance = false;
        private String errorMessage = "";        
        private String originalId = "";
        private ParameterDescriptorGroup sourcedesc;
        private ParameterDescriptorGroup choicedesc;
        private ParameterValueGroup sourcevalue;
        private ParameterValueGroup choicevalue;
        
        
        public String getId(){
            return getIdParameter().stringValue();
        }
        
        public ParameterValue getIdParameter(){
            return sourcevalue.parameter("id");
        }
        
        public ParameterValueGroup getChoiceParameter(){
            return choicevalue;
        }
        
        /**
         * Prepare for a new instance
         */
        private void setToNew(ParameterDescriptorGroup sourcedesc, ParameterDescriptorGroup choicedesc){
            this.newInstance = true;
            this.errorMessage = "";
            this.originalId = null; 
            this.sourcedesc = sourcedesc;
            this.choicedesc = choicedesc;
            this.sourcevalue = sourcedesc.createValue();
            final ParameterValueGroup choiceparam = sourcevalue.groups("choice").get(0);
            this.choicevalue = choiceparam.addGroup(choicedesc.getName().getCode());
        }
        
        /**
         * Prepare for an instance update
         */
        private void setToUpdate(ParameterValueGroup sourcevalue){
            this.newInstance = false;
            this.errorMessage = "";
            //keep the original id in case it is updated
            originalId = sourcevalue.parameter("id").stringValue(); 
            this.sourcedesc = sourcevalue.getDescriptor();
            this.sourcevalue = sourcevalue;
            
            final ParameterValueGroup choiceparam = sourcevalue.groups("choice").get(0);
            for(GeneralParameterValue candidate : choiceparam.values()){
                this.choicevalue = (ParameterValueGroup) candidate;
                this.choicedesc = choicevalue.getDescriptor();
            }
        }
        
        public String getErrorMessage(){
            return errorMessage;
        }
        
        public void apply(){
            final ConstellationServer server = getServer();
            
            //check no other provider have this id
            final String id = getId();
            if(id == null || id.isEmpty()){
                errorMessage = "Id not configured.";
                return;
            }
            
            //check only if id has change in case it's an update
            if(originalId == null || !id.equalsIgnoreCase(originalId)){
                final List<ProviderServiceReport> reports = server.providers.listProviders().getProviderServices();
                for(ProviderServiceReport report : reports){
                    for(ProviderReport rep : report.getProviders()){
                        if(id.equalsIgnoreCase(rep.getId())){
                            errorMessage = "Id already used.";
                            return;
                        }
                    }
                }
            }
            
            if(newInstance){
                //create a new provider
                server.providers.createProvider(SERVICE_NAME, sourcevalue);
            }else{
                //update an existing provider
                server.providers.updateProvider(SERVICE_NAME, originalId, sourcevalue);
            }
            
            //everything went well, get back to main page
            gotoMainPage();
        }
        
    }
    
}
