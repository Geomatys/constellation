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
import javax.swing.Action;

// OpenIDE dependencies
import org.openide.nodes.Sheet;
import org.openide.loaders.DataNode;

// Sicade dependencies
import net.sicade.resources.XArray;


/**
 * Un noeud qui repr�sentera le {@linkplain DataFile contenu d'un fichier} de mani�re visuelle dans
 * l'arboresence. Ces fichiers sont de type {code bbox} et contiennent des informations sur la
 * {@linkplain BoundingBox r�gion spatio-temporelle d'int�r�t}, � partir de laquelle seront d�duites
 * les s�ries de donn�es propos�es � l'utilisateur dans l'arborescence.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class RootNode extends DataNode {
    /**
     * Chemin vers l'icone repr�sentant ce noeud, or {@code null} si aucun icone n'a �t� d�finie.
     */
    static final String ICON_PATH = "toolbarButtonGraphics/development/WebComponent16.gif";

    /**
     * Chemin vers l'icone repr�sentant ce noeud, or {@code null} si aucun icone n'a �t� d�finie.
     */
    static final String ICON_32_PATH = "toolbarButtonGraphics/development/WebComponent24.gif";

    /**
     * Les actions � retourner par {@link #addAction}. Ne sera construit que la premi�re
     * fois o� elles seront n�cessaires.
     */
    private transient Action[] actions;

    /**
     * Construit un noeud qui repr�sentera dans l'arborescence la r�gion d'int�r�t sp�cifi�e.
     */
    public RootNode(final DataFile data) {
        super(data, new ChildrenList(data.bbox));
        if (ICON_PATH != null) {
            setIconBaseWithExtension(ICON_PATH);
        }
    }

    /**
     * Reconstruit la totalit� de l'arborescence, typiquement apr�s un changement de la structure.
     * Cette m�thode est appel�e apr�s {@link BoundingBox#setTreeLayout}.
     */
    final void refresh() {
        setChildren(((ChildrenList) getChildren()).recreate());
    }

    /**
     * Retourne la liste des actions qui peuvent �tre ex�cut�es sur ce noeud. Cette m�thode obtient
     * les actions par d�faut de la classe parente, et y ajoute des actions particuli�res juste avant
     * le dernier s�parateur. Ca a pour effet de placer les actions suppl�mentaires avant l'item
     * "Propri�t�s" dans le menu contextuel.
     */
    @Override
    public Action[] getActions(final boolean context) {
        if (context) {
            return super.getActions(context);
        }
        if (actions == null) {
            final BoundingBox bbox = ((DataFile) getDataObject()).bbox;
            actions = super.getActions(context);
            int insertAt = actions.length;
            do if (--insertAt < 0) {
                insertAt = 0;
                break;
            } while (actions[insertAt] != null);
            actions = XArray.insert(actions, insertAt, 2);
            actions[insertAt++] = null;
            actions[insertAt++] = new TreeLayoutAction(this);
        }
        return actions;
    }

    /**
     * Construit une feuille de propri�t�s pour ce noeud.
     */
    @Override
    protected Sheet createSheet() {
        final BoundingBox bbox = ((DataFile) getDataObject()).bbox;
        return SimpleProperty.createSheet(bbox.getGeographicBoundingBox(),
                                          bbox.getTimeRange(),
                                          bbox.getResolution());
    }
}
