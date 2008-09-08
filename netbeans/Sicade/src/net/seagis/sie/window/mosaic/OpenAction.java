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
package net.seagis.sie.window.mosaic;

// J2SE dependencies
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

// OpenIDE dependencies
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

// Sicade dependencies
import net.seagis.observation.coverage.Series;


/**
 * Action ouvrant une nouvelle fenêtre contenant une {@linkplain MosaicWindow mosaïque d'images}.
 * Cette fenêtre est ouverte comme un document. Son ouverture est déclanchée par la sélection de
 * "Ouvrir" dans le menu contextuel du noeud représentant une série.
 * 
 * @author Martin Desruisseaux
 * @version $Id$
 */
public final class OpenAction extends AbstractAction {
    /**
     * L'icone pour cette action. Ne sera lue que la première fois où elle sera nécessaire.
     */
    private static Icon icon;

    /**
     * La série d'images à ouvrir.
     */
    private final Series series;

    /**
     * Construit une action avec son nom et son icone.
     */
    public OpenAction(final Series series) {
        this.series = series;
        putValue(NAME, NbBundle.getMessage(OpenAction.class, "Open"));
        putValue(SMALL_ICON, getSharedIcon());
    }

    /**
     * Retourne un icone partagé pour cette action.
     */
    private static synchronized Icon getSharedIcon() {
        if (icon == null) {
            icon = new ImageIcon(Utilities.loadImage(MosaicWindow.ICON_PATH, true));
        }
        return icon;
    }

    /**
     * Ouvre une {@linkplain MosaicWindow mosaïque d'images} d'une série.
     */
    public void actionPerformed(final ActionEvent evt) {
        final TopComponent win = MosaicWindow.findInstance(series);
        win.open();
        win.requestActive();
    }
}
