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
import java.io.Serializable;
import javax.swing.InputMap;
import javax.swing.ActionMap;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;
import javax.swing.tree.TreeSelectionModel;

// OpenIDE dependencies
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.ExplorerManager;
import org.openide.loaders.DataObjectNotFoundException;


/**
 * Fen�tre qui affichera l'arborescence des s�ries d'images. Cette fen�tre peut �tre affich�e
 * par {@link ViewAction}, une action qui sera propos�e dans le menu "Window".
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ExplorerWindow extends TopComponent implements ExplorerManager.Provider {
    /**
     * Pour compatibilit� avec diff�rentes versions de cette classe.
     */
    private static final long serialVersionUID = -144435609721401628L;

    /**
     * Une instance unique cr��e par {@link #getDefault} la premi�re fois o� cette derni�re
     * sera appel�e. Cette instance n'est utilis�e que lors de la construction de la fen�tre
     * � partir d'un flot binaire.
     */
    private static ExplorerWindow instance;

    /**
     * Chemin vers l'ic�ne utilis�e pour cette fen�tre ainsi que pour l'action
     * {@link ViewAction} qui l'ouvrira.
     */
    static final String ICON_PATH = "net/sicade/sie/window/series/Icon.gif";

    /**
     * Une cha�ne de caract�res qui repr�sentera cette fen�tre au sein du {@linkplain WindowManager
     * gestionnaire des fen�tres}. Cet ID sert � obtenir une instance unique de cette fen�tre par
     * un appel � la m�thode {@link #findInstance}.
     */
    private static final String PREFERRED_ID = "ExplorerWindow";

    /**
     * Le gestionnaire de l'arborescence qui contiendra la liste des s�ries
     * pour diff�rentes r�gions g�ographiques.
     */
    private final ExplorerManager manager = new ExplorerManager();

    /**
     * Construit une fen�tre contenant une arborescence initialement vide.
     */
    private ExplorerWindow() {
        initComponents();
        final Class c = ExplorerWindow.class;
        setName       (PREFERRED_ID);
        setDisplayName(NbBundle.getMessage(c, "WindowTitle"));
        setToolTipText(NbBundle.getMessage(c, "WindowHint"));
        setIcon       (Utilities.loadImage(ICON_PATH, true));
        view.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        /*
         * D�finit le noeud qui servira de racine. Cette racine ne sera pas
         * n�cessairement visible (�a d�pend de la configuration de 'view').
         * En cas d'erreur, aucune racine ne sera d�finie et l'explorateur
         * restera vide.
         */
        try {
            manager.setRootContext(new RootNode());
        } catch (DataObjectNotFoundException e) {
            ErrorManager.getDefault().notify(e);
        }
        /*
         * Ajoute les actions copier, coller et effacer.
         */
        final ActionMap map = getActionMap();
        if (false) {
            map.put(DefaultEditorKit.copyAction,  ExplorerUtils.actionCopy (manager));
            map.put(DefaultEditorKit.cutAction,   ExplorerUtils.actionCut  (manager));
            map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        }
        map.put("delete", ExplorerUtils.actionDelete(manager, true));
        final InputMap keys = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        if (false) {
            keys.put(KeyStroke.getKeyStroke("control C"), DefaultEditorKit.copyAction);
            keys.put(KeyStroke.getKeyStroke("control X"), DefaultEditorKit.cutAction);
            keys.put(KeyStroke.getKeyStroke("control V"), DefaultEditorKit.pasteAction);
        }
        keys.put(KeyStroke.getKeyStroke("DELETE"), "delete");
        /*
         * Sp�cifie l'objet � utiliser pour trouver les services (impl�mentations d'une interface
         * donn�e). L'objet sp�cifi� ici ce mettra � jour lui-m�me en fonction de l'�tat de
         * l'explorateur.
         */
        associateLookup(ExplorerUtils.createLookup(manager, map));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());

        view.setRootVisible(false);
        add(view, java.awt.BorderLayout.CENTER);

    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final org.openide.explorer.view.BeanTreeView view = new org.openide.explorer.view.BeanTreeView();
    // End of variables declaration//GEN-END:variables

    /**
     * Retourne une instance par d�faut. <strong>N'appellez pas cette m�thode directement!</strong>
     * Cette m�thode est public pour les besoins de la plateforme Netbeans, mais r�serv�e � un usage
     * interne par les fichiers {@code *.settings}, c'est-�-dire durant les lectures � partir d'un flot
     * binaire. Pour obtenir un singleton dans les tous les autres cas, utilisez {@link #findInstance}.
     */
    public static synchronized ExplorerWindow getDefault() {
        if (instance == null) {
            instance = new ExplorerWindow();
        }
        return instance;
    }

    /**
     * Obtient une instance unique d'une fen�tre de cette classe. Utilisez cette m�thode
     * plut�t que {@link #getDefault}.
     */
    public static synchronized ExplorerWindow findInstance() {
        final TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        final String message;
        if (win == null) {
            message = "Aucune composante de type \"" + PREFERRED_ID + "\". " +
                      "La fen�tre ne sera pas positionn�e correctement.";
        } else if (win instanceof ExplorerWindow) {
            return (ExplorerWindow) win;
        } else {
            message = "Il semble y avoir plusieurs composantes de type \"" + PREFERRED_ID + "\". " +
                      "L'application peut ne pas fonctionner correctement.";
        }
        ErrorManager.getDefault().log(ErrorManager.WARNING, message);
        return getDefault();
    }

    /**
     * Retourne l'identifiant des fen�tres de type {@code ExplorerWindow} dans le
     * gestionnaire des fen�tres.
     */
    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    /**
     * Sp�cifie de mani�re explicite que le type de persistence doit �tre
     * {@link #PERSISTENCE_ALWAYS PERSISTENCE_ALWAYS}. La surcharge de cette
     * m�thode est n�cessaire pour �viter un avertissement au moment de l'ex�cution.
     */
    @Override
    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }

    /**
     * Retourne le gestionnaire de l'explorateur affich� par cette composante.
     */
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    /**
     * Active tous les "listeners" qui avaient �t� d�sactiv�s lorsque cette composante a �t�
     * cach�e.
     */
    @Override
    protected void componentActivated() {
        super.componentActivated();
        ExplorerUtils.activateActions(manager, true);
    }

    /**
     * D�sactive tous les "listeners" lorsque cette composante est cach�e.
     */
    @Override
    protected void componentDeactivated() {
        ExplorerUtils.activateActions(manager, false);
        super.componentDeactivated();
    }

    /**
     * Lors de l'�criture en binaire de cette fen�tre, �crit une classe sentinelle
     * � la place de la totalit� de {@code ExplorerWindow}.
     */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    /**
     * Les classes qui seront enregistr�es en binaire � la place de {@link ExplorerWindow}.
     * Lors de la lecture, cette classe appelera {@link ExplorerWindow#getDefault} afin de
     * reconstruire une fen�tre qui appara�tra dans l'application de l'utilisateur.
     *
     * @author Martin Desruisseaux
     * @version $Id$
     */
    final static class ResolvableHelper implements Serializable {
        /**
         * Pour compatibilit� avec diff�rentes versions de cette classe.
         */
        private static final long serialVersionUID = 3743903472227127764L;

        /**
         * Lors de la lecture binaire, remplace cet objet par une instance de la fen�tre
         * {@link ExplorerWindow}.
         */
        public Object readResolve() {
            return ExplorerWindow.getDefault();
        }
    }
}
