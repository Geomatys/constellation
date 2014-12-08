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

import org.constellation.admin.service.ConstellationClient;
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

import javax.swing.*;
import java.net.MalformedURLException;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public final class JConstellationFrame extends JFrame{
    
    public JConstellationFrame(final ConstellationClient serverV2){
        final JTabbedPane pane = new JTabbedPane();
        pane.add("Services", new JServicesPane(
                serverV2,
                (FrameDisplayer)null,
                (RoleController)null,
                new ServiceViewAction(),
                new ServiceEditAction(),
                new ServiceReloadAction(),
                new ServiceStartStopAction(),
                new ServiceDeleteAction()));
        pane.add("Providers", new JProvidersPane(
                serverV2,
                (FrameDisplayer)null,
                (RoleController)null,
                new ProviderViewAction(),
                new ProviderEditAction(),
                new ProviderReloadAction(),
                new ProviderDeleteAction()));
        pane.add("Utils", new JUtilsPane(
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
                
        // new API
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 3);
        } else  {
            url = url.substring(0, url.length() - 2);
        }
        final ConstellationClient serverV2;
        if ("Form".equals(authType)) {
            serverV2 = new ConstellationClient(url).auth(login, password);
        } else if ("Basic".equals(authType)) {
            serverV2 = new ConstellationClient(url).basicAuth(login, password);
        } else {
            throw new IllegalArgumentException("Unexpected auth type:" + authType);
        }
        
        final JConstellationFrame frame = new JConstellationFrame(serverV2);
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
