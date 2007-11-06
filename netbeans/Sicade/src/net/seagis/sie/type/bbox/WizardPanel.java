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
import java.util.Set;
import java.util.HashSet;
import javax.swing.JComponent;
import javax.swing.text.Document;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

// OpenIDE dependencies
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.WizardDescriptor;


/**
 * Un paneau de l'assistant. Ces paneaux sont construits par l'{@linkplain WizardIterator itérateur}
 * et contiennent des informations tels que le nom ou l'aide contextuelle, mais pas les composantes
 * visuelles elle-mêmes. Pour des raisons de performances, ces dernières ne seront construites que
 * lorsque la méthode {@link #getComponent} sera appelée.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class WizardPanel implements WizardDescriptor.Panel, DocumentListener {
    /**
     * L'étape de cet assistant, à partir de 0.
     */
    private final int step;

    /**
     * La composante visuelle propre à ce paneau.
     */
    private JComponent component;

    /**
     * L'ensemble des objets à informer des changement survenant dans ce paneau.
     */
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>();

    /**
     * Les informations obligatoires qui n'ont pas été remplie par l'utilisateur.
     * Ce paneau ne sera considéré valide que si cet ensemble est vide.
     */
    private final Set<Document> missing = new HashSet<Document>();

    /**
     * Construit le paneau.
     *
     * @param step L'étape de cet assistant, à partir de 0.
     */
    public WizardPanel(final int step) {
        this.step = step;
    }

    /**
     * Retourne la composante visuelle propre à ce paneau. Cette méthode peut être appelée
     * à partir de n'importe quel thread (pas nécessairement celui de <cite>Swing</cite>).
     */
    public JComponent getComponent() {
        if (component == null) {
            switch (step) {
                case  0: component = new WizardStep1(this); break;
                case  1: component = new WizardStep2();     break;
                default: throw new IllegalStateException(String.valueOf(step));
            }
            // Le numéro suivant indique la position (à partir de 0)
            // à laquelle est sensée apparaître cette étape.
            component.putClientProperty("WizardPanel_contentSelectedIndex", step);
            component.setName(NbBundle.getMessage(WizardPanel.class, "Wizard_"+(step+1)));
        }
        return component;
    }

    /**
     * Retourne l'aide contextuelle pour ce paneau.
     */
    public HelpCtx getHelp() {
        if (false) {
            // If you have context help:
            return new HelpCtx(WizardPanel.class);
        } else {
            // Show no Help button for this panel:
            return HelpCtx.DEFAULT_HELP;
        }
    }

    /**
     * Retourne {@code true} si l'utilisateur peut appuyer sur "Suivant" ou "Terminer".
     * Si cette méthode retourne {@code false}, alors tous les objets enregistrés avec
     * {@link #addChangeListener addChangeListener} seront informés lorsque les conditions
     * changeront et que cette méthode retournera {@code true}.
     */
    public boolean isValid() {
        return missing.isEmpty();
    }

    /**
     * Ajoute un objet à informer losque la valeur retournée par {@link #isValid} changera.
     */
    public final void addChangeListener(final ChangeListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Retire un objet à informer losque la valeur retournée par {@link #isValid} change.
     */
    public final void removeChangeListener(final ChangeListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * Informe tous les objets intéressés que la valeur retournée par {@link #isValid} a changé.
     */
    private void fireChangeEvent() {
        final ChangeEvent event = new ChangeEvent(this);
        synchronized (listeners) {
            for (final ChangeListener listener : listeners) {
                listener.stateChanged(event);
            }
        }
    }

    /**
     * Méthode appelée automatiquement chaque fois que l'utilisateur a modifié le contenu d'un
     * champ obligatoire. Si ce champ est devenu vide, alors cette méthode lancera un événement
     * informant les objets intéressés que ce paneau est devenu invalide.
     */
    private void mandatoryFieldUpdated(final Document document) {
        final boolean changed;
        if (document.getLength() == 0) {
            changed = missing.add(document);
        } else {
            changed = missing.remove(document);
        }
        if (changed) {
            fireChangeEvent();
        }
    }

    /**
     * Méthode appelée automatiquement chaque fois que l'utilisateur a modifié le contenu d'un
     * champ obligatoire.
     */
    public void insertUpdate(final DocumentEvent event) {
        mandatoryFieldUpdated(event.getDocument());
    }

    /**
     * Méthode appelée automatiquement chaque fois que l'utilisateur a modifié le contenu d'un
     * champ obligatoire.
     */
    public void removeUpdate(final DocumentEvent event) {
        mandatoryFieldUpdated(event.getDocument());
    }

    /**
     * Méthode appelée automatiquement chaque fois que l'utilisateur a modifié le contenu d'un
     * champ obligatoire.
     */
    public void changedUpdate(final DocumentEvent event) {
        mandatoryFieldUpdated(event.getDocument());
    }

    /**
     * Modifie l'état de ce paneau à partir des informations contenues dans l'objet spécifié.
     * L'objet {@code settings} sera habituellement une instance de {@link BoundingBox}.
     */
    public void readSettings(final Object settings) {
        final BoundingBox bbox = (BoundingBox) settings;
        final JComponent component = getComponent();
        if (component instanceof WizardStep) {
            ((WizardStep) component).readSettings(bbox);
        }
    }

    /**
     * Enregistre l'état de ce paneau dans l'objet spécifié.
     * L'objet {@code settings} sera habituellement une instance de {@link BoundingBox}.
     */
    public void storeSettings(final Object settings) {
        final BoundingBox bbox = (BoundingBox) settings;
        final JComponent component = getComponent();
        if (component instanceof WizardStep) {
            ((WizardStep) component).storeSettings(bbox);
        }
    }
}
