/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.gui.observation;

// J2SE dependencies
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import static java.lang.Boolean.TRUE;
import static java.lang.Boolean.FALSE;

// AWT/Swing dependencies
import java.awt.Font;
import java.awt.Window;
import java.awt.Component;
import java.awt.Container;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.ListModel;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.AbstractListModel;
import javax.swing.ListSelectionModel;

// Geotools dependencies
import org.geotoolkit.gui.swing.DisjointLists;
import org.geotools.resources.SwingUtilities;

// J2SE dependencies
import org.constellation.resources.XArray;
import org.constellation.catalog.Element;
import org.constellation.coverage.catalog.Catalog;
import org.constellation.catalog.CatalogException;
import org.constellation.coverage.catalog.Layer;
import org.constellation.coverage.model.Operation;
import org.constellation.coverage.model.Descriptor;
import org.constellation.coverage.model.RegionOfInterest;


/**
 * Sélectionne des descripteurs parmis une liste.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@SuppressWarnings("serial")
public class DescriptorChooser extends JPanel {
    /**
     * Une liste d'éléments mémorisée sour forme de tableau. Ce modèle sera utilisé
     * par les listes de la première étape.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    private static final class Model extends AbstractListModel {
        /**
         * Les éléments dans cette liste.
         */
        private Element[] elements = new Element[0];

        /** Spécifie l'ensemble des éléments à affecter à cette liste. */
        public void setElements(final Set<? extends Element> newElements) {
            final int newLength = newElements.size();
            final int length = (elements != null) ? Math.max(elements.length, newLength) : newLength;
            elements = newElements.toArray(new Element[newLength]);
            if (length != 0) {
                fireContentsChanged(this, 0, length-1);
            }
        }

        /** Retourne le nombre d'éléments dans cette liste. */
        public int getSize() {
            return elements.length;
        }

        /** Retourne l'élément à l'index spécifié. */
        public Element getElementAt(final int index) {
            return elements[index];
        }
    }

    /**
     * Action exécuté lorsque l'un des boutons est appuyé.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    private final class Action implements ActionListener {
        public void actionPerformed(final ActionEvent event) {
            final Object source = event.getSource();
            if (source == previous) {
                setStep(SELECT_PROPERTIES);
                return;
            }
            if (source == next) {
                if (SELECT_DESCRIPTORS.equals(currentStep)) {
                    previous.setEnabled(false);
                    next    .setEnabled(false);
                    saveSelection();
                    execute();
                } else {
                    setStep(SELECT_DESCRIPTORS);
                }
                return;
            }
            if (source == cancel) {
                cancel();
                return;
            }
        }
    }


    //////////////////////////////// fin des classes internes ///////////////////////////////////


    /**
     * Nom de la première étape, qui consiste à choisir les {@linkplain Layer couches},
     * {@linkplain Operation opérations} et {@link RegionOfInterest positions relatives}.
     * Ce nom peut être spécifié en argument à {@link #setStep} pour sélectionner les
     * listes à afficher.
     */
    public static final String SELECT_PROPERTIES = "SelectProperties";

    /**
     * Nom de la seconde étape, qui consiste à choisir les {@linkplain Descriptor descripteurs}
     * eux-mêmes. Ce nom peut être spécifié en argument à {@link #setStep} pour sélectionner les
     * listes à afficher.
     */
    public static final String SELECT_DESCRIPTORS = "SelectDescriptors";

    /**
     * Le nom de la feuille de préférences pour les derniers descripteurs utilisés.
     */
    private static final String LAST_DESCRIPTORS = "LastDescriptors";

    /**
     * Ensemble des {@linkplain Descriptor descripteurs} dont on veut répartir les composantes
     * dans les trois listes {@code phenomenons}, {@code operations} et {@code offsets}.
     */
    private final Map<Descriptor,Boolean> descriptors = new LinkedHashMap<Descriptor,Boolean>();

    /**
     * Ensemble des {@linkplain Layer couches} d'images.
     */
    private final Map<Layer,Boolean> layers = new LinkedHashMap<Layer,Boolean>();

    /**
     * Ensemble des {@linkplain Operation opérations}.
     */
    private final Map<Operation,Boolean> operations = new LinkedHashMap<Operation,Boolean>();
    
    /**
     * Ensemble des {@linkplain RegionOfInterest positions spatio-temporelles relatives}.
     */
    private final Map<RegionOfInterest,Boolean> offsets = new LinkedHashMap<RegionOfInterest,Boolean>();

    /**
     * Liste des {@linkplain Layer couches} d'images.
     */
    private final JList layerList;

    /**
     * Liste des {@linkplain Operation opérations}.
     */
    private final JList operationList;

    /**
     * Liste des {@linkplain RegionOfInterest positions spatio-temporelles relatives}.
     */
    private final JList offsetList;

    /**
     * List des {@linkplain Descriptor descripteurs}.
     */
    private final DisjointLists descriptorList;

    /**
     * L'étape courante, comme une des constantes {@link #SELECT_PROPERTIES} ou
     * {@link #SELECT_DESCRIPTORS}.
     */
    private String currentStep;

    /**
     * La composante graphique qui affichera les différentes étapes.
     */
    private final JPanel cards;

    /**
     * Le bouton pour revenir à l'étape précédente.
     */
    private final JButton previous;

    /**
     * Le bouton pour aller à l'étape suivante.
     */
    private final JButton next;

    /**
     * Le bouton pour annuler.
     */
    private final JButton cancel;

    /**
     * Les préférences à utiliser pour mémoriser les derniers descripteurs utilisés.
     */
    private final Preferences preferences = Preferences.userNodeForPackage(DescriptorChooser.class);

    /**
     * Construit une nouvelle composante initialement vide.
     */
    public DescriptorChooser() {
        super(new BorderLayout(0,9));
        cards = new JPanel(new CardLayout());
        /*
         * Construction de l'étape 1.
         */
        JPanel step = new JPanel(new BorderLayout(0,9));
        JPanel pane = new JPanel(new GridLayout(1,3,9,0));
        pane.add(new JLabel("Couches d'images", JLabel.CENTER));
        pane.add(new JLabel("Opérations",       JLabel.CENTER));
        pane.add(new JLabel("Décalages",        JLabel.CENTER));
        step.add(pane, BorderLayout.NORTH);
        pane = new JPanel(new GridLayout(1,3,9,0));
        pane.add(new JScrollPane(layerList     = new JList(new Model())));
        pane.add(new JScrollPane(operationList = new JList(new Model())));
        pane.add(new JScrollPane(offsetList    = new JList(new Model())));
        step.add(pane, BorderLayout.CENTER);
        cards.add(step, SELECT_PROPERTIES);
        /*
         * Construction de l'étape 2.
         */
        descriptorList = new DisjointLists();
        descriptorList.setAutoSortEnabled(false);
        descriptorList.setFont(new Font("Arial Unicode MS", Font.PLAIN, 16));
        cards.add(descriptorList, SELECT_DESCRIPTORS);
        /*
         * Construction de la barre des boutons.
         */
        add(cards, BorderLayout.CENTER);
        pane = new JPanel(new GridLayout(1,3,9,0));
        pane.add(previous = new JButton("Précédent"));
        pane.add(next     = new JButton("Suivant"));
        pane.add(cancel   = new JButton("Annuler"));
        pane.setBorder(BorderFactory.createEmptyBorder(15,0,0,0));
        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(pane);
        add(box, BorderLayout.SOUTH);
        setBorder(BorderFactory.createCompoundBorder(getBorder(),
                  BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        /*
         * Définition des actions.
         */
        final Action action = new Action();
        previous.addActionListener(action);
        next    .addActionListener(action);
        cancel  .addActionListener(action);
        setStep(SELECT_PROPERTIES);
    }

    /**
     * Construit une nouvelle composante initialisée avec l'ensemble des descripteurs spécifié.
     * Les descripteurs qui avaient été choisis par l'utilisateur lors de la dernière utilisation
     * de cette composante seront automatiquement sélectionnés.
     */
    public DescriptorChooser(final Collection<Descriptor> descriptors) {
        this();
        addDescriptor(descriptors);
        loadSelection();
    }

    /**
     * Ajoute à cette composante l'ensemble des descripteurs spécifié. Aucun des nouveaux
     * descripteurs ne sera initiallement sélectionné. Afin de pré-sélectionner ceux qui
     * avaient été choisis par l'utilisateur lors de la dernière utilisation de cette
     * composante, appelez {@link #loadSelection}.
     */
    public void addDescriptor(final Collection<Descriptor> toAdd) {
        boolean seriesModified    = false;
        boolean operationModified = false;
        boolean offsetModified    = false;
        for (final Descriptor descriptor : toAdd) {
            if (add(descriptors, descriptor)) {
                seriesModified    |= add(layers,     descriptor.getLayer());
                operationModified |= add(operations, descriptor.getOperation());
                offsetModified    |= add(offsets,    descriptor.getRegionOfInterest());
            }
        }
        if (seriesModified)    setElements(layerList,     layers);
        if (operationModified) setElements(operationList, operations);
        if (offsetModified)    setElements(offsetList,    offsets);
    }

    /**
     * Ajoute à l'ensemble spécifié l'élément spécifié. Si l'élément n'existait pas déjà, il sera
     * ajouté comme un élément non-sélectionné. S'il existait déjà, son état sera laissé inchangé.
     */
    private static <T extends Element> boolean add(final Map<T,Boolean> map, final T element) {
        final Boolean old = map.put(element, FALSE);
        if (old == null) {
            return true;
        }
        if (old) {
            if (map.put(element, old) != FALSE) {
                throw new AssertionError(element);
            }
        }
        return false;
    }

    /**
     * Affecte au modèle de la liste spécifiée l'ensemble des éléments spécifié.
     * La sélection sera définie en fonction des valeurs du dictionnaire.
     */
    private static void setElements(final JList list, final Map<? extends Element, Boolean> elements) {
        final Model model = (Model) list.getModel();
        model.setElements(elements.keySet());
        copySelection(elements, list);
    }

    /**
     * Sélectionne les éléments de la liste spécifiée en fonction du dictionnaire spécifié. Cette
     * méthode peut être interprétée comme une copie de la sélection du dictionnaire {@code elements}
     * <em>vers</em> la liste {@code list}.
     */
    private static <T extends Element> void copySelection(final Map<T,Boolean> elements, final JList list) {
        final ListModel model = list.getModel();
        final int size = model.getSize();
        int selected[] = new int[size];
        int count = 0;
        for (int i=0; i<size; i++) {
            if (TRUE.equals(elements.get(model.getElementAt(i)))) {
                selected[count++] = i;
            }
        }
        selected = XArray.resize(selected, count);
        list.setSelectedIndices(selected);
    }

    /**
     * Met à jour l'état du dictionnaire spécifié en fonction de la sélection de la liste spécifiée.
     * Cette méthode peut être interprétée comme une copie de la sélection de la liste {@code list}
     * <em>vers</em> le dictionnaire {@code elements}.
     */
    private static <T extends Element> void copySelection(final JList list, final Map<T,Boolean> elements) {
        final ListModel model = list.getModel();
        final ListSelectionModel selection = list.getSelectionModel();
        final int size = model.getSize();
        for (int i=0; i<size; i++) {
            @SuppressWarnings("unchecked")
            final T element = (T) model.getElementAt(i);
            if (elements.put(element, selection.isSelectedIndex(i)) == null) {
                throw new AssertionError(element);
            }
        }
    }

    /**
     * Met à jour les champs internes (notamment {@link #descriptors}) en fonction de la sélection
     * des listes de couches, procédures et décalages spatio-temporelles. Cette opération intervient
     * typiquement lorsque l'utilisateur appuie sur le bouton "Suivant".
     */
    private void commitPropertiesSelection() {
        copySelection(layerList,     layers);
        copySelection(operationList, operations);
        copySelection(offsetList,    offsets);
        final Set<Descriptor> selected  = new HashSet<Descriptor>();
        final Set<Descriptor> confirmed = new HashSet<Descriptor>();
        for (final Map.Entry<Descriptor,Boolean> entry : descriptors.entrySet()) {
            final Descriptor descriptor = entry.getKey();
            final boolean isSelected = TRUE.equals(layers    .get(descriptor.getLayer      ())) &&
                                       TRUE.equals(operations.get(descriptor.getOperation       ())) &&
                                       TRUE.equals(offsets   .get(descriptor.getRegionOfInterest()));
            if (isSelected) {
                if (!selected.add(descriptor)) {
                    throw new AssertionError(descriptor);
                }
            }
            if (entry.getValue()) {
                if (!confirmed.add(descriptor)) {
                    throw new AssertionError(descriptor);
                }
            }
            entry.setValue(isSelected);
        }
        descriptorList.clear();
        descriptorList.addElements(selected);
        descriptorList.selectElements(confirmed);
    }

    /**
     * Met à jour les champs internes (notamment {@link #layers}, {@link #operations} et {@link #offsets})
     * en fonction de la sélection des descripteurs. Cette opération intervient typiquement lorsque
     * l'utilisateur appuie sur le bouton "Précédent".
     * <p>
     * Notez que cette méthode n'efface pas la sélection précédente des listes {@link #layers},
     * {@link #operations} et {@link #offsets}. Elle peut seulement l'étendre. Si vous souhaitez
     * effacer les sélections précédentes au préalable, appelez d'abord
     * <code>{@link #selectAll selectAll}(..., FALSE)</code>.
     */
    private void commitDescriptorSelection() {
        @SuppressWarnings("unchecked")
        final Set<Descriptor> selected = new HashSet<Descriptor>(descriptorList.getElements(true));
        for (final Map.Entry<Descriptor,Boolean> entry : descriptors.entrySet()) {
            final Descriptor descriptor = entry.getKey();
            final boolean    isSelected = selected.contains(descriptor);
            entry.setValue(isSelected);
            if (isSelected) {
                if (layers    .put(descriptor.getLayer(),       TRUE) == null ||
                    operations.put(descriptor.getOperation(),        TRUE) == null ||
                    offsets   .put(descriptor.getRegionOfInterest(), TRUE) == null)
                {
                    throw new AssertionError(descriptor);
                }
            }
        }
        copySelection(layers,     layerList    );
        copySelection(operations, operationList);
        copySelection(offsets,    offsetList   );
    }

    /**
     * Affecte la valeur spécifiée à toutes les entrées du dictionnaire spécifié.
     */
    private static <T extends Element> void selectAll(final Map<T,Boolean> elements, final Boolean isSelected) {
        for (final Map.Entry<T,Boolean> entry : elements.entrySet()) {
            entry.setValue(isSelected);
        }
    }

    /**
     * Sélectionne les descripteurs qui avaient été choisies par l'utilisateur lors de
     * la dernière utilisation de cette composante. Cette sélection est puisée dans
     * les {@linkplain Preferences préférences}.
     */
    public void loadSelection() {
        final String last = preferences.get(LAST_DESCRIPTORS, null);
        if (last == null) {
            return;
        }
        final Set<String>     selected  = new HashSet<String>();
        final Set<Descriptor> confirmed = new HashSet<Descriptor>();
        final StringTokenizer tokens    = new StringTokenizer(last, "\t\n\r\f");
        while (tokens.hasMoreTokens()) {
            final String token = tokens.nextToken().trim();
            if (token.length() != 0) {
                selected.add(token);
            }
        }
        for (final Map.Entry<Descriptor,Boolean> entry : descriptors.entrySet()) {
            final Descriptor descriptor = entry.getKey();
            if (selected.contains(descriptor.getName())) {
                entry.setValue(TRUE);
                if (!confirmed.add(descriptor)) {
                    throw new AssertionError(descriptor);
                }
            }
        }
        final Set<Descriptor> ensureAdded = new HashSet<Descriptor>(confirmed);
        ensureAdded.removeAll(descriptorList.getElements(true ));
        ensureAdded.removeAll(descriptorList.getElements(false));
        descriptorList.addElements(ensureAdded);
        descriptorList.selectElements(confirmed);
        commitDescriptorSelection();
    }

    /**
     * Sauvegarde la sélection de l'utilisateur dans les {@linkplain Preferences préférences}.
     * Cette sélection pourra être récupérée plus tard avec {@link #loadSelection}. Cette méthode
     * est appelée automatiquement lorsque l'utilisateur appuie sur le bouton "Exécuter".
     */
    public void saveSelection() {
        final StringBuilder buffer = new StringBuilder();
        for (final Descriptor descriptor : getDescriptors(true)) {
            if (buffer.length() != 0) {
                buffer.append('\t');
            }
            buffer.append(descriptor.getName());
        }
        preferences.put(LAST_DESCRIPTORS, buffer.toString());
    }

    /**
     * Spécifie l'étape à afficher. L'argument {@code step} doit être une des constantes
     * suivantes: {@link #SELECT_PROPERTIES} ou {@link #SELECT_DESCRIPTORS}.
     */
    public void setStep(final String step) {
        if (SELECT_PROPERTIES.equals(step)) {
            commitDescriptorSelection();
            previous.setEnabled(false);
            next.setText("Suivant");
        } else if (SELECT_DESCRIPTORS.equals(step)) {
            commitPropertiesSelection();
            previous.setEnabled(true);
            next.setText("Exécuter");
        } else {
            throw new IllegalArgumentException(step);
        }
        ((CardLayout) cards.getLayout()).show(cards, step);
        currentStep = step;
    }

    /**
     * Si {@code selected} est {@code true}, retourne l'ensemble des descripteurs sélectionnés par
     * l'utilisateur. Sinon, retourne l'ensemble des descripteurs qui ne sont pas sélectionnés.
     */
    public Set<Descriptor> getDescriptors(final boolean selected) {
        if (SELECT_PROPERTIES.equals(currentStep)) {
            commitPropertiesSelection();
        }
        @SuppressWarnings("unchecked")
        final Set<Descriptor> s = new HashSet<Descriptor>(descriptorList.getElements(selected));
        return s;
    }

    /**
     * Méthode appelée automatiquement lorsque l'utilisateur a appuyé sur le bouton "Exécuter".
     * L'implémentation par défaut ne fait qu'appeller {@link #dispose}. Les classes dérivées
     * devrait surcharger cette méthode afin d'exécuter l'action qu'elles souhaite effectuer
     * à partir de l'{@linkplain #getSelectedDescriptors ensemble des descripteurs sélectionnés}.
     */
    protected void execute() {
        dispose();
    }

    /**
     * Méthode appelée automatique lorsque l'utilisateur a appuyé sur le bouton "Annuler".
     * L'implémentation par défaut ne fait qu'appeller {@link #dispose}. Les classes dérivées
     * devrait surcharger cette méthode afin d'interrompre l'action lancée par {@link #execute}.
     */
    protected void cancel() {
        dispose();
    }

    /**
     * Fait disparaître la fenêtre parente. Cette méthode est appelée automatiquement lorsque
     * l'utilisateur appuie sur le bouton "Annuler". Elle peut aussi être appelée lorsque
     * l'exécution de la tâche (celle qui est lancée après la sélection des descripteurs)
     * est terminée.
     */
    public void dispose() {
        previous.setEnabled(false);
        next    .setEnabled(false);
        cancel  .setEnabled(false);
        Container parent = this;
        while ((parent = parent.getParent()) != null) {
            if (parent instanceof JInternalFrame) {
                ((JInternalFrame) parent).dispose();
                return;
            }
            if (parent instanceof Window) {
                ((Window) parent).dispose();
                return;
            }
        }
    }

    /**
     * Affiche cette composante graphique. Si {@code owner} est nul, cette composante graphique
     * sera affiché dans son propre {@link JFrame}. Sinon, elle apparaître comme une boîte de
     * dialogue ou une fenêtre interne, en fonction du type de {@code owner}.
     */
    public void show(final Component owner) {
        final Component frame = SwingUtilities.toFrame(owner, this, "Sélection de descripteurs", null);
        frame.setVisible(true);
    }

    /**
     * Affiche cette composante avec l'ensemble des descripteurs de la base de données par défaut.
     * Cette méthode est utilisée principalement à des fins de tests.
     *
     * @throws CatalogException si une erreur est survenue lors de l'interrogation du catalogue.
     */
    public static void main(String[] args) throws CatalogException {
        final Catalog observations = Catalog.getDefault();
        final DescriptorChooser chooser = new DescriptorChooser(observations.getDescriptors());
        chooser.show(null);
    }
}
