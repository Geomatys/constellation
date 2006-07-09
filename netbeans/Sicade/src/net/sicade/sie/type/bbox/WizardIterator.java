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
 * It�rateur sur les diff�rentes �tapes de l'assistant "choix d'une r�gion d'int�r�t". Chaque
 * �tape est repr�sent� par un objet {@code WizardPanel}. Cet assistant proposera les �tapes
 * suivantes:
 * <p>
 * <ul>
 *   <li>Choix d'un serveur de donn�es</li>
 *   <li>Choix de l'envelope spatio-temporelle</li>
 * </ul>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class WizardIterator implements WizardDescriptor.InstantiatingIterator {
    /**
     * Descripteur de l'assistant. La valeur de ce champ n'est pas d�termin�e par cet it�rateur.
     * C'est l'objet {@link WizardDescriptor} lui-m�me qui fournira cette information par un appel
     * � la methode {@link #initialize}.
     */
    private WizardDescriptor wizard;

    /**
     * Liste des paneaux constituant les diff�rentes �tapes de cet assistant. Cette liste est
     * initialement nulle. Elle ne sera construite que la premi�re fois o� l'it�rateur sera
     * {@linkplain #initialize initialis�}.
     */
    private WizardDescriptor.Panel[] panels;

    /**
     * Index de l'�tape en cours, de 0 inclusivement jusqu'� {@code panels.length} exclusivement.
     */
    private int index;

    /**
     * Construit un it�rateur. La m�thode {@link #initialize initialize} devra �tre appel�e avant
     * que cet it�rateur ne soit utilisable.
     */
    public WizardIterator() {
    }

    /**
     * Initialise cet it�rateur. Cette m�thode est appel�e automatiquement par l'objet
     * {@code wizard} donn� en argument.
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
     * Dispose de cet it�rateur. Cette m�thode est appel�e automatiquement lorsque l'assistant
     * est ferm�.
     */
    public void uninitialize(final WizardDescriptor wizard) {
        panels = null;
    }

    /**
     * Retourne les objets cr��s par cet assistant.
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
     * Retourne le nom du paneau courant. Ce nom appara�tra entre parenth�ses apr�s le nom
     * de l'�tape. L'impl�mentation par d�faut retourne "(<var>x</var> de <var>y</var>)" o�
     * <var>x</var> et le num�ro de l'�tape en cours et <var>y</var> le nombre d'�tapes. Le
     * r�sultat ressemblera � "Serveur de donn�es (1 de 2)".
     */
    public String name() {
        return (index + 1) + " de " + panels.length;
    }

    /**
     * Retourne {@code true} s'il y a d'autres paneaux � retourner.
     */
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    /**
     * Retourne {@code true} s'il est possible de revenir en arri�re.
     */
    public boolean hasPrevious() {
        return index > 0;
    }

    /**
     * Avance au paneau suivant. Cette m�thode ne fait qu'incr�menter le compteur;
     * elle ne modifie par l'interface utilisateur elle-m�me. Cette interface sera
     * obtenue par un appel � {@link #current}.
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
     * Recule au paneau pr�c�dent. Cette m�thode ne fait que d�cr�menter le compteur;
     * elle ne modifie par l'interface utilisateur elle-m�me. Cette interface sera
     * obtenue par un appel � {@link #current}.
     *
     * @throws NoSuchElementException s'il n'y a pas de paneau pr�c�dent.
     */
    public void previousPanel() throws NoSuchElementException {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    /**
     * Ajoute un objet � informer si la possibilit� d'avancer ou de reculer change.
     */
    public void addChangeListener(final ChangeListener listener) {
        // Rien � faire, puisque le nombre de paneaux reste fixe.
    }

    /**
     * Retire un objet � informer si la possibilit� d'avancer ou de reculer change.
     */
    public void removeChangeListener(final ChangeListener listener) {
        // Rien � faire, puisque le nombre de paneaux reste fixe.
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
