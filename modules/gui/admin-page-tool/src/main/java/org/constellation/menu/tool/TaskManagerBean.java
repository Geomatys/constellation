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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.context.FacesContext;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import org.constellation.admin.service.ConstellationServer;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.feature.DefaultName;

import org.mapfaces.i18n.I18NBean;
import org.mapfaces.renderkit.html.outline.OutlineRowStyler;
import org.opengis.feature.type.Name;

/**
 * Returns task manager and pages to add new values.
 *
 * @author Johann Sorel (Geomatys)
 */
public class TaskManagerBean extends I18NBean{

    
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
    
    private final TreeModel taskModel;
    private TreeModel processModel;

    public TaskManagerBean(){
        addBundle("tasks.tasks");       
        
        taskModel = new DefaultTreeModel(new DefaultMutableTreeNode("root"));
        
        
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
        return taskModel;
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
                
                final DefaultMutableTreeNode n = new DefaultMutableTreeNode(name.getLocalPart());
                parent.add(n);
            }
            
            processModel = new DefaultTreeModel(root);
        }
        
        return processModel;
    }
    
    public Hints getDefaultHints(){
        return new Hints();
    }

    public List<Object> getHintKeys(){
        return new ArrayList<Object>(getDefaultHints().keySet());
    }

}
