/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 2005, Institut de Recherche pour le D�veloppement
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
package net.sicade.sie.window.mosaic;

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
import net.sicade.observation.coverage.Series;


/**
 * Action ouvrant une nouvelle fen�tre contenant une {@linkplain MosaicWindow mosa�que d'images}.
 * Cette fen�tre est ouverte comme un document. Son ouverture est d�clanch�e par la s�lection de
 * "Ouvrir" dans le menu contextuel du noeud repr�sentant une s�rie.
 * 
 * @author Martin Desruisseaux
 * @version $Id$
 */
public final class OpenAction extends AbstractAction {
    /**
     * L'icone pour cette action. Ne sera lue que la premi�re fois o� elle sera n�cessaire.
     */
    private static Icon icon;

    /**
     * La s�rie d'images � ouvrir.
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
     * Retourne un icone partag� pour cette action.
     */
    private static synchronized Icon getSharedIcon() {
        if (icon == null) {
            icon = new ImageIcon(Utilities.loadImage(MosaicWindow.ICON_PATH, true));
        }
        return icon;
    }

    /**
     * Ouvre une {@linkplain MosaicWindow mosa�que d'images} d'une s�rie.
     */
    public void actionPerformed(final ActionEvent evt) {
        final TopComponent win = MosaicWindow.findInstance(series);
        win.open();
        win.requestActive();
    }
}
