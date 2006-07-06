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
package net.sicade.gui;

// User interface
import java.awt.Color;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JColorChooser;
import javax.swing.BorderFactory;
import javax.swing.plaf.ComponentUI;
import javax.swing.colorchooser.AbstractColorChooserPanel;

// Events
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

// Geometry and images
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;

// Geotools dependencies
import org.geotools.renderer.geom.Arrow2D;


/**
 * Bo�te de dialogue demandant � l'utilisateur de choisir une couleur. La
 * section "aper�u" contiendra une image satellitaire (facultative)  avec
 * une forme g�om�trique par dessus.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@SuppressWarnings("serial")
final class MarkColorChooser extends JColorChooser {
    /**
     * Image � placer en arri�re-plan dans l'aper�u,
     * ou {@code null} s'il n'y en a pas.
     */
    private RenderedImage image;

    /**
     * Transformation affine � appliquer sur l'image pour son affichage.
     * Cette transformation affine comprendra principalement une translation.
     */
    private AffineTransform transform;

    /**
     * Forme g�om�trique utilis�e pour repr�senter une composante
     * sur une image. Cette forme sera utilis�e pour permettre �
     * l'utilisateur de constater de visu l'effet d'un changement
     * de couleur. Cette forme doit �tre centr�e sur (0,0) et ses
     * dimensions doivent �tre en pixels.
     */
    private Shape shape = new Arrow2D(-32f, -24f, 64f, 48f);

    /**
     * Coordonn�es (en pixels) de l'espace occup�e par la ou les formes
     * g�om�triques dont la couleur variera. Ces coordonn�es ne sont pas
     * centr�es � (0,0), contrairement � {@code shape}.
     */
    private Rectangle shapeBounds, paintBounds;

    /**
     * Construit un paneau qui permettra de choisir une couleur
     * pour des composantes � placer par dessus des images.
     *
     * @param simplified {@code true} s'il faut utiliser la forme simplifi�e
     *        de la bo�te de dialogue. La forme simplifi�e ne comprend que la palette
     *        de couleurs, sans les autres paneaux qui servent � contr�ler les
     *        composantes RGBs.
     */
    public MarkColorChooser(final boolean simplified) {
        final Preview preview = new Preview();
        preview.setBorder(BorderFactory.createCompoundBorder(
                          BorderFactory.createEmptyBorder(/*top*/6,/*left*/6,/*bottom*/6,/*right*/6),
                          BorderFactory.createLoweredBevelBorder()));
        setPreviewPanel(preview);
        getSelectionModel().addChangeListener(preview);
        if (simplified) {
            final AbstractColorChooserPanel[] panels = getChooserPanels();
            removeChooserPanel(panels[2]);
            removeChooserPanel(panels[1]);
        }
        setShape(shape);
    }

    /**
     * Sp�cifie l'image � faire appara�tre en arri�re plan, ou {@code null}
     * s'il n'y en a pas. Cette image sera centr�e dans la zone "aper�u" de la
     * bo�te de dialogue.
     */
    public void setBackground(final RenderedImage image) {
        this.image=image;
        transform = (image!=null) ? AffineTransform.getTranslateInstance(-0.5*image.getWidth(),
                                                                         -0.5*image.getHeight()) : null;
    }

    /**
     * Sp�cifie la forme g�om�trique � tracer. Cette forme devrait �tre centr�e
     * � (0,0) et ses coordonn�es devraient �tre exprim�es en pixels. Cette forme
     * sera centr�e dans la zone "aper�u" de la bo�te de dialogue. Les dimensions
     * de cette forme d�termineront les dimensions en pixels de la zone d'aper�u.
     */
    public void setShape(final Shape shape) {
        shapeBounds = shape.getBounds();
        this.shape  = shape;
    }

    /**
     * Duplique la forme g�om�trique. Cette m�thode peut �tre appel�e apr�s
     * {@link #setShape} pour disperser un peu les exemplaires de la forme
     * dans la zone d'aper�u.
     */
    public void duplicateShape(final int count) {
        final AffineTransform transform = AffineTransform.getTranslateInstance(
                                                        -1.25*shape.getBounds2D().getWidth(), 0);
        transform.rotate(Math.toRadians(-30));
        transform.scale(0.75, 0.75);
        final GeneralPath path = new GeneralPath(shape);
        path.transform(transform);
        path.append(shape, false);
        shape = path;
    }

    /**
     * Dessine l'apper�u. Cette m�thode n'a pas � se soucier de r�tablir
     * le graphique dans son �tat original lorsqu'elle aura termin�.
     *
     * @return Un rectangle englobant les coordonn�es
     *         de la forme dont la couleur variera.
     */
    private Rectangle paintPreview(final Graphics2D graphics) {
        if (image != null) {
            graphics.drawRenderedImage(image, transform);
        }
        graphics.setColor(getColor());
        graphics.fill(shape);
        return shape.getBounds();
    }

    /**
     * Classe de la composante qui affichera un apper�u des
     * composantes cartographiques avec leur nouvelle couleur.
     */
    @SuppressWarnings("serial")
    private final class Preview extends JComponent implements ChangeListener {
        /**
         * Utilis� temporairement pour �viter des cr�ation trop fr�quentes.
         */
        private final Insets insets=new Insets(0,0,0,0);

        /**
         * Construit un visualisateur d'aper�us.
         */
        public Preview() {
            setUI(new ComponentUI() {
                public Dimension getPreferredSize(final JComponent c) {
                    final Dimension size=shapeBounds.getSize();
                    size.width  += 24; // Note: se souvenir que de la place
                    size.height += 24; //    est utilis�e pour les bordures
                    return size;
                }

                public void paint(final Graphics g, final JComponent c) {
                    final Insets insets=getInsets(Preview.this.insets);
                    final Graphics2D graphics = (Graphics2D) g;
                    final int centerX = getWidth()/2;
                    final int centerY = getHeight()/2;
                    graphics.clipRect(insets.left, insets.top, getWidth()-(insets.left+insets.right), getHeight()-(insets.top+insets.bottom));
                    graphics.translate(centerX, centerY);
                    paintBounds=paintPreview(graphics);
                    paintBounds.translate(centerX, centerY);
                }
            });
        }

        /**
         * M�thode appel�e automatiquement chaque fois que
         * l'utilisateur s�lectionne une nouvelle couleur.
         */
        public void stateChanged(final ChangeEvent event) {
            if (paintBounds != null) {
                repaint(paintBounds);
            }
        }
    }
}
