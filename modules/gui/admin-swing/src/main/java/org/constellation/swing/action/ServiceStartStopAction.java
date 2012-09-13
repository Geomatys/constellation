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
import org.constellation.configuration.ServiceStatus;
import org.constellation.security.ActionPermissions;
import org.constellation.swing.LayerRowModel;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class ServiceStartStopAction extends Action {

    private static final ImageIcon ICON_SERVICE_START = new ImageIcon(
            ServiceStartStopAction.class.getResource("/org/constellation/swing/serviceStart.png"));
    private static final ImageIcon ICON_SERVICE_STOP =  new ImageIcon(
            ServiceStartStopAction.class.getResource("/org/constellation/swing/serviceStop.png"));

    public ServiceStartStopAction() {
        super(ActionPermissions.START_STOP_SERVICE);
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
        if(target instanceof Map.Entry){
            final Instance inst = (Instance) ((Map.Entry)target).getKey();
            if(ServiceStatus.WORKING.equals(inst.getStatus())){
                return LayerRowModel.BUNDLE.getString("stop");
            }else{
                return LayerRowModel.BUNDLE.getString("start");
            }
        }
        return "";
    }

    @Override
    public ImageIcon getIcon() {
        if(target instanceof Map.Entry){
            final Instance inst = (Instance) ((Map.Entry)target).getKey();
            if(ServiceStatus.WORKING.equals(inst.getStatus())){
                return ICON_SERVICE_STOP;
            }else{
                return ICON_SERVICE_START;
            }
        }
        return null;
    }

    @Override
    public Color getTextColor() {
        return Color.WHITE;
    }

    @Override
    public Color getBackgroundColor() {
        if(target instanceof Map.Entry){
            final Instance inst = (Instance) ((Map.Entry)target).getKey();
            if(ServiceStatus.WORKING.equals(inst.getStatus())){
                return new Color(180,60,60);
            }else{
                return new Color(130,160,50);
            }
        }
        return null;
    }

    @Override
    public void actionPerformed() {
        if(target instanceof Map.Entry){
            final Instance inst = (Instance) ((Map.Entry)target).getKey();
            final String type = (String) ((Map.Entry)target).getValue();

            if(ServiceStatus.WORKING.equals(inst.getStatus())){
                server.services.stopInstance(type, inst.getName());
            }else{
                server.services.startInstance(type, inst.getName());
            }
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
        final Action action = new ServiceStartStopAction();
        action.addPropertyChangeListener(this.getPropertyListeners());
        return action;
    }

}
