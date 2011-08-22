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

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.swing.tree.DefaultMutableTreeNode;
import org.constellation.admin.service.ConstellationServer;
import org.constellation.configuration.ProviderReport;
import org.constellation.configuration.ProviderServiceReport;
import org.constellation.configuration.ProvidersReport;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.StyleConstants;
import org.mapfaces.facelet.styleeditor.StyleEditionConstants;


/**
 * Abstract Datastore service configuration bean.
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractStyleServiceBean extends AbstractProviderConfigBean {
        
    private MutableStyle editedStyle = null;
    private SLDNode editedSLDNode = null;

    public AbstractStyleServiceBean(final String serviceName, final String mainPage, 
            final String configPage, final String sldConfig){
        super(serviceName,mainPage,configPage,sldConfig);
    }

    @Override
    protected DefaultMutableTreeNode buildItemNode(ProviderReport provider, String name, List<String> sourceNames) {
        final SLDNode n = new SLDNode(provider,name);
        return n;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // CONFIGURE SLD INSTANCE //////////////////////////////////////////////////

    private String newStyleName = "newStyle";

    public String getNewStyleName() {
        return newStyleName;
    }

    public void setNewStyleName(final String newStyleName) {
        this.newStyleName = newStyleName;
    }
    
    public void createNewStyle(){
        final MutableStyle style = StyleEditionConstants.SF.style(StyleConstants.DEFAULT_LINE_SYMBOLIZER);
        style.setName(newStyleName);
        style.setDescription(StyleEditionConstants.SF.description(newStyleName, newStyleName));
        style.featureTypeStyles().get(0).setDescription(StyleConstants.DEFAULT_DESCRIPTION);
        style.featureTypeStyles().get(0).rules().get(0).setDescription(StyleConstants.DEFAULT_DESCRIPTION);
        
        final ConstellationServer server = getServer();
        if (server != null) {
            server.providers.createStyle(configuredInstance.provider.getId(), newStyleName, style);
        
            //update the provider report
            final ProvidersReport reports = server.providers.listProviders();
            refreshUsedIds(reports);
            final ProviderServiceReport serviceReport = reports.getProviderService(this.serviceName);
            if(serviceReport != null){
                final ProviderReport report = serviceReport.getProvider(configuredInstance.provider.getId());
                configuredInstance = new ProviderNode(report);
            }
        }
        
        layersModel = null;
    }
    
    public MutableStyle getEditedSLD() {
        return editedStyle;
    }
    
    public void saveEditedSLD(){
        final ConstellationServer server = getServer();
        if (server != null) {
            server.providers.updateStyle(editedSLDNode.provider.getId(), editedSLDNode.key, editedStyle);
        }
        layersModel = null;
        
        //TODO, update service to allow renaming
//        final String newName = editedSLDNode.getUserObject().toString();
//        if(!newName.equals(editedSLDNode.key)){
//            //name changed
//            editedSLDNode.provider.rename(editedSLDNode.key, newName);
//            editedSLDNode.key = newName;
//        }
        
    }

    public SLDNode getEditedSLDNode() {
        return editedSLDNode;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // SUBCLASSES //////////////////////////////////////////////////////////////

    
    public final class SLDNode extends DefaultMutableTreeNode{

        private final ProviderReport provider;
        private String key;

        public SLDNode(final ProviderReport provider, final String key) {
            super(key);
            this.provider = provider;
            this.key = key;
        }
        
        public void config(){
            configuredInstance = new ProviderNode(provider);
            configuredInstance.select();

            final ConstellationServer server = getServer();
            if (server != null) {
                editedStyle = server.providers.downloadStyle(provider.getId(), key);
                editedSLDNode = this;

                if(itemConfigPage != null){
                    final ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
                    try {
                        context.redirect(itemConfigPage);
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING, null, ex);
                    }
                }
            }
        }
        
        public void delete(){
            final ConstellationServer server = getServer();
            if (server != null) {
                server.providers.deleteStyle(provider.getId(), key);
            
                //update the provider report
                final ProvidersReport reports = server.providers.listProviders();            
                refreshUsedIds(reports);
                final ProviderServiceReport serviceReport = reports.getProviderService(AbstractStyleServiceBean.this.serviceName);
                if(serviceReport != null){
                    final ProviderReport report = serviceReport.getProvider(configuredInstance.provider.getId());
                    configuredInstance = new ProviderNode(report);
                }
            }
            
            layersModel = null;
        }
                
    }

}
