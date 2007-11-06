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
package net.seagis.sie.type.bbox;

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
 * Une action représentant l'ajout d'une région géographique. Cette action fait apparaître les
 * boîtes de dialogues demandant à l'utilisateur de fournir les coordonnées spatio-temporelles.
 * L'action d'ajouter une nouvelle région géographique consiste à créer un nouveau fichier
 * {@code .bbox}. Aucune autre action n'est effectuée; l'ajout de ce fichier sera détecté par
 * l'explorateur de Netbeans, ce qui permettra à l'utilisateur de l'ouvrir pour le modifier.
 * <p>
 * L'{@linkplain net.seagis.sie.window.series.ExplorerWindow explorateur des séries de données}
 * proposera cette action dans son menu contextuel, via sa
 * {@linkplain net.seagis.sie.window.series.RootNode racine}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class AddAction extends AbstractAction {
    /**
     * Le dossier racine dans lequel on ajoutera les noeuds représentant des séries de données.
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
     * Retourne le titre à utiliser pour l'item de cette action dans le menu contextuel,
     * ainsi que pour le titre de la boîte de dialogue de l'assistant.
     */
    private static String getTitle() {
        return NbBundle.getMessage(AddAction.class, "CTL_AddAction");
    }

    /**
     * Ajoute une nouvelle région d'intérêt en fonction des informations fournies par
     * l'utilisateur. L'appel de cette méthode fera apparaître un assistant qui guidera
     * l'utilisateur pas à pas dans sa sélection d'une région spatio-temporelle.
     */
    public void actionPerformed(final ActionEvent event) {
        /*
         * Recherche à partir de la racine le premier nom de fichier libre.
         * Si un nom est déjà utilisé, on y ajoutera un suffix tel que " (1)".
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
         * Construit l'objet qui sera à enregistrer (bbox), puis fait apparaître l'assistant
         * qui demandera à l'utilisateur de choisir une région géographique. L'objet bbox est
         * initialisé avec des valeurs par défaut, mais ces valeurs seront modifiées par les
         * différentes étapes de l'assistant via la méthode WizardPanel.storeSettings(bbox).
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
         * Construit une nouvelle région géographique et crée le fichier correspondant.
         */
        try {
            bbox.save(file.createData(filename, Loader.EXTENSION));
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
}
