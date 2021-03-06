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
package org.constellation.swing.action;

import org.constellation.ServiceDef;
import org.constellation.configuration.Instance;
import org.constellation.configuration.ServiceStatus;
import org.constellation.security.ActionPermissions;
import org.constellation.swing.LayerRowModel;
import org.openide.util.Exceptions;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Map;

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
            if(ServiceStatus.STARTED.equals(inst.getStatus())){
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
            if(ServiceStatus.STARTED.equals(inst.getStatus())){
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
            if(ServiceStatus.STARTED.equals(inst.getStatus())){
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
            try {
                final Instance inst = (Instance) ((Map.Entry)target).getKey();
                final String type = (String) ((Map.Entry)target).getValue();

                if(ServiceStatus.STARTED.equals(inst.getStatus())){
                        serverV2.services.stop(ServiceDef.Specification.valueOf(type.toUpperCase()), inst.getIdentifier());
                }else{
                    serverV2.services.start(ServiceDef.Specification.valueOf(type.toUpperCase()), inst.getIdentifier());
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        fireUpdate();
                    }
                });
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public Action clone() {
        final Action action = new ServiceStartStopAction();
        action.addPropertyChangeListener(this.getPropertyListeners());
        return action;
    }

}
