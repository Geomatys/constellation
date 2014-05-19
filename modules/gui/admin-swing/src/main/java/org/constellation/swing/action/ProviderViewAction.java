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
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.constellation.configuration.ProviderReport;
import org.constellation.security.ActionPermissions;
import org.constellation.swing.LayerRowModel;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class ProviderViewAction extends Action {


    public ProviderViewAction() {
        super(ActionPermissions.VIEW_PROVIDER);
    }

    @Override
    public boolean isEnable() {
        if(target instanceof Map.Entry){
            final Map.Entry entry = (Map.Entry) target;
            final String type = (String) entry.getKey();
            if("sld".equalsIgnoreCase(type) || "go2style".equalsIgnoreCase(type)){
                return false;
            }
        }
        return true;
    }

    @Override
    public String getDisplayName() {
        return LayerRowModel.BUNDLE.getString("view");
    }

    @Override
    public ImageIcon getIcon() {
        return null;
    }

    @Override
    public Color getTextColor() {
        return Color.BLACK;
    }

    @Override
    public Color getBackgroundColor() {
        return Color.LIGHT_GRAY;
    }

    @Override
    public void actionPerformed() {
        if (target instanceof Map.Entry) {
            final Map.Entry entry = (Map.Entry) target;
            final String type = (String) entry.getKey();
            final ProviderReport inst = (ProviderReport) entry.getValue();

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    getDisplayer().display(server, type, inst);
                }
            });

        }
    }

    @Override
    public Action clone() {
        final Action action = new ProviderViewAction();
        action.addPropertyChangeListener(this.getPropertyListeners());
        return action;
    }

}
