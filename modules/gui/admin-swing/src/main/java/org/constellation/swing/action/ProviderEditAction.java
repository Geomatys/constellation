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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.constellation.configuration.ProviderReport;
import org.constellation.security.ActionPermissions;
import org.constellation.swing.JProviderEditPane;
import org.constellation.swing.JServicesPane;
import org.constellation.swing.LayerRowModel;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class ProviderEditAction extends Action {

    private static final ImageIcon ICON_SERVICE_EDIT =  new ImageIcon(
            JServicesPane.class.getResource("/org/constellation/swing/serviceEdit.png"));

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("org.constellation.swing.Bundle");

    public ProviderEditAction() {
        super(ActionPermissions.EDIT_PROVIDER);
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
        return LayerRowModel.BUNDLE.getString("edit");
    }

    @Override
    public ImageIcon getIcon() {
        return ICON_SERVICE_EDIT;
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

                    final JProviderEditPane edit = new JProviderEditPane(server, serverV2, type, inst);
                    edit.setName(BUNDLE.getString("data") +" - "+ BUNDLE.getString("edit") +" - "+ inst.getId());
                    final PropertyChangeListener cl = new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent evt) {
                            if ("update".equals(evt.getPropertyName())) {
                                edit.removePropertyChangeListener(this);
                                fireUpdate();
                            }
                        }
                    };

                    edit.addPropertyChangeListener(cl);

                    getDisplayer().display(edit);
                }
            });

        }
    }

    @Override
    public Action clone() {
        final Action action = new ProviderEditAction();
        action.addPropertyChangeListener(this.getPropertyListeners());
        return action;
    }

}
