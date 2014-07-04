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

import org.constellation.admin.service.ConstellationClient;
import org.constellation.swing.JServicesPane;
import org.geotoolkit.gui.swing.util.ActionCell;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class ActionRenderer extends ActionCell.Renderer{

    private static final Font FONT = new Font("Monospaced", Font.PLAIN, 12);
        
    private final ConstellationClient serverV2;

    public ActionRenderer(final ConstellationClient serverV2) {
        super(null);
        this.serverV2 = serverV2;
    }

    @Override
    public Icon getIcon(Object value) {
        
        final Action action = (Action) value;
        
        action.setServerV2(serverV2);
        
        if(!action.isEnable()) return null;
        
        final String displayText = action.getDisplayName();
        final Color textColor = action.getTextColor();
        final Color bgColor = action.getBackgroundColor();
        final ImageIcon icon = action.getIcon();
        
        final BufferedImage img = JServicesPane.createImage(displayText, icon, textColor, FONT, bgColor);
        return new ImageIcon(img);
    }

}
