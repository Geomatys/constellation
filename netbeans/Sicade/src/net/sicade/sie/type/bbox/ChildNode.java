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
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

// OpenIDE dependencies
import org.openide.nodes.Sheet;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.NodeOperation;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;

// Sicade dependencies
import net.sicade.observation.Element;
import net.sicade.observation.CatalogException;
import net.sicade.observation.coverage.Series;
import net.sicade.observation.coverage.sql.TreeDepth;
import net.sicade.sie.window.mosaic.OpenAction;


/**
 * Noeud repr�sentant une th�matique, une op�ration ou une s�rie d'images. Ces noeuds ont
 * soit {@link RootNode} ou soit un autre {@code ChildNode} comme parent.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class ChildNode extends AbstractNode {
    /**
     * Chemin vers l'icone repr�sentant ce noeud, or {@code null} si aucun icone n'a �t� d�finie.
     */
    private static final String THEMATIC_ICON = "toolbarButtonGraphics/general/Zoom16.gif";

    /**
     * Chemin vers l'icone repr�sentant ce noeud, or {@code null} si aucun icone n'a �t� d�finie.
     */
    private static final String OPERATION_ICON = "toolbarButtonGraphics/development/Host16.gif";

    /**
     * Chemin vers l'icone repr�sentant ce noeud, or {@code null} si aucun icone n'a �t� d�finie.
     */
    private static final String SERIES_ICON = "toolbarButtonGraphics/media/Movie16.gif";

    /**
     * Les actions applicable sur ce noeud.
     */
    private transient Action[] actions;

    /**
     * La th�matique, op�ration ou s�rie de donn�es repr�sent�e par ce noeud.
     */
    private final Element element;

    /**
     * Construit un noeud pour la th�matique, l'op�ration ou la s�rie d'images sp�cifi�e.
     *
     * @param element La th�matique, op�ration ou s�rie d'images.
     * @param children Les enfants, ou {@code null} si aucun.
     */
    public ChildNode(final Element element, final Children children, final TreeDepth type) {
        super(children!=null ? children : Children.LEAF);
        this.element = element;
        setName(element.getName());
        final String path;
        switch (type) {
            case THEMATIC:  path = THEMATIC_ICON;  break;
            case PROCEDURE: path = OPERATION_ICON; break;
            case SERIES:    path = SERIES_ICON;    break;
            default:        path = null;           break;
        }
        if (path != null) {
            setIconBaseWithExtension(path);
        }
    }

    /**
     * Construit une feuille de propri�t�s pour ce noeud.
     *
     * @todo Prendre en compte la r�solution.
     * @todo La description ne semble pas fonctionner (except� si le texte est vraiment tr�s court)...
     */
    @Override
    protected Sheet createSheet() {
        if (element instanceof Series) {
            final Sheet sheet;
            final Series series = (Series) element;
            try {
                sheet = SimpleProperty.createSheet(series.getGeographicBoundingBox(),
                                                   series.getTimeRange(), null);
            } catch (CatalogException e) {
                ErrorManager.getDefault().notify(e);
                return super.createSheet();
            }
            final Sheet.Set s = sheet.get(Sheet.PROPERTIES);
            s.setShortDescription(series.getRemarks());
            return sheet;
        }
        return super.createSheet();
    }

    /**
     * Retourne la liste des actions disponibles pour ce noeud. Si l'�l�ment envelopp� par ce
     * noeud est une {@linkplain Series s�ries d'images}, alors le menu propos� comprendra les
     * items "Ouvrir" et "Propri�t�s".
     */
    @Override
    public Action[] getActions(final boolean context) {
        if (context || !(element instanceof Series)) {
            return super.getActions(context);
        }
        if (actions == null) {
            final Series series = (Series) element;
            actions = new Action[] {
                new OpenAction(series),
                null,
                new PropertiesAction()
            };
        }
        return actions;
    }

    /**
     * Retourne l'action par d�faut. Cette action sera ex�cut�e lorsque l'utilisateur
     * effectuera un double-clique sur ce noeud. L'impl�mentation par d�faut retourne
     * la premi�re action de celles qui sont retourn�es par {@link #getActions}, qui
     * correspondra habituellement � l'action "Ouvrir".
     */
    @Override
    public Action getPreferredAction() {
        final Action[] actions = getActions(false);
        return (actions!=null && actions.length!=0) ? actions[0] : super.getPreferredAction();
    }

    /** Action affichant les propri�t�s du noeud. */
    private final class PropertiesAction extends AbstractAction {
        /** Construit l'action. */
        public PropertiesAction() {
            super(NbBundle.getMessage(ChildNode.class, "Properties"));
        }

        /** Ex�cute l'action. */
        public void actionPerformed(final ActionEvent event) {
            NodeOperation.getDefault().showProperties(ChildNode.this);
        }
    }
}
