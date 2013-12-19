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
package org.constellation.swing.action;

import java.awt.Color;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.constellation.configuration.Instance;
import org.constellation.security.ActionPermissions;
import org.constellation.swing.LayerRowModel;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class ServiceReloadAction extends Action {

    private static final ImageIcon ICON_SERVICE_RELOAD =  new ImageIcon(
            ServiceEditAction.class.getResource("/org/constellation/swing/serviceReload.png"));


    public ServiceReloadAction() {
        super(ActionPermissions.RELOAD_SERVICE);
    }

    @Override
    public boolean isEnable() {
        if (target instanceof Map.Entry) {
            final Instance inst = (Instance) ((Map.Entry)target).getKey();
            final String type = (String) ((Map.Entry)target).getValue();
            final String lowerType = type.toLowerCase();
            return true;
        }
        return false;
    }

    @Override
    public String getDisplayName() {
        return LayerRowModel.BUNDLE.getString("reload");
    }

    @Override
    public ImageIcon getIcon() {
        return ICON_SERVICE_RELOAD;
    }

    @Override
    public Color getTextColor() {
        return Color.WHITE;
    }

    @Override
    public Color getBackgroundColor() {
        return new Color(65,150,190);
    }

    @Override
    public void actionPerformed() {
        if(target instanceof Map.Entry){
            final Instance inst = (Instance) ((Map.Entry)target).getKey();
            final String type = (String) ((Map.Entry)target).getValue();
             server.services.restartInstance(type, inst.getIdentifier());
             SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    fireUpdate();
                }
            });
        }
    }

    @Override
    public Action clone() {
        final Action action = new ServiceReloadAction();
        action.addPropertyChangeListener(this.getPropertyListeners());
        return action;
    }

}
