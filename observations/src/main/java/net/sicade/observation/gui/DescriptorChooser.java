/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 2006, Institut de Recherche pour le D�veloppement
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
package net.sicade.observation.gui;

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
import org.geotools.gui.swing.DisjointLists;
import org.geotools.resources.SwingUtilities;

// J2SE dependencies
import net.sicade.resources.XArray;
import net.sicade.observation.Element;
import net.sicade.observation.Observations;
import net.sicade.observation.CatalogException;
import net.sicade.observation.coverage.Series;
import net.sicade.observation.coverage.Operation;
import net.sicade.observation.coverage.Descriptor;
import net.sicade.observation.coverage.LocationOffset;


/**
 * S�lectionne des descripteurs parmis une liste.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DescriptorChooser extends JPanel {
    /**
     * Une liste d'�l�ments m�moris�e sour forme de tableau. Ce mod�le sera utilis�
     * par les listes de la premi�re �tape.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    private static final class Model extends AbstractListModel {
        /**
         * Les �l�ments dans cette liste.
         */
        private Element[] elements = new Element[0];

        /** Sp�cifie l'ensemble des �l�ments � affecter � cette liste. */
        public void setElements(final Set<? extends Element> newElements) {
            final int newLength = newElements.size();
            final int length = (elements != null) ? Math.max(elements.length, newLength) : newLength;
            elements = newElements.toArray(new Element[newLength]);
            if (length != 0) {
                fireContentsChanged(this, 0, length-1);
            }
        }

        /** Retourne le nombre d'�l�ments dans cette liste. */
        public int getSize() {
            return elements.length;
        }

        /** Retourne l'�l�ment � l'index sp�cifi�. */
        public Element getElementAt(final int index) {
            return elements[index];
        }
    }

    /**
     * Action ex�cut� lorsque l'un des boutons est appuy�.
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
     * Nom de la premi�re �tape, qui consiste � choisir les {@linkplain Series s�ries},
     * {@linkplain Operation op�rations} et {@link LocationOffset positions relatives}.
     * Ce nom peut �tre sp�cifi� en argument � {@link #setStep} pour s�lectionner les
     * listes � afficher.
     */
    public static final String SELECT_PROPERTIES = "SelectProperties";

    /**
     * Nom de la seconde �tape, qui consiste � choisir les {@linkplain Descriptor descripteurs}
     * eux-m�mes. Ce nom peut �tre sp�cifi� en argument � {@link #setStep} pour s�lectionner les
     * listes � afficher.
     */
    public static final String SELECT_DESCRIPTORS = "SelectDescriptors";

    /**
     * Le nom de la feuille de pr�f�rences pour les derniers descripteurs utilis�s.
     */
    private static final String LAST_DESCRIPTORS = "LastDescriptors";

    /**
     * Ensemble des {@linkplain Descriptor descripteurs} dont on veut r�partir les composantes
     * dans les trois listes {@code phenomenons}, {@code operations} et {@code offsets}.
     */
    private final Map<Descriptor,Boolean> descriptors = new LinkedHashMap<Descriptor,Boolean>();

    /**
     * Ensemble des {@linkplain Series s�ries} d'images.
     */
    private final Map<Series,Boolean> series = new LinkedHashMap<Series,Boolean>();

    /**
     * Ensemble des {@linkplain Operation op�rations}.
     */
    private final Map<Operation,Boolean> operations = new LinkedHashMap<Operation,Boolean>();
    
    /**
     * Ensemble des {@linkplain LocationOffset positions spatio-temporelles relatives}.
     */
    private final Map<LocationOffset,Boolean> offsets = new LinkedHashMap<LocationOffset,Boolean>();

    /**
     * Liste des {@linkplain Series s�ries} d'images.
     */
    private final JList seriesList;

    /**
     * Liste des {@linkplain Operation op�rations}.
     */
    private final JList operationList;

    /**
     * Liste des {@linkplain LocationOffset positions spatio-temporelles relatives}.
     */
    private final JList offsetList;

    /**
     * List des {@linkplain Descriptor descripteurs}.
     */
    private final DisjointLists descriptorList;

    /**
     * L'�tape courante, comme une des constantes {@link #SELECT_PROPERTIES} ou
     * {@link #SELECT_DESCRIPTORS}.
     */
    private String currentStep;

    /**
     * La composante graphique qui affichera les diff�rentes �tapes.
     */
    private final JPanel cards;

    /**
     * Le bouton pour revenir � l'�tape pr�c�dente.
     */
    private final JButton previous;

    /**
     * Le bouton pour aller � l'�tape suivante.
     */
    private final JButton next;

    /**
     * Le bouton pour annuler.
     */
    private final JButton cancel;

    /**
     * Les pr�f�rences � utiliser pour m�moriser les derniers descripteurs utilis�s.
     */
    private final Preferences preferences = Preferences.userNodeForPackage(DescriptorChooser.class);

    /**
     * Construit une nouvelle composante initialement vide.
     */
    public DescriptorChooser() {
        super(new BorderLayout(0,9));
        cards = new JPanel(new CardLayout());
        /*
         * Construction de l'�tape 1.
         */
        JPanel step = new JPanel(new BorderLayout(0,9));
        JPanel pane = new JPanel(new GridLayout(1,3,9,0));
        pane.add(new JLabel("S�ries d'images", JLabel.CENTER));
        pane.add(new JLabel("Op�rations",      JLabel.CENTER));
        pane.add(new JLabel("D�calages",       JLabel.CENTER));
        step.add(pane, BorderLayout.NORTH);
        pane = new JPanel(new GridLayout(1,3,9,0));
        pane.add(new JScrollPane(seriesList    = new JList(new Model())));
        pane.add(new JScrollPane(operationList = new JList(new Model())));
        pane.add(new JScrollPane(offsetList    = new JList(new Model())));
        step.add(pane, BorderLayout.CENTER);
        cards.add(step, SELECT_PROPERTIES);
        /*
         * Construction de l'�tape 2.
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
        pane.add(previous = new JButton("Pr�c�dent"));
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
         * D�finition des actions.
         */
        final Action action = new Action();
        previous.addActionListener(action);
        next    .addActionListener(action);
        cancel  .addActionListener(action);
        setStep(SELECT_PROPERTIES);
    }

    /**
     * Construit une nouvelle composante initialis�e avec l'ensemble des descripteurs sp�cifi�.
     * Les descripteurs qui avaient �t� choisis par l'utilisateur lors de la derni�re utilisation
     * de cette composante seront automatiquement s�lectionn�s.
     */
    public DescriptorChooser(final Collection<Descriptor> descriptors) {
        this();
        addDescriptor(descriptors);
        loadSelection();
    }

    /**
     * Ajoute � cette composante l'ensemble des descripteurs sp�cifi�. Aucun des nouveaux
     * descripteurs ne sera initiallement s�lectionn�. Afin de pr�-s�lectionner ceux qui
     * avaient �t� choisis par l'utilisateur lors de la derni�re utilisation de cette
     * composante, appelez {@link #loadSelection}.
     */
    public void addDescriptor(final Collection<Descriptor> toAdd) {
        boolean seriesModified    = false;
        boolean operationModified = false;
        boolean offsetModified    = false;
        for (final Descriptor descriptor : toAdd) {
            if (add(descriptors, descriptor)) {
                seriesModified    |= add(series,     descriptor.getPhenomenon());
                operationModified |= add(operations, descriptor.getProcedure());
                offsetModified    |= add(offsets,    descriptor.getLocationOffset());
            }
        }
        if (seriesModified)    setElements(seriesList,    series);
        if (operationModified) setElements(operationList, operations);
        if (offsetModified)    setElements(offsetList,    offsets);
    }

    /**
     * Ajoute � l'ensemble sp�cifi� l'�l�ment sp�cifi�. Si l'�l�ment n'existait pas d�j�, il sera
     * ajout� comme un �l�ment non-s�lectionn�. S'il existait d�j�, son �tat sera laiss� inchang�.
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
     * Affecte au mod�le de la liste sp�cifi�e l'ensemble des �l�ments sp�cifi�.
     * La s�lection sera d�finie en fonction des valeurs du dictionnaire.
     */
    private static void setElements(final JList list, final Map<? extends Element, Boolean> elements) {
        final Model model = (Model) list.getModel();
        model.setElements(elements.keySet());
        copySelection(elements, list);
    }

    /**
     * S�lectionne les �l�ments de la liste sp�cifi�e en fonction du dictionnaire sp�cifi�. Cette
     * m�thode peut �tre interpr�t�e comme une copie de la s�lection du dictionnaire {@code elements}
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
     * Met � jour l'�tat du dictionnaire sp�cifi� en fonction de la s�lection de la liste sp�cifi�e.
     * Cette m�thode peut �tre interpr�t�e comme une copie de la s�lection de la liste {@code list}
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
     * Met � jour les champs internes (notamment {@link #descriptors}) en fonction de la s�lection
     * des listes de s�ries, proc�dures et d�calages spatio-temporelles. Cette op�ration intervient
     * typiquement lorsque l'utilisateur appuie sur le bouton "Suivant".
     */
    private void commitPropertiesSelection() {
        copySelection(seriesList,    series);
        copySelection(operationList, operations);
        copySelection(offsetList,    offsets);
        final Set<Descriptor> selected  = new HashSet<Descriptor>();
        final Set<Descriptor> confirmed = new HashSet<Descriptor>();
        for (final Map.Entry<Descriptor,Boolean> entry : descriptors.entrySet()) {
            final Descriptor descriptor = entry.getKey();
            final boolean isSelected = TRUE.equals(series    .get(descriptor.getPhenomenon    ())) &&
                                       TRUE.equals(operations.get(descriptor.getProcedure     ())) &&
                                       TRUE.equals(offsets   .get(descriptor.getLocationOffset()));
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
     * Met � jour les champs internes (notamment {@link #series}, {@link #operations} et {@link #offsets})
     * en fonction de la s�lection des descripteurs. Cette op�ration intervient typiquement lorsque
     * l'utilisateur appuie sur le bouton "Pr�c�dent".
     * <p>
     * Notez que cette m�thode n'efface pas la s�lection pr�c�dente des listes {@link #series},
     * {@link #operations} et {@link #offsets}. Elle peut seulement l'�tendre. Si vous souhaitez
     * effacer les s�lections pr�c�dentes au pr�alable, appelez d'abord
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
                if (series    .put(descriptor.getPhenomenon(),     TRUE) == null ||
                    operations.put(descriptor.getProcedure(),      TRUE) == null ||
                    offsets   .put(descriptor.getLocationOffset(), TRUE) == null)
                {
                    throw new AssertionError(descriptor);
                }
            }
        }
        copySelection(series,     seriesList   );
        copySelection(operations, operationList);
        copySelection(offsets,    offsetList   );
    }

    /**
     * Affecte la valeur sp�cifi�e � toutes les entr�es du dictionnaire sp�cifi�.
     */
    private static <T extends Element> void selectAll(final Map<T,Boolean> elements, final Boolean isSelected) {
        for (final Map.Entry<T,Boolean> entry : elements.entrySet()) {
            entry.setValue(isSelected);
        }
    }

    /**
     * S�lectionne les descripteurs qui avaient �t� choisies par l'utilisateur lors de
     * la derni�re utilisation de cette composante. Cette s�lection est puis�e dans
     * les {@linkplain Preferences pr�f�rences}.
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
     * Sauvegarde la s�lection de l'utilisateur dans les {@linkplain Preferences pr�f�rences}.
     * Cette s�lection pourra �tre r�cup�r�e plus tard avec {@link #loadSelection}. Cette m�thode
     * est appel�e automatiquement lorsque l'utilisateur appuie sur le bouton "Ex�cuter".
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
     * Sp�cifie l'�tape � afficher. L'argument {@code step} doit �tre une des constantes
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
            next.setText("Ex�cuter");
        } else {
            throw new IllegalArgumentException(step);
        }
        ((CardLayout) cards.getLayout()).show(cards, step);
        currentStep = step;
    }

    /**
     * Si {@code selected} est {@code true}, retourne l'ensemble des descripteurs s�lectionn�s par
     * l'utilisateur. Sinon, retourne l'ensemble des descripteurs qui ne sont pas s�lectionn�s.
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
     * M�thode appel�e automatiquement lorsque l'utilisateur a appuy� sur le bouton "Ex�cuter".
     * L'impl�mentation par d�faut ne fait qu'appeller {@link #dispose}. Les classes d�riv�es
     * devrait surcharger cette m�thode afin d'ex�cuter l'action qu'elles souhaite effectuer
     * � partir de l'{@linkplain #getSelectedDescriptors ensemble des descripteurs s�lectionn�s}.
     */
    protected void execute() {
        dispose();
    }

    /**
     * M�thode appel�e automatique lorsque l'utilisateur a appuy� sur le bouton "Annuler".
     * L'impl�mentation par d�faut ne fait qu'appeller {@link #dispose}. Les classes d�riv�es
     * devrait surcharger cette m�thode afin d'interrompre l'action lanc�e par {@link #execute}.
     */
    protected void cancel() {
        dispose();
    }

    /**
     * Fait dispara�tre la fen�tre parente. Cette m�thode est appel�e automatiquement lorsque
     * l'utilisateur appuie sur le bouton "Annuler". Elle peut aussi �tre appel�e lorsque
     * l'ex�cution de la t�che (celle qui est lanc�e apr�s la s�lection des descripteurs)
     * est termin�e.
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
     * sera affich� dans son propre {@link JFrame}. Sinon, elle appara�tre comme une bo�te de
     * dialogue ou une fen�tre interne, en fonction du type de {@code owner}.
     */
    public void show(final Component owner) {
        final Component frame = SwingUtilities.toFrame(owner, this, "S�lection de descripteurs", null);
        frame.setVisible(true);
    }

    /**
     * Affiche cette composante avec l'ensemble des descripteurs de la base de donn�es par d�faut.
     * Cette m�thode est utilis�e principalement � des fins de tests.
     *
     * @throws CatalogException si une erreur est survenue lors de l'interrogation du catalogue.
     */
    public static void main(String[] args) throws CatalogException {
        final Observations observations = Observations.getDefault();
        final DescriptorChooser chooser = new DescriptorChooser(observations.getDescriptors());
        chooser.show(null);
    }
}
