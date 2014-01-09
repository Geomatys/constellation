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
import java.awt.Font;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.constellation.admin.service.ConstellationClient;
import org.constellation.admin.service.ConstellationServer;
import org.constellation.swing.JServicesPane;
import org.geotoolkit.gui.swing.util.ActionCell;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class ActionRenderer extends ActionCell.Renderer{

    private static final Font FONT = new Font("Monospaced", Font.PLAIN, 12);
        
    private final ConstellationServer server;
    private final ConstellationClient serverV2;

    public ActionRenderer(final ConstellationServer server, final ConstellationClient serverV2) {
        super(null);
        this.server   = server;
        this.serverV2 = serverV2;
    }

    @Override
    public Icon getIcon(Object value) {
        
        final Action action = (Action) value;
        
        action.setServer(server);
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
