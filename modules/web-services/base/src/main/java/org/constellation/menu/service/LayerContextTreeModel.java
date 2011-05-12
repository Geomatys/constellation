/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.menu.service;

import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.Source;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.configuration.ProviderParameters;
import org.geotoolkit.gui.swing.tree.DefaultMutableTreeNode;
import org.geotoolkit.parameter.Parameters;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class LayerContextTreeModel extends DefaultTreeModel{

    private final LayerContext layerContext;
    
    public LayerContextTreeModel(final LayerContext context){
        super(new javax.swing.tree.DefaultMutableTreeNode());
        this.layerContext = context;
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
                
                final boolean loadAll = Boolean.TRUE.equals(src.getLoadAll()); 
                final List<String> included = new ArrayList<String>();
                final List<String> excluded = new ArrayList<String>();
                for(Layer l : src.getInclude()){
                    included.add(l.getName().getLocalPart());
                }
                for(Layer l : src.getExclude()){
                    excluded.add(l.getName().getLocalPart());
                }
                
                final List<SourceElement> elements = new ArrayList<SourceElement>();
                
                for(LayerProvider provider : LayerProviderProxy.getInstance().getProviders()){
                    final String name = Parameters.stringValue(ProviderParameters.SOURCE_ID_DESCRIPTOR, provider.getSource());
                    if(!name.equals(id)){
                        continue;
                    }
                    
                    
                    
                }
                
                for(SourceElement ele : elements){
                    
                }
                                
            }
        }

    }

    public static class SourceElement{
        
        private final String name;
        private final boolean selected;

        public SourceElement(String name, boolean selected) {
            this.name = name;
            this.selected = selected;
        }
        
        
        
    }
    
}
