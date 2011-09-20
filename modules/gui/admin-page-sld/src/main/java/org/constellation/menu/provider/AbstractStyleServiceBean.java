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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.bind.JAXBException;
import org.constellation.admin.service.ConstellationServer;
import org.constellation.bean.MenuBean;
import org.constellation.configuration.ProviderReport;
import org.constellation.configuration.ProviderServiceReport;
import org.constellation.configuration.ProvidersReport;
import org.geotoolkit.sld.xml.Specification.StyledLayerDescriptor;
import org.geotoolkit.sld.xml.Specification.SymbologyEncoding;
import org.geotoolkit.sld.xml.XMLUtilities;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.StyleConstants;
import org.mapfaces.facelet.styleeditor.StyleEditionConstants;
import org.opengis.util.FactoryException;


/**
 * Abstract Datastore service configuration bean.
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractStyleServiceBean extends AbstractProviderConfigBean {
        
    private MutableStyle editedStyle = null;
    private SLDNode editedSLDNode = null;

    private String xmlSLD = "";
    
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

    
    public void createNewStyle(){
        
        final ConstellationServer server = getServer();
        if (server != null) {
            String newStyleName = "newStyle";
            int i = 1;
            boolean freeName = false;
            while (!freeName) {
                freeName = true;
                for (final String p : configuredInstance.provider.getItems()) {
                    if (p.equals(newStyleName)) {
                        //an instance with this already exist
                        freeName = false;
                        newStyleName = "newStyle" + i;
                        i++;
                        break;
                    }
                }
            }

            final MutableStyle style = StyleEditionConstants.SF.style(StyleConstants.DEFAULT_LINE_SYMBOLIZER);
            style.setName(newStyleName);
            style.setDescription(StyleEditionConstants.SF.description(newStyleName, newStyleName));
            style.featureTypeStyles().get(0).setDescription(StyleConstants.DEFAULT_DESCRIPTION);
            style.featureTypeStyles().get(0).rules().get(0).setDescription(StyleConstants.DEFAULT_DESCRIPTION);
        
        
            server.providers.createStyle(configuredInstance.provider.getId(), newStyleName, style);
        
            //update the provider report
            final ProvidersReport reports = server.providers.listProviders();
            refreshUsedIds(reports);
            final ProviderServiceReport serviceReport = reports.getProviderService(this.serviceName);
            if (serviceReport != null) {
                final ProviderReport report = serviceReport.getProvider(configuredInstance.provider.getId());
                configuredInstance = new ProviderNode(report);
                configuredInstance.select();

            
                editedStyle = server.providers.downloadStyle(configuredInstance.provider.getId(), newStyleName);
                editedSLDNode = new SLDNode(report, newStyleName);

                // marshall the current Style into the xmlSLD area
                xmlSLD = readXmlSLD(editedStyle);
                
                if (itemConfigPage != null) {
                    creatingFlag = true;
                    final MenuBean bean = getMenuBean();
                    if (bean != null) {
                        bean.addToNavigationStack(newStyleName);
                    }
                    FacesContext.getCurrentInstance().getViewRoot().setViewId(itemConfigPage);
                }
            }
        }
        
        layersModel = null;
    }
    
    private String readXmlSLD(final MutableStyle style) {
        final XMLUtilities util = new XMLUtilities();
        try {
            final StringWriter sw = new StringWriter();
            util.writeStyle(sw, style, StyledLayerDescriptor.V_1_1_0);
            return sw.toString();
        } catch (JAXBException ex) {
            LOGGER.log(Level.WARNING, "JAXBException while marshalling Style.", ex);
        }
        return null;
    }
    
    private MutableStyle writeXmlSLD(final String xmlStyle) {
        final XMLUtilities util = new XMLUtilities();
        try {
            final StringWriter sw = new StringWriter();
            return util.readStyle(new StringReader(xmlStyle), SymbologyEncoding.V_1_1_0);
            
        } catch (FactoryException ex) {
            LOGGER.log(Level.WARNING, "FactoryException while unmarshalling Style.", ex);
        } catch (JAXBException ex) {
            LOGGER.log(Level.WARNING, "JAXBException while unmarshalling Style.", ex);
        }
        return null;
    }
    
    public MutableStyle getEditedSLD() {
        return editedStyle;
    }
    
    public void saveEditedSLD(){
        final ConstellationServer server = getServer();
        if (server != null) {
            server.providers.updateStyle(editedSLDNode.provider.getId(), editedSLDNode.key, editedStyle);
        }
        creatingFlag = false;
        layersModel = null;
        goMainPageStyle();
        
        //TODO, update service to allow renaming
//        final String newName = editedSLDNode.getUserObject().toString();
//        if(!newName.equals(editedSLDNode.key)){
//            //name changed
//            editedSLDNode.provider.rename(editedSLDNode.key, newName);
//            editedSLDNode.key = newName;
//        }
        
    }

    public void saveXmlSLD(){
        final ConstellationServer server = getServer();
        if (server != null) {
            final MutableStyle xmlStyle = writeXmlSLD(xmlSLD);
            server.providers.updateStyle(editedSLDNode.provider.getId(), editedSLDNode.key, xmlStyle);
        }
        creatingFlag = false;
        layersModel = null;
        goMainPageStyle();
        
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
    
    /**
     * @return the xmlSLD
     */
    public String getXmlSLD() {
        return xmlSLD;
    }

    /**
     * @param xmlSLD the xmlSLD to set
     */
    public void setXmlSLD(String xmlSLD) {
        this.xmlSLD = xmlSLD;
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
                editedStyle   = server.providers.downloadStyle(provider.getId(), key);
                editedSLDNode = this;
                xmlSLD        = readXmlSLD(editedStyle);

                if (itemConfigPage != null) {
                    final MenuBean bean = getMenuBean();
                    if (bean != null) {
                        bean.addToNavigationStack(key);
                    }
                    FacesContext.getCurrentInstance().getViewRoot().setViewId(itemConfigPage);
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
                    goMainPageStyle();
                }
            }
            
            layersModel = null;
        }
    }

    public void goMainPageStyle(){
        if (creatingFlag && configuredInstance != null) {
            creatingFlag = false;
            final ConstellationServer server = getServer();
            if (server != null) {
                server.providers.deleteStyle(configuredInstance.provider.getId(), editedSLDNode.key);
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
}
