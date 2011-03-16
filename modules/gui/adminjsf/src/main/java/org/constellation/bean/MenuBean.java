/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.bean;

import java.util.HashMap;
import java.util.Map;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import org.constellation.bean.MenuItem.Path;
import org.geotoolkit.gui.swing.tree.DefaultMutableTreeNode;
import org.mapfaces.component.outline.UIOutline;
import org.mapfaces.renderkit.html.outline.OutlineRowStyler;
import org.mapfaces.utils.FacesUtils;

/**
 *
 * @author jsorel
 */
public class MenuBean extends I18NBean{

    private static final OutlineRowStyler STYLER = new OutlineRowStyler() {
        @Override
        public String getRowStyle(TreeNode node) {
            return null;
        }
        @Override
        public String getRowClass(TreeNode node) {
            if(node.getChildCount() > 0){
                return "navigationGroupClass";
            }else{
                return "navigationLeafClass";
            }

        }
    };

    private final TreeModel model;
    private String currentPage;

    public MenuBean() {

        final Map<String,I18NNode> nodes = new HashMap<String, I18NNode>();

        addBundle("org.constellation.bundle.base");

        final I18NNode root = new I18NNode("root",null,null);
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
            final String webapp = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();            
            link = path.linkedPage;
            link = link.replaceAll(".xhtml", ".jsf");
            link = webapp+link;
        }

        final I18NNode node = new I18NNode(path.i18nKey, path.icon, link);
        cache.put(path.i18nKey, node);
        parent.add(node);
        return node;
    }


    public TreeModel getModel() {
        return model;
    }

    public OutlineRowStyler getStyler(){
        return STYLER;
    }

    public String getCurrentPage() {
        return currentPage;
    }

    public void navigateTo(){
        final String nodeId = FacesContext.getCurrentInstance().getExternalContext()
                              .getRequestParameterMap().get("targetPageNode");

        if(nodeId == null || nodeId.isEmpty()){
            return;
        }

        final String strRowId = nodeId.substring(nodeId.lastIndexOf(':')+1);
        final int rowId = Integer.valueOf(strRowId);

        final UIOutline outline = (UIOutline) FacesUtils.findComponentById(FacesContext.getCurrentInstance().getViewRoot(), "navTree");
        final TreeNode[] path = outline.getPath(rowId);

        if(path != null){
            currentPage = ((I18NNode)path[path.length-1]).getTargetPage();
        }
        
    }

    public class I18NNode extends DefaultMutableTreeNode{

        private final String i18nkey;
        private final String icon;
        private final String targetPage;

        private I18NNode(final String i18nKey, final String icon, final String targetPage){
            this.i18nkey = i18nKey;
            this.icon = icon;
            this.targetPage = targetPage;
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
