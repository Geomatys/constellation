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

package org.constellation.bean;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import org.constellation.bean.MenuItem.Path;
import org.geotoolkit.gui.swing.tree.DefaultMutableTreeNode;
import org.geotoolkit.lang.Setup;
import org.geotoolkit.util.logging.Logging;
import org.mapfaces.component.outline.UIOutline;
import org.mapfaces.i18n.I18NBean;
import org.mapfaces.renderkit.html.outline.OutlineRowStyler;
import org.mapfaces.utils.FacesUtils;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class MenuBean extends I18NBean{

    private static final Logger LOGGER = Logging.getLogger(MenuBean.class);

    private static final OutlineRowStyler STYLER = new OutlineRowStyler() {
        @Override
        public String getRowStyle(TreeNode node) {
            return null;
        }
        @Override
        public String getRowClass(TreeNode node) {

            String candidate = "";
            if(node.getChildCount() > 0){
                candidate += "navigationGroupClass";
            }else{
                candidate += "navigationLeafClass";
            }

            String page = FacesContext.getCurrentInstance().getViewRoot().getViewId();
            final I18NNode n = (I18NNode) node;
            if(page != null && n.targetPage != null){
                final String sp = n.targetPage.substring(0, n.targetPage.length()-3);
                final String rp = page.substring(0, page.length()-5);
                if(sp.contains(rp)){
                    candidate += " active";
                }
            }
            
            return candidate;
        }
    };

    private final TreeModel model;

    public MenuBean() {
        final Map<String,I18NNode> nodes = new HashMap<String, I18NNode>();

        addBundle("org.constellation.bundle.base");

        final I18NNode root = new I18NNode("root",null,null,0);
        nodes.put("root", root);
        model = new DefaultTreeModel(root);

        for(final MenuItem page : MenuItems.getPages()){

            //load the extension bundle
            final String bundle = page.getResourceBundlePath();
            if(bundle != null){
                addBundle(bundle);
            }
            
            //add all pages
            for(final Path path : page.getPaths()){
                create(root, nodes, path);
            }
        }

    }

    private I18NNode create(final I18NNode root, final Map<String,I18NNode> cache, final Path path){
       
        //find the node where to insert this one
        I18NNode parent = root;
        if(path.parent != null){
            //search the cache first
            parent = cache.get(path.parent.i18nKey);
            if(parent == null){
                //not found so create it
                parent = create(root, cache, path.parent);
            }
        }

        String link = null;
        if(path.linkedPage != null){
            link = toApplicationPath(path.linkedPage);
        }

        final I18NNode node = new I18NNode(path.i18nKey, path.icon, link,path.priority);
        cache.put(path.i18nKey, node);

        //insert node based on it's priority
        parent.insert(node, getInsertIndex(parent, node));
        return node;
    }

    /**
     * change a path to an .xhtml pag to a path to .jsf prefixed with application name.
     */
    public static String toApplicationPath(final String pagePath){
        final String webapp = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
        String link = pagePath;
        link = link.replaceAll(".xhtml", ".jsf");
        link = webapp+link;
        return link;
    }

    private static int getInsertIndex(final I18NNode parent, final I18NNode candidate){

        int index = 0;
        for(int n=parent.getChildCount(); index<n; index++){
            final I18NNode child = (I18NNode) parent.getChildAt(index);
            if(child.priority < candidate.priority){
                //found the insert index
                break;
            }
        }
        return index;
    }


    public TreeModel getModel() {
        return model;
    }

    public OutlineRowStyler getStyler(){
        return STYLER;
    }

    public void navigateTo(){
        final ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        final String nodeId = context.getRequestParameterMap().get("targetPageNode");

        if(nodeId == null || nodeId.isEmpty()){
            return;
        }

        final String strRowId = nodeId.substring(nodeId.lastIndexOf(':')+1);
        final int rowId = Integer.valueOf(strRowId);

        final UIOutline outline = (UIOutline) FacesUtils.findComponentById(FacesContext.getCurrentInstance().getViewRoot(), "navTree");
        final TreeNode[] path = outline.getTreePath(rowId);

        if(path != null){
            final String targetPage = ((I18NNode)path[path.length-1]).getTargetPage();
            try {
                context.redirect(targetPage);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
        }
        
    }

    public class I18NNode extends DefaultMutableTreeNode{

        private final String i18nkey;
        private final String icon;
        private final String targetPage;
        private final int priority;

        private I18NNode(final String i18nKey, final String icon, final String targetPage, final int priority){
            this.i18nkey = i18nKey;
            this.icon = icon;
            this.targetPage = targetPage;
            this.priority = priority;
        }

        public String getTitle(){
            return getI18n().get(i18nkey);
        }

        public String getIconPath(){
            return icon;
        }

        public String getTargetPage() {
            return targetPage;
        }

    }

}
