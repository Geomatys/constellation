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
 * Fenêtre qui affichera l'arborescence des séries d'images. Cette fenêtre peut être affichée
 * par {@link ViewAction}, une action qui sera proposée dans le menu "Window".
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ExplorerWindow extends TopComponent implements ExplorerManager.Provider {
    /**
     * Pour compatibilité avec différentes versions de cette classe.
     */
    private static final long serialVersionUID = -144435609721401628L;

    /**
     * Une instance unique créée par {@link #getDefault} la première fois où cette dernière
     * sera appelée. Cette instance n'est utilisée que lors de la construction de la fenêtre
     * à partir d'un flot binaire.
     */
    private static ExplorerWindow instance;

    /**
     * Chemin vers l'icône utilisée pour cette fenêtre ainsi que pour l'action
     * {@link ViewAction} qui l'ouvrira.
     */
    static final String ICON_PATH = "net/sicade/sie/window/series/Icon.gif";

    /**
     * Une chaîne de caractères qui représentera cette fenêtre au sein du {@linkplain WindowManager
     * gestionnaire des fenêtres}. Cet ID sert à obtenir une instance unique de cette fenêtre par
     * un appel à la méthode {@link #findInstance}.
     */
    private static final String PREFERRED_ID = "ExplorerWindow";

    /**
     * Le gestionnaire de l'arborescence qui contiendra la liste des séries
     * pour différentes régions géographiques.
     */
    private final ExplorerManager manager = new ExplorerManager();

    /**
     * Construit une fenêtre contenant une arborescence initialement vide.
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
         * Définit le noeud qui servira de racine. Cette racine ne sera pas
         * nécessairement visible (ça dépend de la configuration de 'view').
         * En cas d'erreur, aucune racine ne sera définie et l'explorateur
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
         * Spécifie l'objet à utiliser pour trouver les services (implémentations d'une interface
         * donnée). L'objet spécifié ici ce mettra à jour lui-même en fonction de l'état de
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
     * Retourne une instance par défaut. <strong>N'appellez pas cette méthode directement!</strong>
     * Cette méthode est public pour les besoins de la plateforme Netbeans, mais réservée à un usage
     * interne par les fichiers {@code *.settings}, c'est-à-dire durant les lectures à partir d'un flot
     * binaire. Pour obtenir un singleton dans les tous les autres cas, utilisez {@link #findInstance}.
     */
    public static synchronized ExplorerWindow getDefault() {
        if (instance == null) {
            instance = new ExplorerWindow();
        }
        return instance;
    }

    /**
     * Obtient une instance unique d'une fenêtre de cette classe. Utilisez cette méthode
     * plutôt que {@link #getDefault}.
     */
    public static synchronized ExplorerWindow findInstance() {
        final TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        final String message;
        if (win == null) {
            message = "Aucune composante de type \"" + PREFERRED_ID + "\". " +
                      "La fenêtre ne sera pas positionnée correctement.";
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
     * Retourne l'identifiant des fenêtres de type {@code ExplorerWindow} dans le
     * gestionnaire des fenêtres.
     */
    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    /**
     * Spécifie de manière explicite que le type de persistence doit être
     * {@link #PERSISTENCE_ALWAYS PERSISTENCE_ALWAYS}. La surcharge de cette
     * méthode est nécessaire pour éviter un avertissement au moment de l'exécution.
     */
    @Override
    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }

    /**
     * Retourne le gestionnaire de l'explorateur affiché par cette composante.
     */
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    /**
     * Active tous les "listeners" qui avaient été désactivés lorsque cette composante a été
     * cachée.
     */
    @Override
    protected void componentActivated() {
        super.componentActivated();
        ExplorerUtils.activateActions(manager, true);
    }

    /**
     * Désactive tous les "listeners" lorsque cette composante est cachée.
     */
    @Override
    protected void componentDeactivated() {
        ExplorerUtils.activateActions(manager, false);
        super.componentDeactivated();
    }

    /**
     * Lors de l'écriture en binaire de cette fenêtre, écrit une classe sentinelle
     * à la place de la totalité de {@code ExplorerWindow}.
     */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    /**
     * Les classes qui seront enregistrées en binaire à la place de {@link ExplorerWindow}.
     * Lors de la lecture, cette classe appelera {@link ExplorerWindow#getDefault} afin de
     * reconstruire une fenêtre qui apparaîtra dans l'application de l'utilisateur.
     *
     * @author Martin Desruisseaux
     * @version $Id$
     */
    final static class ResolvableHelper implements Serializable {
        /**
         * Pour compatibilité avec différentes versions de cette classe.
         */
        private static final long serialVersionUID = 3743903472227127764L;

        /**
         * Lors de la lecture binaire, remplace cet objet par une instance de la fenêtre
         * {@link ExplorerWindow}.
         */
        public Object readResolve() {
            return ExplorerWindow.getDefault();
        }
    }
}
