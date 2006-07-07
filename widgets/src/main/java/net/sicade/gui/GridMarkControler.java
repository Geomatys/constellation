/*
 * Sicade - Syst�mes int�gr�s de connaissances
 *          pour l'aide � la d�cision en environnement
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
package net.sicade.gui;

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

// Seagis
import net.sicade.resources.seasview.Resources;
import net.sicade.resources.seasview.ResourceKeys;


/**
 * Boite de dialogue proposant � l'utilisateur d'ajuster la densit� des marques sur une grille.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@SuppressWarnings("serial")
public final class GridMarkControler extends JPanel {
    /**
     * Titre de la bo�te de dialogue.
     */
    private final String title;

    /**
     * Paneau permettant de choisir une couleur.
     */
    private final MarkColorChooser colorChooser = new MarkColorChooser(true);

    /**
     * Case � cocher pour indiquer s'il faut ajuster
     * la densit� automatiquement en fonction du zoom.
     */
    private final JToggleButton automatic;

    /**
     * Case � cocher pour indiquer s'il faut utiliser
     * une valeur fixe pour la densit� des marques.
     */
    private final JToggleButton manual;

    /**
     * Composante permettant de s�lectionner la densit� des marques. La valeur
     * "1" correspond � la densit� maximale (minimum de d�cimation).
     */
    private final JSlider decimation=new JSlider(1, 10, 1);

    /**
     * Construit une bo�te de dialogue par d�faut.
     *
     * @param title Titre de la bo�te de dialogue.
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
        ////  Densit�  ////
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
     * Sp�cifie l'image � faire appara�tre en arri�re plan, ou {@code null}
     * s'il n'y en a pas. Cette image sera centr�e dans la zone "aper�u" du
     * paneau servant � s�lectionner la couleur.
     */
    public void setBackground(final RenderedImage image) {
        colorChooser.setBackground(image);
    }

    /**
     * Sp�cifie la forme g�om�trique � tracer. Cette forme devrait �tre centr�e
     * � (0,0) et ses coordonn�es devraient �tre exprim�es en pixels. Cette forme
     * sera centr�e dans la zone "aper�u" de la bo�te de dialogue. Les dimensions
     * de cette forme d�termineront les dimensions en pixels de la zone d'aper�u.
     */
    public void setShape(final Shape shape) {
        colorChooser.setShape(shape);
    }

    /**
     * Retourne la couleur s�lectionn�e.
     */
    public Color getColor() {
        return colorChooser.getColor();
    }

    /**
     * Sp�cifie la couleur s�lectionn�e.
     */
    public void setColor(final Color color) {
        colorChooser.setColor(color);
    }

    /**
     * Retourne la d�cimation s�lectionn�e par l'utilisateur, ou 0 si la d�cimation
     * doit �tre d�termin�e automatiquement. Si la d�cimation a �t� sp�cifi�e par
     * l'utilisateur, elle sera obligatoirement sup�rieure � 0.
     */
    public int getDecimation() {
        return manual.isSelected() ? decimation.getValue() : 0;
    }

    /**
     * Sp�cifie la d�cimation s�lectionn�e. La valeur de 0 signifie
     * que la d�cimation devra �tre d�termin�e automatiquement.
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
     * Fait appara�tre la boite de dialogue. Cette m�thode retourne {@code true}
     * si l'utilisateur a cliqu� sur "Ok" apr�s avoir chang� au moins un param�tre.
     * S'il a cliqu� sur "Annuler", fermer la bo�te de dialogue ou cliqu� sur "Ok"
     * sans n'avoir rien chang�, alors cette m�thode retourne {@code false}.
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
