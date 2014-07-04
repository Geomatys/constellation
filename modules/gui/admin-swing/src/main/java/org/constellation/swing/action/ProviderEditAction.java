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

import org.constellation.configuration.ProviderReport;
import org.constellation.security.ActionPermissions;
import org.constellation.swing.JProviderEditPane;
import org.constellation.swing.JServicesPane;
import org.constellation.swing.LayerRowModel;
import org.openide.util.Exceptions;

import javax.swing.*;
import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Map;
import java.util.ResourceBundle;

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

                    try {
                        final JProviderEditPane edit = new JProviderEditPane(serverV2, type, inst);
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
                    } catch (IOException | XMLStreamException | ClassNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
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
