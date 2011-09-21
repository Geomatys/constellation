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

package org.constellation.menu.service;

import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.constellation.admin.service.ConstellationServer;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.ProviderReport;
import org.constellation.configuration.ProviderServiceReport;
import org.constellation.configuration.ProvidersReport;
import org.constellation.configuration.Source;
import org.geotoolkit.gui.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class LayerContextTreeModel extends DefaultTreeModel{

    private final LayerContext layerContext;
    private final ConstellationServer server;
    
    public LayerContextTreeModel(final LayerContext context, ConstellationServer server){
        super(new javax.swing.tree.DefaultMutableTreeNode());
        this.layerContext = context;
        this.server       = server;
        final ValueNode node = new ValueNode(context);
        setRoot(node);
        refresh();
    }

    public void refresh(){
        ((ValueNode)getRoot()).refresh();
    }

    public void removeProperty(final TreePath path){
        final ValueNode node = (ValueNode) path.getLastPathComponent();
        final Object userObject = node.getUserObject();

        //only works if the last node is a source
        if(!(userObject instanceof Source)){
            return;
        }

        final Source src = (Source) userObject;
        layerContext.getLayers().remove(src);
        
        //update the treenode
        removeNodeFromParent(node); //fires event
    }

    private class ValueNode extends DefaultMutableTreeNode{

        public ValueNode(Object obj) {
            super(obj);
        }

        public synchronized void refresh(){

            //todo not the best way but at least it's properly refreshed
            //remove all children
            for(int i=getChildCount()-1;i>=0;i--){
                removeNodeFromParent((ValueNode)getChildAt(i));
            }


            //create all children
            if(userObject instanceof LayerContext){
                final LayerContext catt = (LayerContext) userObject;
                
                for(Source src : catt.getLayers()){
                    final ValueNode n = new ValueNode(src);
                    insertNodeInto(n, this, getChildCount()); //fires event
                    n.refresh();
                }                
            }else if(userObject instanceof Source){
                final Source src = (Source) userObject;                
                final String id = src.getId();
                                
                final List<SourceElement> elements = new ArrayList<SourceElement>();
                
                ProvidersReport report = server.providers.listProviders();
                for (ProviderServiceReport providerService : report.getProviderServices()){
                    for (ProviderReport provider : providerService.getProviders()) {
                        if (!provider.getId().equals(id)) {
                            continue;
                        }
                        for(String n : provider.getItems()){
                            elements.add(new SourceElement(src, n));
                        }
                    }
                }
                
                for(SourceElement ele : elements){
                    final ValueNode n = new ValueNode(ele);
                    insertNodeInto(n, this, getChildCount()); //fires event
                    n.refresh();
                }         
            }
        }

    }
    
}
