/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
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
package org.constellation.sie.window.series;

// J2SE dependencies
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

// OpenIDE dependencies
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;


/**
 * Action affichant la {@linkplain ExplorerWindow fenêtre de l'explorateur} des séries d'images.
 * Cette action est déclarée dans le fichier {@code layer.xml}, qui le placera aussi dans le menu
 * "Window".
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class ViewAction extends AbstractAction {
    /**
     * Construit une action avec son nom et son icone.
     */
    public ViewAction() {
        putValue(NAME, NbBundle.getMessage(ViewAction.class, "WindowTitle"));
        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage(ExplorerWindow.ICON_PATH, true)));
    }

    /**
     * Ouvre une {@linkplain ExplorerWindow fenêtre de l'explorateur} des séries d'images.
     */
    public void actionPerformed(final ActionEvent event) {
        final TopComponent win = ExplorerWindow.findInstance();
        win.open();
        win.requestActive();
    }
}
