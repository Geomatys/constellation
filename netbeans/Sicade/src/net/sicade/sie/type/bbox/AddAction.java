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
package net.sicade.sie.type.bbox;

// J2SE dependencies
import java.io.IOException;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

// OpenIDE dependencies
import org.openide.ErrorManager;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.loaders.DataFolder;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;


/**
 * Une action repr�sentant l'ajout d'une r�gion g�ographique. Cette action fait appara�tre les
 * bo�tes de dialogues demandant � l'utilisateur de fournir les coordonn�es spatio-temporelles.
 * L'action d'ajouter une nouvelle r�gion g�ographique consiste � cr�er un nouveau fichier
 * {@code .bbox}. Aucune autre action n'est effectu�e; l'ajout de ce fichier sera d�tect� par
 * l'explorateur de Netbeans, ce qui permettra � l'utilisateur de l'ouvrir pour le modifier.
 * <p>
 * L'{@linkplain net.sicade.sie.window.series.ExplorerWindow explorateur des s�ries de donn�es}
 * proposera cette action dans son menu contextuel, via sa
 * {@linkplain net.sicade.sie.window.series.RootNode racine}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class AddAction extends AbstractAction {
    /**
     * Le dossier racine dans lequel on ajoutera les noeuds repr�sentant des s�ries de donn�es.
     */
    private final DataFolder folder;

    /**
     * Construit une nouvelle action.
     */
    public AddAction(final DataFolder folder) {
        this.folder = folder;
        putValue(NAME, getTitle());
        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage(RootNode.ICON_PATH, true)));
    }

    /**
     * Retourne le titre � utiliser pour l'item de cette action dans le menu contextuel,
     * ainsi que pour le titre de la bo�te de dialogue de l'assistant.
     */
    private static String getTitle() {
        return NbBundle.getMessage(AddAction.class, "CTL_AddAction");
    }

    /**
     * Ajoute une nouvelle r�gion d'int�r�t en fonction des informations fournies par
     * l'utilisateur. L'appel de cette m�thode fera appara�tre un assistant qui guidera
     * l'utilisateur pas � pas dans sa s�lection d'une r�gion spatio-temporelle.
     */
    public void actionPerformed(final ActionEvent event) {
        /*
         * Recherche � partir de la racine le premier nom de fichier libre.
         * Si un nom est d�j� utilis�, on y ajoutera un suffix tel que " (1)".
         */
        final FileObject file = folder.getPrimaryFile();
        String filename; // Sera le nom sans l'extension.
        if (true) {
            final StringBuilder builder = new StringBuilder(Loader.DEFAULT_BASE_NAME);
            final int length = builder.length();
            int sequence = 1;
            do {
                builder.setLength(length);
                if (sequence > 1) {
                    builder.append(" (");
                    builder.append(sequence);
                    builder.append(')');
                }
                sequence++;
                filename = builder.toString();
            } while (file.getFileObject(filename, Loader.EXTENSION) != null);
        } else {
            filename = Loader.DEFAULT_BASE_NAME;
        }
        /*
         * Construit l'objet qui sera � enregistrer (bbox), puis fait appara�tre l'assistant
         * qui demandera � l'utilisateur de choisir une r�gion g�ographique. L'objet bbox est
         * initialis� avec des valeurs par d�faut, mais ces valeurs seront modifi�es par les
         * diff�rentes �tapes de l'assistant via la m�thode WizardPanel.storeSettings(bbox).
         */
        final BoundingBox bbox = new BoundingBox(filename);
        final WizardDescriptor wizard = new WizardDescriptor(new WizardIterator(), bbox);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(wizard);
        dialog.setTitle(getTitle());
        dialog.setVisible(true);
        if (wizard.getValue() != WizardDescriptor.FINISH_OPTION) {
            return;
        }
        filename = bbox.getName();
        /*
         * Construit une nouvelle r�gion g�ographique et cr�e le fichier correspondant.
         */
        try {
            bbox.save(file.createData(filename, Loader.EXTENSION));
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
}
