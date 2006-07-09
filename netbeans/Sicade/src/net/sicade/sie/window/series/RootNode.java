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
package net.sicade.sie.window.series;

// J2SE dependencies
import java.awt.Image;
import javax.swing.Action;

// OpenIDE dependencies
import org.openide.nodes.Node;
import org.openide.nodes.FilterNode;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;

// Sicade dependencies
import net.sicade.sie.type.bbox.AddAction;


/**
 * Le noeud � la racine de toutes l'arborescence qui appara�tra dans la fen�tre {@link ExplorerWindow}.
 * Cette racine ne sera pas n�cessairement visible; �a d�pend de la configuration de l'afficheur de
 * l'arborescence. Cette racine enveloppe un noeud du {@linkplain FileSystem syst�me de fichier} de
 * Netbeans. Ce syst�me de fichiers est en quelque sorte virtuel, et sa racine d�clar�e par l'�l�ment
 * {@value #ROOT_NAME} du fichier {@code layer.xml} (tous les �l�ments du fichier {@code layer.xml}
 * peuvent �tre un vus comme un syst�me de fichiers par Netbeans).
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class RootNode extends FilterNode {
    /**
     * Le nom de la racine dans le fichier {@code layer.xml}.
     */
    public static final String ROOT_NAME = "Series";

    /**
     * Ic�ne repr�sentant cette racine. Ne sera construit que la premi�re fois o� il sera
     * n�cessaire.
     */
    private transient Image icon;

    /**
     * Les actions � retourner par {@link #addAction}. Ne sera construit que la premi�re
     * fois o� elles seront n�cessaires.
     */
    private transient Action[] actions;

    /**
     * Construit une nouvelle racine pour l'�l�ment {@value #ROOT_NAME} d�clar� dans
     * le fichier {@code layer.xml}.
     *
     * @throws DataObjectNotFoundException si l'�l�ment {@value #ROOT_NAME} n'a pas �t� trouv�.
     */
    public RootNode() throws DataObjectNotFoundException {
        super(getSystemRoot());
        disableDelegation(DELEGATE_GET_DISPLAY_NAME    |
                          DELEGATE_SET_DISPLAY_NAME    |
                          DELEGATE_GET_CONTEXT_ACTIONS |
                          DELEGATE_GET_ACTIONS         |
                          DELEGATE_DESTROY);
        setDisplayName(NbBundle.getMessage(RootNode.class, "RootNode"));
        DataNode.setShowFileExtensions(false);
    }

    /**
     * Retourne la racine {@value #ROOT_NAME} vue par le {@linkplain FileSystem syst�me de fichier}
     * par d�faut de Netbeans. Ce noeud devra �tre envelopp� dans une instance de {@link RootNode}
     * afin d'offrir les services sp�cifiques � cette classe.
     *
     * @throws DataObjectNotFoundException si l'�l�ment {@value #ROOT_NAME} n'a pas �t� trouv�.
     */
    private static Node getSystemRoot() throws DataObjectNotFoundException {
        final FileSystem fs   = Repository.getDefault().getDefaultFileSystem();
        final DataObject data = DataObject.find(fs.getRoot().getFileObject(ROOT_NAME));
        return data.getNodeDelegate();
    }

    /**
     * Retourne l'ic�ne repr�sentant cette racine avant que le dossier ne soit ouvert.
     */
    @Override
    public Image getIcon(final int type) {
        if (icon == null) {
            icon = Utilities.loadImage(ExplorerWindow.ICON_PATH, true);
        }
        return icon;
    }

    /**
     * Retourne l'ic�ne repr�sentant cette racine apr�s que le dossier aie �t� ouvert.
     * L'impl�mentation par d�faut retourne <code>{@linkplain #getIcon getIcon}(type)</code>
     * (autrement dit nous ne faisons pas de distinction entre les dossiers ouverts et ferm�s).
     */
    @Override
    public Image getOpenedIcon(final int type) {
        return getIcon(type);
    }

    /**
     * Retourne {@code false} pour interdire tout renommage de cette racine. Un renommage peut
     * provoquer des r�sultats inatendus puisque le nom d'affichage sp�cifi� par le constructeur
     * n'est pas le m�me que le nom sp�cifi� interne sp�cifi� dans le fichier {@code layer.xml}.
     */
    @Override
    public boolean canRename() {
        return false;
    }

    /**
     * Retourne {@code false} pour interdire toute destruction (ou effacement) de la racine.
     */
    @Override
    public boolean canDestroy() {
        return false;
    }

    /**
     * Masque les propri�t�s pour ce noeud, car ces informations sont sans int�r�t pour
     * l'utilisateur et leur modification peut provoquer un dysfonctionnement du programme.
     */
    @Override
    public Node.PropertySet[] getPropertySets() {
        return new Node.PropertySet[0];
    }

    /**
     * Retourne l'ensemble des actions disponibles pour cette racine. Cette m�thode est
     * appel�e pour construire les menus "popup". Le tableau retourn� peut contenir des
     * valeurs {@code null} pour ins�rer des s�parateurs. Ce menu devra contenir au moins
     * un item pour cr�er un nouvel objet {@code .bbox}.
     */
    @Override
    public Action[] getActions(final boolean context) {
        if (actions == null) {
            final DataFolder df = (DataFolder) getLookup().lookup(DataFolder.class);
            actions = new Action[] {
                new AddAction(df)
            };
        }
        return actions;
    }
}
