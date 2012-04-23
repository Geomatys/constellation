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
import javax.swing.JFrame;
import org.constellation.admin.service.ConstellationServer;
import org.constellation.admin.service.ConstellationServerFactory;
import org.geotoolkit.parameter.Parameters;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public final class ConstellationFrame {
    
    private ConstellationFrame(){}
    
    
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
        Parameters.getOrCreate(ConstellationServerFactory.URL, param).setValue(url);
        Parameters.getOrCreate(ConstellationServerFactory.USER, param).setValue(login);
        Parameters.getOrCreate(ConstellationServerFactory.PASSWORD, param).setValue(password);
        
        
        final ConstellationServer server = new ConstellationServer(param);
        
        final JFrame frame = new JFrame();
        frame.setContentPane(new JConstellationPane(server));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    
}
