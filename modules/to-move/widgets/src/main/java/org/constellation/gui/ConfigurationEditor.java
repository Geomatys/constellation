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
package org.constellation.gui;

// Interface utilisateur

import org.constellation.catalog.ConfigurationKey;
import org.constellation.catalog.Database;
import org.constellation.resources.i18n.ResourceKeys;
import org.constellation.resources.i18n.Resources;
import org.geotoolkit.util.Utilities;
import org.geotools.resources.SwingUtilities;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

// Modèles et événements
// Divers
// Geotools dependencies
// Constellation


/**
 * Editeur de la configuration de l'application. La configuration comprend entre autres les
 * instructions SQL à utiliser pour accéder à la base de données. Cet objet peut être construit
 * en lui spécifiant en paramètres l'objet {@link Database} qui contient les instructions SQL à
 * utiliser. On peut ensuite appeler {@link #addKey} pour ajouter un aspect de la configuration
 * qui poura être édité. Enfin, on peut appeler la méthode {@link #showDialog} pour faire apparaître
 * l'éditeur.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
public class ConfigurationEditor extends JPanel {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = -7936405915390502830L;

    /**
     * Liste des clés représentant les instructions SQL éditables.
     */
    private final List<ConfigurationKey> keySQL = new ArrayList<ConfigurationKey>();

    /**
     * Liste des instructions SQL éditées par l'utilisateur.
     */
    private final List<String> userSQL = new ArrayList<String>();
    
    /**
     * Base de données à éditer.
     */
    protected final Database configuration;

    /**
     * Journal dans lequel écrire une notification
     * des requêtes qui ont été changées.
     */
    private final Logger logger;

    /**
     * Composante dans laquelle l'utilisateur pourra éditer les instructions SQL.
     * Avant de changer la requête à éditer, le contenu de ce champ devra être
     * copié dans {@code userSQL.get(index)}.
     */
    private final JTextArea valueArea = new JTextArea(5, 40);

    /**
     * Modèle pour l'affichage de la liste des noms descriptifs des instructions SQL.
     * Ce modèle s'occupe des transferts entre {@code valueArea} et {@code userSQL}.
     */
    private final Model model = new Model();

    /**
     * Liste des instructions SQL.
     */
    private final JList sqlList = new JList(model);

    /**
     * Modèle pour l'affichage de la liste des noms descriptifs des instructions SQL.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    private final class Model extends AbstractListModel implements ListSelectionListener, ActionListener {
        /**
         * Pour compatibilités entre les enregistrements binaires de différentes versions.
         */
        private static final long serialVersionUID = 5243424642395410933L;

        /**
         * Index de l'instruction sélectionné.
         */
        int index = -1;

        /**
         * Taille qu'avait {@link #userSQL} lors du dernier appel de {@link #update}.
         */
        private int lastSize;

        /**
         * Retourne le nombre d'instructions.
         */
        public int getSize() {
            return keySQL.size();
        }

        /**
         * Retourne l'instruction à l'index spécifié.
         */
        public Object getElementAt(final int index) {
            return keySQL.get(index).getKey();
        }

        /**
         * Sélectionne une nouvelle instruction. Le
         * contenu du champ de texte sera mis à jour.
         */
        public void valueChanged(final ListSelectionEvent event) {
            if (index>=0 && index<userSQL.size()) {
                commit();
            }
            valueTextChanged();
        }

        /**
         * Sauvegarde la requête SQL que l'utilisateur vient de modifier.
         * Cette modification n'est pas encore enregistrées dans les
         * configuration. Cette étape sera faite à la fin par la méthode
         * {@link #save()} si l'utilisateur clique sur "Ok"
         */
        final void commit() {
            String editedText = valueArea.getText();
            if (editedText.trim().length() == 0) {
                editedText = keySQL.get(index).getDefaultValue();
            }
            userSQL.set(index, editedText);
        }

        /**
         * Affiche dans {@code valueArea} l'instruction SQL qui
         * correspond à la sélection courrante de l'utilisateur.
         */
        void valueTextChanged() {
            index = sqlList.getSelectedIndex();
            if (index>=0 && index<userSQL.size()) {
                valueArea.setText(userSQL.get(index));
                valueArea.setEnabled(true);
            } else {
                valueArea.setText(null);
                valueArea.setEnabled(false);
            }
        }

        /**
         * Vérifie si de nouvelles instructions SQL ont été
         * ajoutées à la suite des instruction déjà déclarées.
         */
        protected void update() {
            final int size = userSQL.size();
            if (size > lastSize) {
                fireIntervalAdded(this, lastSize, size-1);
                lastSize = size;
            }
        }
        
        /**
         * Méthode appelée automatiquement lorsque l'utilisateur
         * clique sur le bouton "Rétablir".
         */
        public void actionPerformed(final ActionEvent event) {
            reset();
        }
    }

    /**
     * Construit un éditeur d'instructions SQL.
     *
     * @param configuration Base de données dont on veut éditer la configuration.
     * @param description Note explicative destinée à l'utilisateur.
     * @param logger Journal dans lequel écrire une notification des
     *               requêtes qui ont été changées.
     */
    public ConfigurationEditor(final Database configuration,
                               final String   description,
                               final Logger   logger)
    {
        super(new BorderLayout());
        this.logger = logger;
        this.configuration = configuration;
        if (configuration == null) {
            throw new NullPointerException("Configuration must not be null.");
        }
        sqlList.addListSelectionListener(model);
        sqlList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        valueArea.setFont(new Font("monospaced", Font.PLAIN, 12));
        setPreferredSize(new Dimension(720, 320));

        final JScrollPane scrollList  = new JScrollPane(sqlList);
        final JScrollPane scrollValue = new JScrollPane(valueArea);
        final JSplitPane  splitPane   = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, scrollList, scrollValue);
        final JComponent  comments    = SwingUtilities.getMultilineLabelFor(scrollList, description);
        comments.setBorder(BorderFactory.createEmptyBorder(/*top*/0, /*left*/0, /*bottom*/18, /*right*/0));
        add(comments,   BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Fait apparaître l'éditeur des instructions SQL. Si l'utilisateur clique sur "Ok",
     * alors les instructions éditées seront sauvegardées par un appel à la méthode
     * {@link #save}.
     *
     * @param  owner Composante par-dessus laquelle faire apparaître la boîte de dialogue.
     * @return {@code true} si l'utilisateur à cliqué sur "Ok", ou {@code false} sinon.
     */
    public boolean showDialog(final Component owner) {
        if (userSQL.isEmpty()) {
            // Il n'y a rien à afficher.
            return false;
        }
        model.update();
        sqlList.setSelectedIndex(0);

        // TODO: JOptionPane ne fait pas un bon travail concernant la taille des boutons
        //       que l'on ajoute sur la barre des boutons (en plus de "Ok" et "Annuler").
        //       Pour afficher le bouton "Rétablir" malgré ces défauts, ne pas mettre
        //       'model' en commentaire.
        final boolean ok = SwingUtilities.showOptionDialog(owner, this, "Configuration"/*, model*/);
        model.commit();
        if (ok) save();
        return ok;
    }

    /**
     * Ajoute un caractère de changement de ligne ('\n')
     * à la fin de texte spécifié s'il n'y en avait pas
     * déjà un.
     */
    private static String line(String value) {
        if (value == null) {
            return "";
        }
        final int length = value.length();
        if (length != 0) {
            final char c = value.charAt(length-1);
            if (c!='\r' && c!='\n') {
                value += '\n';
            }
        }
        return value;
    }

    /**
     * Ajoute une instruction SQL à la liste des instructions qui pourront être éditées.
     *
     * @param key Clé permetant de retrouver l'instruction SQL actuelle dans l'objet {@link Database}.
     */
     public synchronized void addKey(final ConfigurationKey key) {
         userSQL.add(line(configuration.getProperty(key)));
         keySQL.add(key);
     }

    /**
     * Enregistre les modifications apportées aux instructions SQL. Cette
     * méthode sera appelée automatiquement lorsque l'utilisateur appuie
     * sur "Ok" dans la boîte de dialogue.
     */
    protected void save() {
        for (int i=userSQL.size(); --i>=0;) {
            final ConfigurationKey key = keySQL.get(i);
            String value = userSQL.get(i);
            if (value != null) {
                value = value.trim();
                if (value.length() == 0) {
                    value = null;
                }
            }
            if (!Utilities.equals(value, configuration.getProperty(key))) {
                final int clé;
                if (Utilities.equals(value, key.getDefaultValue())) {
                    clé = ResourceKeys.REMOVE_QUERY_$1;
                    value = null;
                } else {
                    clé = ResourceKeys.DEFINE_QUERY_$1;
                }
                configuration.setProperty(key, value);
                if (logger != null) {
                    logger.config(Resources.format(clé, key.getKey()));
                }
            }
        }
    }

    /**
     * Rétablit les requêtes par défaut.
     */
    private void reset() {
        for (int i=userSQL.size(); --i>=0;) {
            userSQL.set(i, line(keySQL.get(i).getDefaultValue()));
        }
        model.valueTextChanged();
    }
}
