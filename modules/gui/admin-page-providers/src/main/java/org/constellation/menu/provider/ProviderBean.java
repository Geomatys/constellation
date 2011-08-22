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

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import javax.faces.context.FacesContext;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import org.constellation.admin.service.ConstellationServer;
import org.constellation.configuration.ProviderReport;
import org.constellation.configuration.ProviderServiceReport;
import org.constellation.configuration.ProvidersReport;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.mapfaces.i18n.I18NBean;

/**
 * Returns several information from the used GeotoolKit.
 *
 * @author Johann Sorel (Geomatys)
 */
public class ProviderBean extends I18NBean {

    private final MapContext context = MapBuilder.createContext();
    private TreeModel stylesModel = null;

    public ProviderBean(){
        addBundle("provider.overview");
    }

    private static ConstellationServer getServer(){
        return (ConstellationServer) FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get(AbstractProviderConfigBean.SERVICE_ADMIN_KEY);
    }
    
    public void reloadLayerProviders() {
        final ConstellationServer server = getServer();
        if (server != null) {
            server.providers.restartAllLayerProviders();
        }
    }

    public void reloadStyleProviders() {
        final ConstellationServer server = getServer();
        if (server != null) {
            server.providers.restartAllStyleProviders();
        }
    }

    public void show(){
//
//        final ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
//        final String nodeId = context.getRequestParameterMap().get("currentNodeId");
//
//        if(nodeId == null || nodeId.isEmpty()){
//            return;
//        }
//
//
//        final String strRowId = nodeId.substring(nodeId.lastIndexOf(':')+1);
//        final int rowId = Integer.valueOf(strRowId);
//
//        final UIOutline outline = (UIOutline) FacesUtils.findComponentById(FacesContext.getCurrentInstance().getViewRoot(), "layerPreview");
//        final TreeNode[] path = outline.getTreePath(rowId);
//
//        if(path != null){
//            final DefaultMutableTreeNode node = ((DefaultMutableTreeNode)path[path.length-1]);
//            final LayerDetails details = (LayerDetails) node.getUserObject();
//            try {
//                this.context.layers().add(details.getMapLayer(null, null));
//            } catch (PortrayalException ex) {
//                Logger.getLogger(ProviderBean.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }        
    }

    public MapContext getMapContext() {
        return context;
    }

    /**
     * Build a tree model representation of all available layers.
     */
    public synchronized TreeModel getLayerModel(){
        return buildModel(false,false);
    }

    public synchronized TreeModel getStyleModel(){
        return buildModel(true,false);
    }

    private static TreeModel buildModel(final boolean styleServices, final boolean onlyKeys){
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode("");

        final ConstellationServer server = getServer();
        if (server != null) {
            final ProvidersReport report = server.providers.listProviders();


            final Set<ProviderServiceReport> map = new TreeSet<ProviderServiceReport>(new Comparator<ProviderServiceReport>(){

                @Override
                public int compare(ProviderServiceReport o1, ProviderServiceReport o2) {
                    return o1.getType().compareTo(o2.getType());
                }

            });
            map.addAll(report.getProviderServices());

            for(final ProviderServiceReport service : map){
                if(service.isStyleService() != styleServices){
                    continue;
                }

                final DefaultMutableTreeNode n = new DefaultMutableTreeNode(service.getType());
                root.add(n);

                for(final ProviderReport pr : service.getProviders()){
                    final DefaultMutableTreeNode lpn = new DefaultMutableTreeNode(pr.getId());
                    n.add(lpn);

                    for(String layer : pr.getItems()){
                        final DefaultMutableTreeNode ldn = new DefaultMutableTreeNode(layer);
                        lpn.add(ldn);
                    }

                }

            }
        }
        return new DefaultTreeModel(root);
    }

}
