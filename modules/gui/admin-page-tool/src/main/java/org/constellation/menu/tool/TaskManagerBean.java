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

package org.constellation.menu.tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.constellation.admin.service.ConstellationServer;
import org.constellation.bean.MenuBean;

import org.geotoolkit.factory.Hints;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.util.logging.Logging;

import org.mapfaces.i18n.I18NBean;
import org.mapfaces.renderkit.html.outline.OutlineRowStyler;

import org.opengis.feature.type.Name;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Returns task manager and pages to add new values.
 *
 * @author Johann Sorel (Geomatys)
 */
public class TaskManagerBean extends I18NBean{

    private static final Logger LOGGER = Logging.getLogger(TaskManagerBean.class);
    
    /**
     * When user is log in, a ServiceAdministrator object is added in the session map.
     */
    public static final String SERVICE_ADMIN_KEY = "serviceAdmin";
    
    public static final OutlineRowStyler ROW_STYLER = new OutlineRowStyler() {

        @Override
        public String getRowStyle(final TreeNode node) {
            final DefaultMutableTreeNode mn = (DefaultMutableTreeNode) node;
            final Object obj = mn.getUserObject();
            return "";
        }

        @Override
        public String getRowClass(final TreeNode node) {
            return "";
        }
    };
    
    private final String mainPage;
    private final String configPage;
    private TreeModel processModel;

    public TaskManagerBean(){
        addBundle("tasks.tasks");       
        
        mainPage = MenuBean.toApplicationPath("/tasks/quartzmanager.xhtml");
        configPage = MenuBean.toApplicationPath("/tasks/taskConfig.xhtml");
    }

    public String getMainPage() {
        return mainPage;
    }

    protected ConstellationServer getServer(){
        final ConstellationServer server = (ConstellationServer) FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get(SERVICE_ADMIN_KEY);
        
        if(server == null){
            throw new IllegalStateException("Distant server is null.");
        }
        
        return server;
    }
    
    public TreeModel getTaskModel(){
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        
        final List<String> values = getServer().tasks.listProcess().getList();
        for(String value : values){
            final DefaultMutableTreeNode n = new DefaultMutableTreeNode(value);
            root.add(n);
        }
        
        return new DefaultTreeModel(root);
    }

    public TreeModel getProcessModel() {
        if(processModel == null){
            final DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
            final List<String> values = getServer().tasks.listProcess().getList();
            final Map<String,DefaultMutableTreeNode> authorities = new HashMap<String, DefaultMutableTreeNode>();
            
            for(String value : values){
                final Name name = DefaultName.valueOf(value);
                final String ns = name.getNamespaceURI();
                
                DefaultMutableTreeNode parent = authorities.get(ns);
                if(parent == null){
                    parent = new DefaultMutableTreeNode(ns);
                    root.add(parent);
                    authorities.put(ns, parent);
                }
                
                final DefaultMutableTreeNode n = new SeletableNode(name);
                parent.add(n);
            }
            
            processModel = new DefaultTreeModel(root);
        }
        
        return processModel;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    private String id = null;
    private String title = "";
    private int taskstep = 15;
    private GeneralParameterValue parameters = null;
    private Name processid;

    public int getTaskStep() {
        return taskstep;
    }

    public void setTaskStep(int taskstep) {
        this.taskstep = taskstep;
    }

    public Name getProcessId() {
        return processid;
    }
    
    public void saveTask() {
        final ConstellationServer server = getServer();
        if(server == null) return;
        
        server.tasks.createTask(processid.getNamespaceURI(), processid.getLocalPart(), 
                id, title, taskstep, parameters);
        
    }

    public GeneralParameterValue getTaskParameters() {
        return parameters;
    }
    
    public void createTask(){
        
        //reset values
        id = UUID.randomUUID().toString();
        parameters = null;
        taskstep = 15;
        if(configPage != null){
            final ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
            try {
                context.redirect(configPage);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Redirection to "+configPage+" failed.", ex);
            }
        }
    }
    
    
    public final class SeletableNode extends DefaultMutableTreeNode{
    
        public SeletableNode(Object uo){
            super(uo);
        }
        
        public void select(){
            if(!(userObject instanceof Name)) return;
            
            final Name name = (Name)userObject;            
            processid = name;
            final GeneralParameterDescriptor desc = getServer().tasks.getProcessDescriptor(name.getNamespaceURI(), name.getLocalPart());
            if(desc != null){
                parameters = desc.createValue();
            }
        }
    
    }
    
}
