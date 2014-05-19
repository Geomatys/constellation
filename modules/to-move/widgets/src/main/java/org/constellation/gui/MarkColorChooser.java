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
import org.geotoolkit.display.shape.Arrow2D;


/**
 * Boîte de dialogue demandant à l'utilisateur de choisir une couleur. La
 * section "aperçu" contiendra une image satellitaire (facultative)  avec
 * une forme géométrique par dessus.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@SuppressWarnings("serial")
final class MarkColorChooser extends JColorChooser {
    /**
     * Image à placer en arrière-plan dans l'aperçu,
     * ou {@code null} s'il n'y en a pas.
     */
    private RenderedImage image;

    /**
     * Transformation affine à appliquer sur l'image pour son affichage.
     * Cette transformation affine comprendra principalement une translation.
     */
    private AffineTransform transform;

    /**
     * Forme géométrique utilisée pour représenter une composante
     * sur une image. Cette forme sera utilisée pour permettre à
     * l'utilisateur de constater de visu l'effet d'un changement
     * de couleur. Cette forme doit être centrée sur (0,0) et ses
     * dimensions doivent être en pixels.
     */
    private Shape shape = new Arrow2D(-32f, -24f, 64f, 48f);

    /**
     * Coordonnées (en pixels) de l'espace occupée par la ou les formes
     * géométriques dont la couleur variera. Ces coordonnées ne sont pas
     * centrées à (0,0), contrairement à {@code shape}.
     */
    private Rectangle shapeBounds, paintBounds;

    /**
     * Construit un paneau qui permettra de choisir une couleur
     * pour des composantes à placer par dessus des images.
     *
     * @param simplified {@code true} s'il faut utiliser la forme simplifiée
     *        de la boîte de dialogue. La forme simplifiée ne comprend que la palette
     *        de couleurs, sans les autres paneaux qui servent à contrôler les
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
     * Spécifie l'image à faire apparaître en arrière plan, ou {@code null}
     * s'il n'y en a pas. Cette image sera centrée dans la zone "aperçu" de la
     * boîte de dialogue.
     */
    public void setBackground(final RenderedImage image) {
        this.image=image;
        transform = (image!=null) ? AffineTransform.getTranslateInstance(-0.5*image.getWidth(),
                                                                         -0.5*image.getHeight()) : null;
    }

    /**
     * Spécifie la forme géométrique à tracer. Cette forme devrait être centrée
     * à (0,0) et ses coordonnées devraient être exprimées en pixels. Cette forme
     * sera centrée dans la zone "aperçu" de la boîte de dialogue. Les dimensions
     * de cette forme détermineront les dimensions en pixels de la zone d'aperçu.
     */
    public void setShape(final Shape shape) {
        shapeBounds = shape.getBounds();
        this.shape  = shape;
    }

    /**
     * Duplique la forme géométrique. Cette méthode peut être appelée après
     * {@link #setShape} pour disperser un peu les exemplaires de la forme
     * dans la zone d'aperçu.
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
     * Dessine l'apperçu. Cette méthode n'a pas à se soucier de rétablir
     * le graphique dans son état original lorsqu'elle aura terminé.
     *
     * @return Un rectangle englobant les coordonnées
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
     * Classe de la composante qui affichera un apperçu des
     * composantes cartographiques avec leur nouvelle couleur.
     */
    @SuppressWarnings("serial")
    private final class Preview extends JComponent implements ChangeListener {
        /**
         * Utilisé temporairement pour éviter des création trop fréquentes.
         */
        private final Insets insets=new Insets(0,0,0,0);

        /**
         * Construit un visualisateur d'aperçus.
         */
        public Preview() {
            setUI(new ComponentUI() {
                public Dimension getPreferredSize(final JComponent c) {
                    final Dimension size=shapeBounds.getSize();
                    size.width  += 24; // Note: se souvenir que de la place
                    size.height += 24; //    est utilisée pour les bordures
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
         * Méthode appelée automatiquement chaque fois que
         * l'utilisateur sélectionne une nouvelle couleur.
         */
        public void stateChanged(final ChangeEvent event) {
            if (paintBounds != null) {
                repaint(paintBounds);
            }
        }
    }
}
