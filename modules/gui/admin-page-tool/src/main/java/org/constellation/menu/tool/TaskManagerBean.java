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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.constellation.admin.service.ConstellationServer;
import org.constellation.configuration.StringTreeNode;

import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.util.logging.Logging;

import org.mapfaces.i18n.I18NBean;
import org.mapfaces.renderkit.html.outline.OutlineRowStyler;

import org.opengis.feature.type.Name;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptorGroup;

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
        
        mainPage = "/tasks/quartzmanager.xhtml";
        configPage = "/tasks/taskConfig.xhtml";
    }

    public String getMainPage() {
        return mainPage;
    }
    
    public void goMainPage(){
        if (mainPage != null) {
            FacesContext.getCurrentInstance().getViewRoot().setViewId(mainPage);
        }
    }

    protected ConstellationServer getServer(){
        return (ConstellationServer) FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get(SERVICE_ADMIN_KEY);
    }
    
    public TreeModel getTaskModel(){
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        final ConstellationServer server = getServer();
        if (server != null) {
            final List<StringTreeNode> values = server.tasks.listTasks().getChildren();
            for(StringTreeNode value : values){
                final DefaultMutableTreeNode n = new SeletableNode(value);
                root.add(n);
            }
        }
        return new DefaultTreeModel(root);
    }

    public TreeModel getProcessModel() {
        if(processModel == null){
            final DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
            final ConstellationServer server = getServer();
            if (server != null) {
                final List<String> values = server.tasks.listProcess().getList();
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

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
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
        
        if(id != null){
            //update task
            server.tasks.updateTask(processid.getNamespaceURI(), processid.getLocalPart(), 
                id, title, taskstep, parameters);
        }else{
            //create a task
            id = UUID.randomUUID().toString();
            server.tasks.createTask(processid.getNamespaceURI(), processid.getLocalPart(), 
                id, title, taskstep, parameters);
        }
        
    }

    public GeneralParameterValue getTaskParameters() {
        return parameters;
    }
    
    public void createTask(){
        
        //reset values
        id = null;
        parameters = null;
        taskstep = 15;
        if (configPage != null) { 
            FacesContext.getCurrentInstance().getViewRoot().setViewId(configPage);
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
            final ConstellationServer server = getServer();
            if (server != null) {
                final GeneralParameterDescriptor desc = server.tasks.getProcessDescriptor(name.getNamespaceURI(), name.getLocalPart());
                if(desc != null){
                    parameters = desc.createValue();
                }
            }
        }
        
        public void edit(){
            if(!(userObject instanceof StringTreeNode)){
                return;
            }
            
            final StringTreeNode n = (StringTreeNode) userObject;
            id = n.getProperties().get("id");            
            title = n.getProperties().get("title");            
            final String auto = n.getProperties().get("authority");
            final String code = n.getProperties().get("code");            
            processid = new DefaultName(auto, code);
            taskstep = Integer.valueOf(n.getProperties().get("step"));
            
            final ConstellationServer server = getServer();
            if (server != null) {
                final ParameterDescriptorGroup desc = (ParameterDescriptorGroup) server.tasks.getProcessDescriptor(auto,code);
                parameters = server.tasks.getTaskParameters(id,desc);
            }
            
            if (configPage != null) {
                FacesContext.getCurrentInstance().getViewRoot().setViewId(configPage);
            }
        }
        
        public void delete(){
            if (!(userObject instanceof StringTreeNode)) {
                return;
            }
            final StringTreeNode n = (StringTreeNode) userObject;
            final String nid = n.getProperties().get("id");
            final ConstellationServer server = getServer();
            if (server != null) {
                server.tasks.deleteTask(nid);
            }
        }
    
    }
    
}
