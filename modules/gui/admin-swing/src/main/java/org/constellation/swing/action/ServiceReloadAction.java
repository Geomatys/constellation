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

import java.awt.Color;
import java.io.IOException;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.constellation.ServiceDef.Specification;
import org.constellation.configuration.Instance;
import org.constellation.security.ActionPermissions;
import org.constellation.swing.LayerRowModel;
import org.openide.util.Exceptions;

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
            try {
                final Instance inst = (Instance) ((Map.Entry)target).getKey();
                final String type = (String) ((Map.Entry)target).getValue();
                serverV2.services.restart(Specification.valueOf(type), inst.getIdentifier(), false);
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
        final Action action = new ServiceReloadAction();
        action.addPropertyChangeListener(this.getPropertyListeners());
        return action;
    }

}
