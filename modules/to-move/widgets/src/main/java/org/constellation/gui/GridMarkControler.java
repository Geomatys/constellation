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

// User interface
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.ButtonGroup;
import javax.swing.JTabbedPane;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.SwingConstants;

// Miscellaneous
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.RenderedImage;

// Events
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

// Geotools dependencies
import org.geotools.resources.SwingUtilities;

// Constellation
import org.constellation.resources.i18n.Resources;
import org.constellation.resources.i18n.ResourceKeys;


/**
 * Boite de dialogue proposant à l'utilisateur d'ajuster la densité des marques sur une grille.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@SuppressWarnings("serial")
public final class GridMarkControler extends JPanel {
    /**
     * Titre de la boîte de dialogue.
     */
    private final String title;

    /**
     * Paneau permettant de choisir une couleur.
     */
    private final MarkColorChooser colorChooser = new MarkColorChooser(true);

    /**
     * Case à cocher pour indiquer s'il faut ajuster
     * la densité automatiquement en fonction du zoom.
     */
    private final JToggleButton automatic;

    /**
     * Case à cocher pour indiquer s'il faut utiliser
     * une valeur fixe pour la densité des marques.
     */
    private final JToggleButton manual;

    /**
     * Composante permettant de sélectionner la densité des marques. La valeur
     * "1" correspond à la densité maximale (minimum de décimation).
     */
    private final JSlider decimation=new JSlider(1, 10, 1);

    /**
     * Construit une boîte de dialogue par défaut.
     *
     * @param title Titre de la boîte de dialogue.
     */
    public GridMarkControler(final String title) {
        super(new BorderLayout());
        this.title = title;

        final Resources resources = Resources.getResources(null);
        automatic = new JRadioButton(resources.getString(ResourceKeys.ADJUST_WITH_ZOOM));
        manual    = new JRadioButton(resources.getLabel(ResourceKeys.CONSTANT_DECIMATION), true);

        final JTabbedPane tabs=new JTabbedPane();
        final GridBagConstraints c=new GridBagConstraints();
        ///////////////////
        ////  Couleur  ////
        ///////////////////
        if (true) {
            tabs.addTab(resources.getString(ResourceKeys.COLOR), colorChooser);
        }
        ///////////////////
        ////  Densité  ////
        ///////////////////
        if (true) {
            final JPanel panel=new JPanel(new GridBagLayout());
            decimation.setMajorTickSpacing(1);
            decimation.setPaintTicks (true);
            decimation.setSnapToTicks(true);
            decimation.setPaintLabels(true);
            decimation.setBorder(BorderFactory.createCompoundBorder(
                                 BorderFactory.createLoweredBevelBorder(),
                                 BorderFactory.createEmptyBorder(/*top*/6, /*left*/6, /*bottom*/6, /*right*/6)));
            c.gridx=0; c.anchor=c.WEST;
            c.gridy=0;                   panel.add(automatic,  c);
            c.gridy=1;                   panel.add(manual,     c);
            c.gridy=2; c.insets.left=15; panel.add(decimation, c);

            final ButtonGroup group=new ButtonGroup();
            group.add(automatic);
            group.add(manual);

            final ActionListener listener=new ActionListener() {
                public void actionPerformed(final ActionEvent event) {
                    decimation.setEnabled(manual.isSelected());
                }
            };
            automatic.addActionListener(listener);
            manual   .addActionListener(listener);
            tabs.addTab(resources.getString(ResourceKeys.DECIMATION), panel);
        }
        add(tabs, BorderLayout.CENTER);
        setShape(new Ellipse2D.Float(-5, -5, 10, 10));
        setColor(new Color(234, 192, 0));
    }

    /**
     * Spécifie l'image à faire apparaître en arrière plan, ou {@code null}
     * s'il n'y en a pas. Cette image sera centrée dans la zone "aperçu" du
     * paneau servant à sélectionner la couleur.
     */
    public void setBackground(final RenderedImage image) {
        colorChooser.setBackground(image);
    }

    /**
     * Spécifie la forme géométrique à tracer. Cette forme devrait être centrée
     * à (0,0) et ses coordonnées devraient être exprimées en pixels. Cette forme
     * sera centrée dans la zone "aperçu" de la boîte de dialogue. Les dimensions
     * de cette forme détermineront les dimensions en pixels de la zone d'aperçu.
     */
    public void setShape(final Shape shape) {
        colorChooser.setShape(shape);
    }

    /**
     * Retourne la couleur sélectionnée.
     */
    public Color getColor() {
        return colorChooser.getColor();
    }

    /**
     * Spécifie la couleur sélectionnée.
     */
    public void setColor(final Color color) {
        colorChooser.setColor(color);
    }

    /**
     * Retourne la décimation sélectionnée par l'utilisateur, ou 0 si la décimation
     * doit être déterminée automatiquement. Si la décimation a été spécifiée par
     * l'utilisateur, elle sera obligatoirement supérieure à 0.
     */
    public int getDecimation() {
        return manual.isSelected() ? decimation.getValue() : 0;
    }

    /**
     * Spécifie la décimation sélectionnée. La valeur de 0 signifie
     * que la décimation devra être déterminée automatiquement.
     */
    public void setDecimation(final int dc) {
        if (dc != 0) {
            manual.setSelected(true);
            decimation.setEnabled(true);
            decimation.setValue(dc);
        } else {
            automatic.setSelected(true);
            decimation.setEnabled(false);
        }
    }

    /**
     * Fait apparaître la boite de dialogue. Cette méthode retourne {@code true}
     * si l'utilisateur a cliqué sur "Ok" après avoir changé au moins un paramètre.
     * S'il a cliqué sur "Annuler", fermer la boîte de dialogue ou cliqué sur "Ok"
     * sans n'avoir rien changé, alors cette méthode retourne {@code false}.
     */
    public boolean showDialog(final Component owner) {
        final Color color=getColor();
        final int dc=getDecimation();
        final int value=decimation.getValue();
        if (SwingUtilities.showOptionDialog(owner, this, title)) {
            return !color.equals(getColor()) || dc!=getDecimation();
        } else {
            setColor(color);
            setDecimation(dc);
            decimation.setValue(value);
            return false;
        }
    }
}
