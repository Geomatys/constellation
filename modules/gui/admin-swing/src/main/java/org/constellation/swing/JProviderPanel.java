/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.UUID;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.constellation.admin.service.ConstellationServer;
import org.constellation.configuration.ProviderReport;
import org.constellation.configuration.ProviderServiceReport;
import org.constellation.configuration.ProvidersReport;
import org.geotoolkit.feature.FeatureUtilities;
import org.geotoolkit.gui.swing.propertyedit.JFeatureOutLine;
import org.jdesktop.swingx.JXTitledPanel;
import org.netbeans.swing.outline.*;
import org.opengis.feature.ComplexAttribute;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class JProviderPanel extends JXTitledPanel {
    
    
    private final ConstellationServer server;
    private final String providerId;
    private final ParameterDescriptorGroup sourceDesc;
    private final String subGroup;
    private final Outline outline = new Outline();
    private final ParameterDescriptorGroup paramDesc;
    

    public JProviderPanel(final ConstellationServer server, final String providerId, 
            final ParameterDescriptorGroup sourceDesc , final String subgroup) {
        super("");
        setBorder(new EmptyBorder(10, 0, 10, 0));
        setTitle(subgroup);        
        
        this.server = server;
        this.providerId = providerId;
        this.sourceDesc = sourceDesc;
        this.subGroup = subgroup;
        
        //get the description for this type
        final ParameterDescriptorGroup choiceDesc = (ParameterDescriptorGroup) sourceDesc.descriptor("choice");        
        paramDesc = (ParameterDescriptorGroup) choiceDesc.descriptor(subgroup);
        
        
        outline.setBorder(new EmptyBorder(0, 0, 0, 0));
        outline.setRootVisible(false);
        outline.setShowGrid(false);
        outline.setRenderDataProvider(new ProviderRenderer());
        
        final JPanel panel = new JPanel(new BorderLayout(5,5));
        final JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new AbstractAction("New") {
            @Override
            public void actionPerformed(ActionEvent e) {
                ParameterValueGroup param = paramDesc.createValue();
                final ComplexAttribute prop = FeatureUtilities.toFeature(param);
                JFeatureOutLine.show(prop);
                param = FeatureUtilities.toParameter(prop, paramDesc);
                
                final ParameterValueGroup sourceParam = sourceDesc.createValue();
                sourceParam.parameter("id").setValue(UUID.randomUUID().toString());
                final ParameterValueGroup choiceparam = sourceParam.groups("choice").get(0);
                choiceparam.values().add(param);
                
                server.providers.createProvider(providerId, sourceParam);
                refresh();
            }
        });
        panel.add(BorderLayout.NORTH,toolbar);
        panel.add(BorderLayout.CENTER,outline);
        
        
        setContentContainer(panel);
        refresh();
    }
    
    
    private void refresh(){
        final ProvidersReport report = server.providers.listProviders();
        final ProviderServiceReport service = report.getProviderService(providerId);
        final List<ProviderReport> providers = service.getProviders();
        
                
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        final DefaultTreeModel tree = new DefaultTreeModel(root);
        
        for(ProviderReport pr : providers){
            final ParameterValueGroup param = (ParameterValueGroup) server.providers.getProviderConfiguration(pr.getId(), sourceDesc);
            final ParameterValueGroup choice = param.groups("choice").get(0);
            final ParameterValueGroup type = (ParameterValueGroup) choice.values().get(0);
            if(!type.getDescriptor().getName().getCode().equals(subGroup)){
                continue;
            }
            
            final DefaultMutableTreeNode node = new DefaultMutableTreeNode(pr);
            root.add(node);
            final List<String> items = pr.getItems();
            if(items != null){
                for(String item : items){
                    final DefaultMutableTreeNode itemnode = new DefaultMutableTreeNode(item);
                    node.add(itemnode);
                }
            }
        }
        
        final RowModel rows = new ProviderRowModel();
        final OutlineModel model = DefaultOutlineModel.createOutlineModel(tree, rows);
        outline.setModel(model);
        
    }
    
    private static class ProviderRowModel implements RowModel{

        @Override
        public int getColumnCount() {
            return 0;
        }

        @Override
        public Object getValueFor(Object o, int i) {
            
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
            o = node.getUserObject();
            
            System.out.println(o.getClass());
            
            if(o instanceof ProviderReport){
                final ProviderReport report = (ProviderReport) o;
                return report.getId();
            }
            
            return o.toString();
        }

        @Override
        public Class getColumnClass(int i) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(Object o, int i) {
            return false;
        }

        @Override
        public void setValueFor(Object o, int i, Object o1) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getColumnName(int i) {
            return "";
        }
        
    }
    
    private static class ProviderRenderer implements RenderDataProvider{

        @Override
        public String getDisplayName(Object o) {
            
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
            o = node.getUserObject();
            
            if(o instanceof ProviderReport){
                final ProviderReport report = (ProviderReport) o;
                return report.getId();
            }
            
            return o.toString();
        }

        @Override
        public boolean isHtmlDisplayName(Object o) {
            return false;
        }

        @Override
        public Color getBackground(Object o) {
            return null;
        }

        @Override
        public Color getForeground(Object o) {
            return null;
        }

        @Override
        public String getTooltipText(Object o) {
            return null;
        }

        @Override
        public Icon getIcon(Object o) {
            return null;
        }
        
    }
    
}
