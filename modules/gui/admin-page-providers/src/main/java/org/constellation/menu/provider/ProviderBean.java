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

import javax.swing.tree.TreeModel;
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
    private TreeModel layersModel = null;
    private TreeModel stylesModel = null;

    public ProviderBean(){
        addBundle("provider.overview");
    }

    public void reloadLayerProviders() {
//        LayerProviderProxy.getInstance().reload();
    }

    public void reloadStyleProviders() {
//        StyleProviderProxy.getInstance().reload();
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
        return null;
//        if(layersModel == null){
//            layersModel = buildModel(LayerProviderProxy.getInstance(),false);
//        }
//        return layersModel;
    }

    public synchronized TreeModel getStyleModel(){
        return null;
//        if(stylesModel == null){
//            stylesModel = buildModel(StyleProviderProxy.getInstance(),true);
//        }
//        return stylesModel;
    }

//    private static TreeModel buildModel(final AbstractProviderProxy proxy, final boolean onlyKeys){
//        final DefaultMutableTreeNode root = new DefaultMutableTreeNode("");
//
//        final Map<ProviderService,List<Provider>> map = new TreeMap<ProviderService, List<Provider>>(new Comparator<ProviderService>(){
//
//            @Override
//            public int compare(ProviderService o1, ProviderService o2) {
//                return o1.getName().compareTo(o2.getName());
//            }
//
//        });
//        final Collection<ProviderService> services = proxy.getServices();
//        for(ProviderService service : services){
//            map.put(service, new ArrayList<Provider>());
//        }
//
//        final Collection<Provider> providers = proxy.getProviders();
//        for(final Provider provider : providers){
//            final ProviderService service = provider.getService();
//            map.get(service).add(provider);
//        }
//
//        for(final Map.Entry<ProviderService,List<Provider>> entry : map.entrySet()){
//            final DefaultMutableTreeNode n = new DefaultMutableTreeNode(entry.getKey());
//            root.add(n);
//
//            for(final Provider lp : entry.getValue()){
//                final DefaultMutableTreeNode lpn = new DefaultMutableTreeNode(lp);
//                n.add(lpn);
//
//                for(Object key : lp.getKeys()){
//                    final DefaultMutableTreeNode ldn = new DefaultMutableTreeNode(onlyKeys?key:lp.get(key));
//                    lpn.add(ldn);
//                }
//
//            }
//
//        }
//
//        return new DefaultTreeModel(root);
//    }

}
