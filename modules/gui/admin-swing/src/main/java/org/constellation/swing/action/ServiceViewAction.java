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
import org.constellation.swing.LayerRowModel;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class ServiceViewAction extends Action {

    public ServiceViewAction() {
        super("view");
    }

    @Override
    public boolean isEnable() {
        if (target instanceof Map.Entry) {
            final Instance inst = (Instance) ((Map.Entry)target).getKey();
            final String type = (String) ((Map.Entry)target).getValue();
            final String lowerType = type.toLowerCase();

            if(!(lowerType.equals("wms") || lowerType.equals("wmts") || lowerType.equals("wfs"))){
                //not a viewable type
                return false;
            }

            return true;
        }
        return false;
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
            final Instance inst = (Instance) ((Map.Entry)target).getKey();
            final String type = (String) ((Map.Entry)target).getValue();
            final String lowerType = type.toLowerCase();

            if(!(lowerType.equals("wms") || lowerType.equals("wmts") || lowerType.equals("wfs"))){
                //not a viewable type
                return;
            }

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
        final Action action = new ServiceViewAction();
        action.addPropertyChangeListener(this.getPropertyListeners());
        return action;
    }

}
