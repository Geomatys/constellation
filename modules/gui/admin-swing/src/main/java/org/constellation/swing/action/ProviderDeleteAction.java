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
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.constellation.configuration.ProviderReport;
import org.constellation.security.ActionPermissions;
import org.constellation.swing.LayerRowModel;
import org.openide.util.Exceptions;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class ProviderDeleteAction extends Action {

    private static final ImageIcon ICON_DELETE =  new ImageIcon(
            ServiceEditAction.class.getResource("/org/constellation/swing/serviceCross.png"));
    
    public ProviderDeleteAction() {
        super(ActionPermissions.NEW_PROVIDER);
    }

    @Override
    public boolean isEnable() {
        if (target instanceof Map.Entry) {
            final Map.Entry entry = (Map.Entry) target;
            final String type = (String) entry.getKey();
            if(!"go2style".equalsIgnoreCase(type)){
                return true;
            }
        }
        return false;
    }

    @Override
    public String getDisplayName() {
        return LayerRowModel.BUNDLE.getString("delete");
    }

    @Override
    public ImageIcon getIcon() {
        return ICON_DELETE;
    }

    @Override
    public Color getTextColor() {
        return Color.WHITE;
    }

    @Override
    public Color getBackgroundColor() {
        return new Color(180,60,60);
    }

    @Override
    public void actionPerformed() {
        if(target instanceof Map.Entry){
            final Map.Entry entry = (Map.Entry) target;
            final ProviderReport inst = (ProviderReport) entry.getValue();

            final int res = JOptionPane.showConfirmDialog(null, LayerRowModel.BUNDLE.getString("confirmdelete")
                    ,getDisplayName(),JOptionPane.YES_NO_OPTION);
            
            if(res == JOptionPane.YES_OPTION){
                try {
                    serverV2.providers.deleteProvider(inst.getId(), false);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                SwingUtilities.invokeLater(new Runnable() {
                   @Override
                   public void run() {
                       fireUpdate();
                   }
               });
            }
        }
    }

    @Override
    public Action clone() {
        final Action action = new ProviderDeleteAction();
        action.addPropertyChangeListener(this.getPropertyListeners());
        return action;
    }

}
