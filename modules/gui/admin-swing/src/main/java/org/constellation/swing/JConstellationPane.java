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
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import org.constellation.admin.service.ConstellationServer;
import org.constellation.configuration.ProviderServiceReport;
import org.constellation.configuration.ProvidersReport;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class JConstellationPane extends JPanel{
    
    private final ConstellationServer server;
    private final JXTaskPane guiServicePane = new JXTaskPane();
    private final JXTaskPane guiDataPane = new JXTaskPane();
    private final JXTaskPane guiConfiguerationPane = new JXTaskPane();
    private final JSplitPane splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    private final JPanel rightPanel = new JPanel(new BorderLayout());
    
    public JConstellationPane(ConstellationServer server) {
        super(new BorderLayout());
        this.server = server;
        
        splitpane.setDividerSize(2);
        
        final JXTaskPaneContainer tpc = new JXTaskPaneContainer();
        guiServicePane.setTitle("Services");
        guiServicePane.setIcon(new ImageIcon(JConstellationPane.class.getResource("/icons/service.png")));
        
        guiDataPane.setTitle("Datas");
        guiDataPane.setIcon(new ImageIcon(JConstellationPane.class.getResource("/icons/data.png")));
        guiConfiguerationPane.setTitle("Configuration");
        guiConfiguerationPane.setIcon(new ImageIcon(JConstellationPane.class.getResource("/icons/config.png")));
        tpc.add(guiServicePane);
        tpc.add(guiDataPane);
        tpc.add(guiConfiguerationPane);
        
        splitpane.setLeftComponent(tpc);
        splitpane.setRightComponent(rightPanel);
        
        //fill all service types -----------------------------------------------
        final List<String> serviceIds = new ArrayList<String>();
        for(final Object serviceName : server.services.getAvailableService().keySet()){
            serviceIds.add(serviceName.toString());
        }
        Collections.sort(serviceIds);
        
        for(final String id : serviceIds){
            guiServicePane.add(new AbstractAction(id) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setServicePanel(id);
                }
            });
        }
        
        
        
        //fill all data types --------------------------------------------------
        final List<String> providerIds = new ArrayList<String>();
        final ProvidersReport report = server.providers.listProviders();
        for(ProviderServiceReport provider : report.getProviderServices()){
            providerIds.add(provider.getType());
        }
        Collections.sort(providerIds);
        
        for(final String id : providerIds){
            guiDataPane.add(new AbstractAction(id) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setDataPanel(id);
                }
            });
        }
        
        
        add(splitpane);
    }

    public ConstellationServer getServer() {
        return server;
    }
    
    private void setConfigPanel(JComponent panel){
        rightPanel.removeAll();
        rightPanel.add(panel);
        rightPanel.revalidate();
    }
    
    private void setServicePanel(String id){
        
    }
    
    private void setDataPanel(String id){
        setConfigPanel(new JProvidersPanel(server, id));
        
    }
    
}
