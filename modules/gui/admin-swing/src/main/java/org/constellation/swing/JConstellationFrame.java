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

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.constellation.admin.service.ConstellationServer;
import org.constellation.admin.service.ConstellationServerFactory;
import org.constellation.security.RoleController;
import org.constellation.swing.action.ProviderDeleteAction;
import org.constellation.swing.action.ProviderEditAction;
import org.constellation.swing.action.ProviderReloadAction;
import org.constellation.swing.action.ProviderViewAction;
import org.constellation.swing.action.ServiceDeleteAction;
import org.constellation.swing.action.ServiceEditAction;
import org.constellation.swing.action.ServiceReloadAction;
import org.constellation.swing.action.ServiceStartStopAction;
import org.constellation.swing.action.ServiceViewAction;
import org.geotoolkit.parameter.Parameters;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public final class JConstellationFrame extends JFrame{
    
    public JConstellationFrame(ConstellationServer server){
        final JTabbedPane pane = new JTabbedPane();
        pane.add("Services", new JServicesPane(
                server,
                (FrameDisplayer)null,
                (RoleController)null,
                new ServiceViewAction(),
                new ServiceEditAction(),
                new ServiceReloadAction(),
                new ServiceStartStopAction(),
                new ServiceDeleteAction()));
        pane.add("Providers", new JProvidersPane(
                server,
                (FrameDisplayer)null,
                (RoleController)null,
                new ProviderViewAction(),
                new ProviderEditAction(),
                new ProviderReloadAction(),
                new ProviderDeleteAction()));
        setContentPane(pane);
    }
    
    public static void show(String login, String password, String url, boolean showLoginDialog) throws MalformedURLException{
        
        if(showLoginDialog){
            final JConstellationLoginDialog dialog = new JConstellationLoginDialog(null, true);
            dialog.setLogin(login);
            dialog.setPassword(password);
            dialog.setURL(url);
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
            login = dialog.getLogin();
            password = dialog.getPassword();
            url = dialog.getURL();
        }
                
        final ParameterValueGroup param = ConstellationServerFactory.PARAMETERS.createValue();
        Parameters.getOrCreate(ConstellationServerFactory.URL, param).setValue(new URL(url));
        Parameters.getOrCreate(ConstellationServerFactory.USER, param).setValue(login);
        Parameters.getOrCreate(ConstellationServerFactory.PASSWORD, param).setValue(password);
                
        final ConstellationServer server = new ConstellationServer(param);
        
        final JConstellationFrame frame = new JConstellationFrame(server);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    public static void main(String[] args) throws MalformedURLException {
    	String url = args.length==1?args[0]:"http://localhost:8084/constellation/WS/";
        
		JConstellationFrame.show("admin", "admin", url, true);
    }
    
}
