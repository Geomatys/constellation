/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.swing;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import org.constellation.admin.service.ConstellationClient;

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
    
    public JConstellationFrame(final ConstellationServer server, final ConstellationClient serverV2){
        final JTabbedPane pane = new JTabbedPane();
        pane.add("Services", new JServicesPane(
                server,
                serverV2,
                (FrameDisplayer)null,
                (RoleController)null,
                new ServiceViewAction(),
                new ServiceEditAction(),
                new ServiceReloadAction(),
                new ServiceStartStopAction(),
                new ServiceDeleteAction()));
        pane.add("Providers", new JProvidersPane(
                server,
                serverV2,
                (FrameDisplayer)null,
                (RoleController)null,
                new ProviderViewAction(),
                new ProviderEditAction(),
                new ProviderReloadAction(),
                new ProviderDeleteAction()));
        pane.add("Utils", new JUtilsPane(
                server,
                serverV2,
                (FrameDisplayer)null));
        setContentPane(pane);
    }
    
    public static void show(String login, String password, String url, boolean showLoginDialog) throws MalformedURLException{
        
        String authType = "Form";
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
            authType = dialog.getAuthType();
        }
                
        final ParameterValueGroup param = ConstellationServerFactory.PARAMETERS.createValue();
        Parameters.getOrCreate(ConstellationServerFactory.URL, param).setValue(new URL(url));
        Parameters.getOrCreate(ConstellationServerFactory.USER, param).setValue(login);
        Parameters.getOrCreate(ConstellationServerFactory.PASSWORD, param).setValue(password);
        Parameters.getOrCreate(ConstellationServerFactory.SECURITY_TYPE, param).setValue(authType);

        // old API
        final ConstellationServer server   = new ConstellationServer(param);
        // new API
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 3);
        } else  {
            url = url.substring(0, url.length() - 2);
        }
        final ConstellationClient serverV2 = new ConstellationClient(url).auth(login, password);
        
        final JConstellationFrame frame = new JConstellationFrame(server, serverV2);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    public static void main(String[] args) throws MalformedURLException {
    	String url = args.length==1?args[0]:"http://localhost:8080/constellation/WS/";
        JConstellationFrame.show("admin", "admin", url, true);
    }
    
}
