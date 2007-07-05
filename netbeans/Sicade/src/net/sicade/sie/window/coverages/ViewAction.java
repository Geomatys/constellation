/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sicade.sie.window.coverages;

// J2SE dependencies
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

// OpenIDE dependencies
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;


/**
 * Action affichant la {@linkplain CoveragesWindow fenêtre de la liste des images} d'une série.
 * Cette action est déclarée dans le fichier {@code layer.xml}, qui le placera aussi dans le
 * menu "Window".
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
        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage(CoveragesWindow.ICON_PATH, true)));
    }

    /**
     * Ouvre une {@linkplain CoveragesWindow fenêtre de la liste des images} d'une série.
     */
    public void actionPerformed(ActionEvent evt) {
        final TopComponent win = CoveragesWindow.findInstance();
        win.open();
        win.requestActive();
    }
}
