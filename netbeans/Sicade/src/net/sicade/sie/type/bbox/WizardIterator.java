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
package net.sicade.sie.type.bbox;

// J2SE dependencies
import java.util.Set;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.io.IOException;
import java.awt.Image;
import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

// OpenIDE dependencies
import org.openide.util.NbBundle;
import org.openide.WizardDescriptor;

// Sicade dependencies
import net.sicade.resources.XArray;


/**
 * Itérateur sur les différentes étapes de l'assistant "choix d'une région d'intérêt". Chaque
 * étape est représenté par un objet {@code WizardPanel}. Cet assistant proposera les étapes
 * suivantes:
 * <p>
 * <ul>
 *   <li>Choix d'un serveur de données</li>
 *   <li>Choix de l'envelope spatio-temporelle</li>
 * </ul>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class WizardIterator implements WizardDescriptor.InstantiatingIterator {
    /**
     * Descripteur de l'assistant. La valeur de ce champ n'est pas déterminée par cet itérateur.
     * C'est l'objet {@link WizardDescriptor} lui-même qui fournira cette information par un appel
     * à la methode {@link #initialize}.
     */
    private WizardDescriptor wizard;

    /**
     * Liste des paneaux constituant les différentes étapes de cet assistant. Cette liste est
     * initialement nulle. Elle ne sera construite que la première fois où l'itérateur sera
     * {@linkplain #initialize initialisé}.
     */
    private WizardDescriptor.Panel[] panels;

    /**
     * Index de l'étape en cours, de 0 inclusivement jusqu'à {@code panels.length} exclusivement.
     */
    private int index;

    /**
     * Construit un itérateur. La méthode {@link #initialize initialize} devra être appelée avant
     * que cet itérateur ne soit utilisable.
     */
    public WizardIterator() {
    }

    /**
     * Initialise cet itérateur. Cette méthode est appelée automatiquement par l'objet
     * {@code wizard} donné en argument.
     */
    public void initialize(final WizardDescriptor wizard) {
        this.wizard = wizard;
        if (panels != null) {
            return;
        }
        panels = new WizardDescriptor.Panel[2];
        final String[] steps = new String[panels.length];
        for (int i=0; i<panels.length; i++) {
            panels[i] = new WizardPanel(i);
            steps [i] = NbBundle.getMessage(WizardPanel.class, "Wizard_" + (i+1));
        }
        final Image  image = null; // TODO
        final String error = NbBundle.getMessage(WizardPanel.class, "ERR_MandatoryField");
        wizard.putProperty("WizardPanel_contentData",     steps); // Sets steps names for a panel
        wizard.putProperty("WizardPanel_autoWizardStyle",  true); // Turn on subtitle creation on each step
        wizard.putProperty("WizardPanel_contentDisplayed", true); // Show steps on the left side with the image on the background
        wizard.putProperty("WizardPanel_contentNumbered",  true); // Turn on numbering of all steps
//      wizard.putProperty("WizardPanel_image",           image); // Sets image displayed as background of content
        wizard.putProperty("WizardPanel_errorMessage",    error); // The localized message at the bottom of the wizard panel
    }

    /**
     * Dispose de cet itérateur. Cette méthode est appelée automatiquement lorsque l'assistant
     * est fermé.
     */
    public void uninitialize(final WizardDescriptor wizard) {
        panels = null;
    }

    /**
     * Retourne les objets créés par cet assistant.
     */
    public Set instantiate() throws IOException {
        return Collections.EMPTY_SET;
    }

    /**
     * Retourne le paneau courant.
     */
    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    /**
     * Retourne le nom du paneau courant. Ce nom apparaîtra entre parenthèses après le nom
     * de l'étape. L'implémentation par défaut retourne "(<var>x</var> de <var>y</var>)" où
     * <var>x</var> et le numéro de l'étape en cours et <var>y</var> le nombre d'étapes. Le
     * résultat ressemblera à "Serveur de données (1 de 2)".
     */
    public String name() {
        return (index + 1) + " de " + panels.length;
    }

    /**
     * Retourne {@code true} s'il y a d'autres paneaux à retourner.
     */
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    /**
     * Retourne {@code true} s'il est possible de revenir en arrière.
     */
    public boolean hasPrevious() {
        return index > 0;
    }

    /**
     * Avance au paneau suivant. Cette méthode ne fait qu'incrémenter le compteur;
     * elle ne modifie par l'interface utilisateur elle-même. Cette interface sera
     * obtenue par un appel à {@link #current}.
     *
     * @throws NoSuchElementException s'il n'y a pas de paneau suivant.
     */
    public void nextPanel() throws NoSuchElementException {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }
    
    /**
     * Recule au paneau précédent. Cette méthode ne fait que décrémenter le compteur;
     * elle ne modifie par l'interface utilisateur elle-même. Cette interface sera
     * obtenue par un appel à {@link #current}.
     *
     * @throws NoSuchElementException s'il n'y a pas de paneau précédent.
     */
    public void previousPanel() throws NoSuchElementException {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    /**
     * Ajoute un objet à informer si la possibilité d'avancer ou de reculer change.
     */
    public void addChangeListener(final ChangeListener listener) {
        // Rien à faire, puisque le nombre de paneaux reste fixe.
    }

    /**
     * Retire un objet à informer si la possibilité d'avancer ou de reculer change.
     */
    public void removeChangeListener(final ChangeListener listener) {
        // Rien à faire, puisque le nombre de paneaux reste fixe.
    }
    
    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then uncomment
    // the following and call when needed: fireChangeEvent();
    /*
    private transient Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        listeners = new HashSet<ChangeListener>(1);
    }
     */
}
