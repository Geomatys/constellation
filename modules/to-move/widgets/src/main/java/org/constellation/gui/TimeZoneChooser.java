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

// Temps et utilitaires

import org.constellation.resources.i18n.ResourceKeys;
import org.constellation.resources.i18n.Resources;
import org.geotools.resources.SwingUtilities;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.TimeZone;

// Interface utilisateur
// Evénements
// Geotools
// Divers


/**
 * A dialog box for choosing a time zone. If the parent container is a
 * {@link javax.swing.JDesktopPane}, then the dialog box will appear as
 * an internal frame. Otherwise, it will appear as a regular dialog box.
 * <br><br>
 * Every click on a time zone fires a "TimeZone" property change event.
 * Classes can listen to this event by registering themselves:
 * <blockquote><code>
 * timezoneChooser.{@link JComponent#addPropertyChangeListener(String,PropertyChangeListener)
 * addPropertyChangeListener}("TimeZone", listener);
 * </code></blockquote>
 *
 * <p>&nbsp;</p>
 * <p align="center"><img src="doc-files/TimeZoneChooser.png"></p>
 * <p>&nbsp;</p>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@SuppressWarnings("serial")
public class TimeZoneChooser extends JPanel {
    /**
     * Liste des fuseaux horaires.
     */
    private final JList list;

    /**
     * Selected timezone, or {@code null}.
     */
    private TimeZone selected;

    /**
     * Construit un objet {@code TimeZoneChooser} qui proposera un choix parmi tous les
     * fuseaux horaires disponibles.
     *
     * @param message Message à placer en haut de la liste.
     */
    public TimeZoneChooser(final String message) {
        this(message, TimeZone.getAvailableIDs());
    }

    /**
     * Construit un objet {@code TimeZoneChooser} qui proposera un choix parmi tous
     * les fuseaux horaires qui représentent le décalage spécifié.
     *
     * @param message Message à placer en haut de la liste.
     * @param rawOffset Décalage (en milliseconde) par rapport à l'heure UTC, sans correction
     *        pour l'heure solaire. Il s'agit du décalage à ajouter à l'heure UTC pour obtenir
     *        l'heure locale.
     */
    public TimeZoneChooser(final String message, final int rawOffset) {
        this(message, TimeZone.getAvailableIDs(rawOffset));
    }

    /**
     * Construit un objet {@code TimeZoneChooser} qui
     * proposera un choix parmi les fuseaux horaire spécifiés.
     */
    private TimeZoneChooser(final String message, final String[] IDs) {
        super(new BorderLayout());
        Arrays.sort(IDs);
        list=new JList(IDs);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(list), BorderLayout.CENTER);
        setTimeZone(TimeZone.getDefault());
        addComponentListener(new ComponentAdapter() {
            /*
             * "Workaround" pour forcer l'apparition du fuseau horaire
             * sélectionné dans la fenêtre. 'ensureIndexIsVisible(int)'
             * ne fonctionne pas lorsque la fenêtre n'est pas visible.
             */
            public void componentResized(final ComponentEvent event) {
                final int index = list.getSelectedIndex();
                if (index >= 0) {
                    list.ensureIndexIsVisible(index);
                }
            }
        });
        list.addListSelectionListener(new ListSelectionListener() {
            /*
             * Prévient cette classe chaque fois que l'utilisateur
             * a sélectionné un nouveau fuseau horaire.
             */
            public void valueChanged(final ListSelectionEvent event) {
                if (!event.getValueIsAdjusting()) {
                    final TimeZone old = selected;
                    final Object value = list.getSelectedValue();
                    selected = (value!=null) ? TimeZone.getTimeZone(value.toString()) : null;
                    firePropertyChange("TimeZone", old, selected);
                }
            }
        });
        final JComponent label=SwingUtilities.getMultilineLabelFor(list, message);
        label.setBorder(BorderFactory.createEmptyBorder(/*top*/0, /*left*/0, /*bottom*/6, /*right*/0));
        add(label, BorderLayout.NORTH);
        setPreferredSize(new Dimension(200,200));
    }

    /**
     * Sélectionne le fuseau horaire spécifié.
     */
    public void setTimeZone(final TimeZone timezone) {
        list.setSelectedValue((timezone!=null) ? timezone.getID() : null, true);
    }

    /**
     * Retourne le fuseau horaire présentement sélectionné,
     * ou {@code null} si aucun fuseau horaire n'a été
     * sélectionné.
     */
    public TimeZone getTimeZone() {
        final Object value = list.getSelectedValue();
        return (value!=null) ? TimeZone.getTimeZone(value.toString()) : null;
    }

    /**
     * Fait apparaître ce panneau dans une boîte de dialogue avec les boutons
     * "ok" et "annuler" et attend la sélection de l'utilisateur.
     *
     * @param  owner Composante parente.
     * @return Le fuseau horaire sélectionné si l'utilisateur a cliqué sur "ok",
     *         ou {@code null} s'il a cliqué sur "annuler".
     */
    public TimeZone showDialog(final Component owner) {
        final Resources resources = Resources.getResources((owner!=null) ? owner.getLocale() : null);
        if (SwingUtilities.showOptionDialog(owner, this, resources.getString(ResourceKeys.TIME_ZONE))) {
            return getTimeZone();
        }
        return null;
    }
}
