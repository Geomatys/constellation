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
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.BoundedRangeModel;
import javax.swing.text.JTextComponent;
import javax.swing.DefaultBoundedRangeModel;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

// Graphics
import java.awt.Font;
import java.awt.Paint;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.awt.font.FontRenderContext;

// Geometry
import java.awt.Shape;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

// Collections
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.lang.reflect.Array;
import java.util.ConcurrentModificationException;
import javax.media.jai.util.Range;

// Miscellaneous
import java.util.Date;
import java.util.TimeZone;
import java.text.Format;
import javax.units.Unit;

// Geotools dependencies
import org.geotoolkit.util.collection.RangeSet;
import org.geotoolkit.gui.swing.ZoomPane;
import org.geotoolkit.gui.swing.ExceptionMonitor;
import org.geotoolkit.util.converter.ConverterRegistry;

// Axis and graduation
//import org.geotools.units.Unit;
import org.geotoolkit.display.axis.Axis2D;
import org.geotoolkit.display.axis.Graduation;
import org.geotoolkit.display.axis.DateGraduation;
import org.geotoolkit.display.axis.NumberGraduation;
import org.geotoolkit.display.axis.AbstractGraduation;

// Resources
import org.constellation.resources.i18n.Resources;
import org.constellation.resources.i18n.ResourceKeys;


/**
 * Paneau représentant les plages des données disponibles. Ces plages sont
 * représentées par des barres verticales. L'axe des <var>x</var> représente
 * les valeurs, et sur l'axe des <var>y</var> on place les différents types
 * de données, un peu comme le ferait un histogramme.
 *
 * <p>&nbsp;</p>
 * <p align="center"><img src="doc-files/RangeBars.png"></p>
 * <p>&nbsp;</p>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@SuppressWarnings("serial")
public class RangeBars extends ZoomPane {
    /**
     * Small value for floating point comparaison.
     */
    private static final double EPS = 1E-12;

    /**
     * Constant for the horizontal orientation.
     * Labels and bars are both horizontal.
     */
    public static final int HORIZONTAL = SwingConstants.HORIZONTAL;

    /**
     * Constant for the vertical orientation.
     * Labels and bars are both vertical.
     */
    public static final int VERTICAL = SwingConstants.VERTICAL;

    /**
     * Constant for the vertical orientation.
     * Bars are vertical, but labels still horizontal.
     */
    public static final int VERTICAL_EXCEPT_LABELS = 2;

    /**
     * Margin (in pixels) to left in the window after a call to {@link #reset}.
     * Set to 0 in order to use fully all the available area.
     */
    private static final int RESET_MARGIN = 12;

    /**
     * An affine transform for applying a 90° rotation on text labels.
     */
    private static final AffineTransform ROTATE_90 = new AffineTransform(0, 1, -1, 0, 0, 0);

    /**
     * Données des barres. Chaque entré est constitué d'une paire
     * (<em>étiquette</em>, <em>tableau de données</em>). Le tableau de
     * donnée sera généralement (mais pas obligatoirement) un tableau de
     * type {@code long[]}. Les données de ces tableaux seront organisées
     * par paires de valeurs, de la forme (<i>début</i>,<i>fin</i>).
     */
    private final Map<String,RangeSet> ranges = new LinkedHashMap<String,RangeSet>();

    /**
     * Axe des <var>x</var> servant à écrire les valeurs des plages. Les
     * méthodes de {@link Axis2D} peuvent être appellées pour modifier le format
     * des nombres, une étiquette, des unités ou pour spécifier "à la main" les
     * minimums et maximums.
     */
    private final Axis2D axis;

    /**
     * Valeur minimale à avoir été spécifiée avec {@link #addRange}.
     * Cette valeur n'est pas valide si <code>(minimum<maximum)</code>
     * est {@code false}. Cette valeur peut etre calculée par un
     * appel à {@link #ensureValidGlobalRange}.
     */
    private transient double minimum;

    /**
     * Valeur maximale à avoir été spécifiée avec {@link #addRange}.
     * Cette valeur n'est pas valide si <code>(minimum<maximum)</code>
     * est {@code false}. Cette valeur peut etre calculée par un
     * appel à {@link #ensureValidGlobalRange}.
     */
    private transient double maximum;

    /**
     * Zoomable bounds in pixel coordinates. This boundind box is computed from the widget
     * bounds minus the {@linkplain #labelBounds label bounding box}. The computation is
     * performed by:
     *
     * <blockquote><pre>
     * {@code zoomableBounds = getZoomableBounds(zoomableBounds);}
     * </pre></blockquote>
     */
    private transient Rectangle zoomableBounds;

    /**
     * Coordonnées (en pixels) de la région dans laquelle seront dessinées
     * les étiquettes. Ce champ est nul si ces coordonnées ne sont pas encore
     * connues. Ces coordonnées sont calculées par
     *
     *                    {@link #paintComponent(Graphics2D)}
     *
     * (notez que cette dernière accepte un argument {@link Graphics2D} nul).
     */
    private transient Rectangle labelBounds;

    /**
     * Coordonnées (en pixels) de la région dans laquelle sera dessinée l'axe.
     * Ce champ est nul si ces coordonnées ne sont pas encore connues. Ces
     * coordonnées sont calculées par {@link #paintComponent(Graphics2D)}
     * (notez que cette dernière accepte un argument {@link Graphics2D} nul).
     */
    private transient Rectangle axisBounds;

    /**
     * Indique si cette composante sera orientée horizontalement ou
     * verticalement.
     */
    private final boolean horizontal;

    /**
     * {@code true} if labels should be vertical as well when the
     * {@code RangeBars} component is vertical.
     */
    private final boolean verticalLabels;

    /**
     * Indique si la méthode {@link #reset} a été appelée
     * sur cet objet avec une dimension valide de la fenêtre.
     */
    private boolean valid;

    /**
     * Espaces (en pixels) à laisser de chaque côtés
     * du graphique. Ces dimensions seront retournées
     * par {@link #getInsets}.
     */
    private short top=12, left=12, bottom=6, right=15;

    /**
     * Hauteur (en pixels) des barres des histogrammes.
     */
    private final short barThickness=12;

    /**
     * Espace (en pixels) entre les étiquettes et leurs barres.
     */
    private final short barOffset=6;

    /**
     * Espace (en pixels) à ajouter entre deux lignes.
     */
    private final short lineSpacing=6;

    /**
     * Empirical value (in pixels) to add to {@code labelBounds.width}
     * after painting vertical bars. I cant' understand why it is needed!
     */
    private static final int XOFFSET_FOR_VERTICAL_BARS = 4;

    /**
     * The background color for the zoomable area (default to white).
     * This is different from the widget's background color (default
     * to gray), which is specified with {@link #setBackground(Color)}.
     */
    private final Color backgbColor = Color.white;

    /**
     * The bars color (default to orange).
     */
    private final Color barColor = new Color(255, 153, 51);

    /**
     * The slider color. Default to a transparent purple.
     */
    private final Color selColor = new Color(128,  64,  92, 96);

    /*
     * There is no field for label color. Label color can
     * be specified with {@link #setForeground(Color)}.
     */

    /**
     * The border to paint around the zoomable area.
     */
    private final Border border = BorderFactory.createEtchedBorder();

    /**
     * Plage de valeurs présentement sélectionnée par l'utilisateur. Cette
     * plage apparaîtra comme un rectangle transparent (une <em>visière</em>)
     * par dessus les barres. Ce champ est initialement nul. Une visière ne
     * sera créée que lorsqu'elle sera nécessaire.
     */
    private transient MouseReshapeTracker slider;

    /**
     * Modèle permettant de décrire la position de la visière par un entier.
     * Ce model est fournit pour faciliter les interactions avec <i>Swing</i>.
     * Ce champ peut être nul si aucun model n'a encore été demandé.
     */
    private transient SwingModel swingModel;

    /**
     * Point utilisé temporairement lors
     * des transformations affine.
     */
    private transient Point2D.Double point;

    /**
     * Objet {@link #insets} à réutiliser autant que possible.
     */
    private transient Insets insets;

    /**
     * Construit un paneau initialement vide qui représentera des
     * nombres sans unités. Des données pourront être ajoutées avec
     * la méthode {@link #addRange} pour faire apparaître des barres.
     */
    public RangeBars() {
        this((Unit)null, HORIZONTAL);
    }

    /**
     * Construit un paneau initialement vide qui représentera des
     * nombres selon les unités spécifiées. Des données pourront
     * être ajoutées avec la méthode {@link #addRange} pour faire
     * apparaître des barres.
     *
     * @param unit Unit of measure, or {@code null}.
     * @param orientation Either {@link #HORIZONTAL}, {@link #VERTICAL}
     *        or {@link #VERTICAL_EXCEPT_LABELS}.
     */
    public RangeBars(final Unit unit, final int orientation) {
        this(new NumberGraduation(null/*unit*/), // TODO
             isHorizontal    (orientation),
             isVerticalLabels(orientation));
    }

    /**
     * Construit un paneau initialement vide qui représentera des
     * dates dans le fuseau horaire spécifié. Des données pourront
     * être ajoutées avec la méthode {@link #addRange} pour faire
     * apparaître des barres.
     *
     * @param timezone The timezone.
     * @param orientation Either {@link #HORIZONTAL}, {@link #VERTICAL}
     *        or {@link #VERTICAL_EXCEPT_LABELS}.
     */
    public RangeBars(final TimeZone timezone, final int orientation) {
        this(new DateGraduation(timezone),
             isHorizontal    (orientation),
             isVerticalLabels(orientation));
    }

    /**
     * Construit un paneau initialement vide. Des données pourront
     * être ajoutées avec la méthode {@link #addRange} pour faire
     * apparaître des barres.
     */
    private RangeBars(final AbstractGraduation graduation,
                      final boolean horizontal,
                      final boolean verticalLabels)
    {
        super(horizontal ? (TRANSLATE_X|SCALE_X|RESET) : (TRANSLATE_Y|SCALE_Y|RESET));
        this.horizontal     = horizontal;
        this.verticalLabels = verticalLabels;
        axis = new Axis2D(graduation);
        axis.setLabelClockwise(horizontal);
        axis.setRenderingHint(Graduation.AXIS_TITLE_FONT, new Font("SansSerif", Font.BOLD,  11));
        axis.setRenderingHint(Graduation.TICK_LABEL_FONT, new Font("SansSerif", Font.PLAIN, 10));
        LookAndFeel.installColors(this, "Label.background", "Label.foreground");
        setMagnifierEnabled(false);
        /*
         * Resizing vertical bars is trickier than resizing horizontal bars,
         * because vertical bars are aligned on maximal X (right aligned) while
         * horizontal bars are aligned on minimal Y (top aligned). It is easier
         * to simply clear the cache on component resize.
         */
        if (!horizontal) {
            addComponentListener(new ComponentAdapter() {
                public void componentResized(final ComponentEvent event) {
                    clearCache();
                }
            });
        }
        setPaintingWhileAdjusting(true);
    }

    /**
     * Check the orientation.
     *
     * @param orientation Either {@link #HORIZONTAL}, {@link #VERTICAL}
     *        or {@link #VERTICAL_EXCEPT_LABELS}.
     */
    private static boolean isHorizontal(final int orientation) {
        switch (orientation) {
            case HORIZONTAL:             return true;
            case VERTICAL:               return false;
            case VERTICAL_EXCEPT_LABELS: return false;
            default: throw new IllegalArgumentException();
        }
    }

    /**
     * Check the labels orientation.
     *
     * @param orientation Either {@link #HORIZONTAL}, {@link #VERTICAL}
     *        or {@link #VERTICAL_EXCEPT_LABELS}.
     */
    private static boolean isVerticalLabels(final int orientation) {
        switch (orientation) {
            case HORIZONTAL:             return false;
            case VERTICAL:               return true ;
            case VERTICAL_EXCEPT_LABELS: return false;
            default: throw new IllegalArgumentException();
        }
    }

    /**
     * Set the timezone for graduation label. This affect only the way
     * labels are displayed. This method can be invoked only if this
     * {@code RangeBars} has been constructed with the
     * {@code RangeBars(TimeZone)} constructor.
     *
     * @param  timezone The new time zone.
     * @throws IllegalStateException if this {@code RangeBars} has has
     *         not been constructed with the {@code RangeBars(TimeZone)}
     *         constructor.
     */
    public void setTimeZone(final TimeZone timezone) {
        final Graduation graduation = axis.getGraduation();
        if (graduation instanceof DateGraduation) {
            final DateGraduation dateGrad = (DateGraduation) graduation;
            final TimeZone    oldTimezone = dateGrad.getTimeZone();
            dateGrad.setTimeZone(timezone);
            clearCache();
            repaint();
            firePropertyChange("timezone", oldTimezone, timezone);
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Efface toutes les barres qui étaient tracées.
     */
    public synchronized void clear() {
        ranges.clear();
        clearCache();
        repaint();
    }

    /**
     * Efface les barres correspondant à l'étiquette spécifiée.
     */
    public synchronized void remove(final String label) {
        ranges.remove(label);
        clearCache();
        repaint();
    }

    /**
     * Ajoute une plage de valeurs. Chaque plage de valeurs est associée à une
     * étiquette. Il est possible de spécifier (dans n'importe quel ordre)
     * plusieurs plages à une même étiquette. Si deux plages se chevauchent
     * pour une étiquette donnée, elles seront fusionnées ensemble.
     *
     * @param label Etiquette désignant la barre pour laquelle on veut ajouter
     *              une plage. Si cette étiquette avait déjà été utilisée
     *              précédemment, les données seront ajoutées à la barre déjà
     *              existante. Sinon, une nouvelle barre sera créée. Les
     *              différences entres majuscules et minuscules sont prises
     *              en compte. La valeur {@code null} est autorisée.
     * @param first Début de la plage.
     * @param last  Fin de la plage.
     *
     * @throws NullPointerException Si {@code first} ou {@code last} est nul.
     * @throws IllegalArgumentException Si {@code first} et {@code last}
     *         ne sont pas de la même classe, ou s'ils ne sont pas de la classe
     *         des éléments précédemment mémorisés sous l'étiquette {@code label}.
     */
    public synchronized void addRange(final String     label,
                                      final Comparable first,
                                      final Comparable last)
    {
        RangeSet rangeSet = ranges.get(label);
        if (rangeSet == null) {
            rangeSet = new RangeSet(first.getClass());
            ranges.put(label, rangeSet);
        }
        rangeSet.add(first, last);
        clearCache();
        repaint();
    }

    /**
     * Définit les plages de valeurs pour l'étiquette spécifiée.
     * Les anciennes plages de valeurs pour cette étiquette seront
     * oubliées.
     *
     * @param label     Etiquette pour laquelle définir une plage de valeur.
     * @param newRanges Nouvelle plage de valeurs.
     */
    public synchronized void setRanges(final String label, final RangeSet newRanges) {
        if (newRanges != null) {
            ranges.put(label, newRanges);
            clearCache();
            repaint();
        } else {
            remove(label);
        }
    }

    /**
     * Update {@link #minimum} and {@link #maximum} value if it was not already
     * done.   If minimum and maximum was already up to date, then nothing will
     * be done.  This update is performed using all intervals specified to this
     * {@code RangeBars}.
     *
     * @return {@code true} if {@link #minimum} and {@link #maximum} are
     *         valid after this call,  or {@code false} if an update was
     *         necessary but failed for whatever reasons (for example because
     *         there is no intervals in this {@code RangeBars}).
     */
    private boolean ensureValidGlobalRange() {
        if (minimum < maximum) {
            return true;
        }
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (final RangeSet rangeSet : ranges.values()) {
            final int size = rangeSet.size();
            if (size != 0) {
                double tmp;
                if ((tmp=rangeSet.getMinValueAsDouble(0     )) < min) min=tmp;
                if ((tmp=rangeSet.getMaxValueAsDouble(size-1)) > max) max=tmp;
            }
        }
        if (min < max) {
            this.minimum = min;
            this.maximum = max;
            return true;
        }
        return false;
    }

    /**
     * Déclare qu'un changement a été fait et que ce changement
     * peut nécessiter le recalcul d'informations conservées
     * dans une cache interne.
     */
    private void clearCache() {
        minimum = Double.NaN;
        maximum = Double.NaN;
        labelBounds = null;
        axisBounds  = null;
        if (swingModel != null) {
            swingModel.revalidate();
        }
    }

    /**
     * Spécifie la légende de l'axe. La valeur {@code null}
     * signifie qu'il ne faut pas afficher de légende.
     */
    public void setLegend(final String label) {// No 'synchronized' needed here
        ((AbstractGraduation) axis.getGraduation()).setTitle(label);
    }

    /**
     * Retourne la légende de l'axe.
     */
    public String getLegend() { // No 'synchronized' needed here
        return axis.getGraduation().getTitle(false);
    }

    /**
     * Retourne la liste des étiquettes en mémoire, dans l'ordre dans lequel
     * elles seront écrites. Le tableau retourné est une copie des tableaux
     * internes. En conséquence, les changements faits sur ce tableau n'auront
     * pas de répercussions sur {@code this}.
     */
    public synchronized String[] getLabels() {
        return ranges.keySet().toArray(new String[ranges.size()]);
    }

    /**
     * Retourne la valeur minimale mémorisée. Si plusieurs étiquettes ont été
     * spécifiées, elles seront tous prises en compte. Si aucune valeur n'a été
     * mémorisée dans cet objet, alors cette méthode retourne {@code null}.
     */
    public synchronized Comparable getMinimum() {
        return getMinimum(getLabels());
    }

    /**
     * Retourne la valeur maximale mémorisée. Si plusieurs étiquettes ont été
     * spécifiées, elles seront tous prises en compte. Si aucune valeur n'a été
     * mémorisée dans cet objet, alors cette méthode retourne {@code null}.
     */
    public synchronized Comparable getMaximum() {
        return getMaximum(getLabels());
    }

    /**
     * Retourne la valeur minimale mémorisée sous l'étiquette spécifiée. Si aucune
     * donnée n'a été mémorisée sous cette étiquette, retourne {@code null}.
     */
    public Comparable getMinimum(final String label) {
        return getMinimum(new String[] {label});
    }

    /**
     * Retourne la valeur maximale mémorisée sous l'étiquette spécifiée. Si aucune
     * donnée n'a été mémorisée sous cette étiquette, retourne {@code null}.
     */
    public Comparable getMaximum(final String label) {
        return getMaximum(new String[] {label});
    }

    /**
     * Retourne la valeur minimale mémorisée sous les étiquettes spécifiées.
     * Si aucune donnée n'a été mémorisée sous ces étiquettes, retourne
     * {@code null}.
     */
    public synchronized Comparable getMinimum(final String[] labels) {
        Comparable min = null;
        for (int i=0; i<labels.length; i++) {
            final RangeSet rangeSet = ranges.get(labels[i]);
            if (!rangeSet.isEmpty()) {
                final Comparable tmp = ((Range)rangeSet.first()).getMinValue();
                if (min==null || min.compareTo(tmp)>0) {
                    min = tmp;
                }
            }
        }
        return min;
    }

    /**
     * Retourne la valeur maximale mémorisée sous les étiquettes spécifiées.
     * Si aucune donnée n'a été mémorisée sous ces étiquettes, retourne
     * {@code null}.
     */
    public synchronized Comparable getMaximum(final String[] labels) {
        Comparable max = null;
        for (int i=0; i<labels.length; i++) {
            final RangeSet rangeSet = ranges.get(labels[i]);
            if (!rangeSet.isEmpty()) {
                final Comparable tmp = ((Range)rangeSet.last()).getMaxValue();
                if (max==null || max.compareTo(tmp)<0) {
                    max = tmp;
                }
            }
        }
        return max;
    }

    /**
     * Déclare qu'on aura besoin d'une visière. Cette méthode Vérifie que
     * {@code slider} est non-nul. S'il était nul, une nouvelle visière
     * sera créée et positionnée. Si on n'avait pas assez d'informations pour
     * positionner la visière, sa création sera annulée.
     */
    private void ensureSliderCreated() {
        if (slider != null) {
            return;
        }
        slider = new MouseReshapeTracker() {
            /** Invoked after the position and size of the visor has changed. */
            protected void stateChanged(final boolean isAdjusting) {
                if (swingModel != null) {
                    swingModel.fireStateChanged(isAdjusting);
                }
            }

            /** Invoked when a change in the clip is required (e.g. user edited a field). */
            protected void clipChangeRequested(double xmin, double xmax, double ymin, double ymax) {
                setVisibleRange(xmin, xmax, ymin, ymax);
            }
        };
        addMouseListener(slider);
        addMouseMotionListener(slider);
        /*
         * Si un modèle existait, on l'utilisera pour
         * définir la position initiale de la visière.
         * Sinon, on construira un nouveau modèle.
         */
        if (swingModel == null) {
            if (ensureValidGlobalRange()) {
                final double min = this.minimum;
                final double max = this.maximum;
                if (horizontal) {
                    slider.setX(min, min+0.25*(max-min));
                } else {
                    slider.setY(min, min+0.25*(max-min));
                }
            }
        } else {
            swingModel.synchronize();
        }
    }

    /**
     * Retourne la valeur au centre de la
     * plage sélectionnée par l'utilisateur.
     */
    public double getSelectedValue() {
        if (slider == null) {
            return Double.NaN;
        }
        return horizontal ? slider.getCenterX() : slider.getCenterY();
    }

    /**
     * Retourne la valeur au début de la
     * plage sélectionnée par l'utilisateur.
     */
    public double getMinSelectedValue() {
        if (slider == null) {
            return Double.NaN;
        }
        return horizontal ? slider.getMinX() : slider.getMinY();
    }

    /**
     * Retourne la valeur à la fin de la
     * plage sélectionnée par l'utilisateur.
     */
    public double getMaxSelectedValue() {
        if (slider == null) {
            return Double.NaN;
        }
        return horizontal ? slider.getMaxX() : slider.getMaxY();
    }

    /**
     * Spécifie la plage de valeurs à sélectionner.
     * Cette plage de valeurs apparaîtra comme un
     * rectangle transparent superposé aux barres.
     */
    public void setSelectedRange(final double min, final double max) {
        ensureSliderCreated();
        repaint(slider.getBounds());
        if (horizontal) {
            slider.setX(min, max);
        } else {
            slider.setY(min, max);
        }
        /*
         * Déclare que la position de la visière à changée.
         * Les barres seront redessinées et le model sera
         * prévenu du changement
         */
        repaint(slider.getBounds());
        if (swingModel != null) {
            swingModel.fireStateChanged(false);
        }
    }

    /**
     * Modifie le zoom du graphique de façon à faire apparaître la
     * plage de valeurs spécifiée. Si l'intervale spécifié n'est pas
     * entièrement compris dans la plage des valeurs en mémoire, cette
     * méthode décalera et/ou zoomera l'intervale spécifié de façon à
     * l'inclure dans la plage des valeurs en mémoire.
     *
     * @param min Valeur minimale.
     * @param max Valeur maximale.
     */
    public void setVisibleRange(final double min, final double max) {
        if (horizontal) {
            setVisibleRange(min, max, Double.NaN, Double.NaN);
        } else {
            setVisibleRange(Double.NaN, Double.NaN, min, max);
        }
    }

    /**
     * Modifie le zoom du graphique de façon à faire apparaître la
     * plage de valeurs spécifiée. Si l'intervale spécifié n'est pas
     * entièrement compris dans la plage des valeurs en mémoire, cette
     * méthode décalera et/ou zoomera l'intervale spécifié de façon à
     * l'inclure dans la plage des valeurs en mémoire.
     */
    private void setVisibleRange(double xmin, double xmax, double ymin, double ymax) {
        if (ensureValidGlobalRange()) {
            final double minimim = this.minimum;
            final double maximum = this.maximum;
            final Insets insets  = this.insets = getInsets(this.insets);
            final int    top     = insets.top;
            final int    left    = insets.left;
            final int    bottom  = insets.bottom;
            final int    right   = insets.right;
            if (horizontal) {
                /*
                 * Note: "xmax -= (xmin-minimum)" is an abreviation of
                 *       "xmax  = (xmax-xmin) + minimum".  Setting new
                 *       values for "xmin" and "xmax" is an intentional
                 *       side effect of "if" clause, to be run only if
                 *       the first "if" term is true.
                 */
                if (xmin<minimum && maximum<(xmax -= xmin-(xmin=minimum))) xmax=maximum;
                if (xmax>maximum && minimum>(xmin -= xmax-(xmax=maximum))) xmin=minimum;
                if (xmin < xmax) {
                    setVisibleArea(new Rectangle2D.Double(xmin, top, xmax-xmin,
                                       Math.max(bottom-top, barThickness)));
                    if (slider != null) {
                        final int height = Math.max(barThickness, getZoomableHeight());
                        slider.setClipMinMax(xmin, xmax, top, top+height);
                    }
                }
            } else {
                if (ymin<minimum && maximum<(ymax -= ymin-(ymin=minimum))) ymax=maximum;
                if (ymax>maximum && minimum>(ymin -= ymax-(ymax=maximum))) ymin=minimum;
                if (ymin < ymax) {
                    final int rightAlign = Math.max(getWidth()-right, left);
                    final int width = Math.max(barThickness, getZoomableWidth());
                    setVisibleArea(new Rectangle2D.Double(rightAlign-width, ymin,
                                       Math.max(rightAlign, barThickness), ymax-ymin));
                    if (slider != null) {
                        slider.setClipMinMax(rightAlign-width, rightAlign, ymin, ymax);
                    }
                }
            }
        }
    }

    /**
     * Returns {@code true} if user is allowed to edit
     * or drag the slider's dimension. If {@code false},
     * then the user can change the slider's location but not
     * its dimension.
     */
    public boolean isRangeAdjustable() {
        if (slider == null) {
            return false;
        }
        if (horizontal) {
            return slider.isAdjustable(SwingConstants.EAST) ||
                   slider.isAdjustable(SwingConstants.WEST);
        } else {
            return slider.isAdjustable(SwingConstants.NORTH) ||
                   slider.isAdjustable(SwingConstants.SOUTH);
        }
    }

    /**
     * Specify if the user is allowed to edit or drag the slider's dimension.
     * If {@code true}, then the user is allowed to change both slider's
     * dimension and location. If {@code false}, then the user is allowed
     * to change slider's location only.
     */
    public void setRangeAdjustable(final boolean b) {
        ensureSliderCreated();
        if (horizontal) {
            slider.setAdjustable(SwingConstants.EAST, b);
            slider.setAdjustable(SwingConstants.WEST, b);
        } else {
            slider.setAdjustable(SwingConstants.NORTH, b);
            slider.setAdjustable(SwingConstants.SOUTH, b);
        }
    }

    /**
     * Set the font for labels and graduations. This font is applied "as is"
     * to labels. However, graduations will use a slightly smaller and plain
     * font, even if the specified font was in bold or italic.
     */
    public void setFont(final Font font) {
        super.setFont(font);
        axis.setRenderingHint(Graduation.AXIS_TITLE_FONT, font);
        final int size = font.getSize();
        axis.setRenderingHint(Graduation.TICK_LABEL_FONT,
                              font.deriveFont(Font.PLAIN, size-(size>=14 ? 2 : 1)));
        clearCache();
    }

    /**
     * Retourne le nombre de pixels à laisser entre la région dans laquelle les
     * barres sont dessinées et les bords de cette composante. <strong>Notez que
     * les marges retournées par {@code getInsets(Insets)} peuvent etre plus
     * grandes que celles qui ont été spécifiées à {@link #setInsets}.</strong>
     * Un espace suplémentaire peut avoir ajouté pour tenir compte d'une
     * éventuelle bordure qui aurait été ajoutée à la composante.
     *
     * @param  insets Objet à réutiliser si possible, ou {@code null}.
     * @return Les marges à laisser de chaque côté de la zone de traçage.
     */
    public Insets getInsets(Insets insets) {
        insets = super.getInsets(insets);
        insets.top    += top;
        insets.left   += left;
        insets.bottom += bottom;
        insets.right  += right;
        return insets;
    }

    /**
     * Défini le nombre de pixels à laisser entre la région dans laquelle les
     * barres sont dessinées et les bords de cette composante. Ce nombre de
     * pixels doit être suffisament grand pour laisser de la place pour les
     * étiquettes de l'axe. Notez que {@link #getInsets} ne va pas
     * obligatoirement retourner exactement ces marges.
     */
    public void setInsets(final Insets insets) {
        top    = (short) insets.top;
        left   = (short) insets.left;
        bottom = (short) insets.bottom;
        right  = (short) insets.right;
        repaint();
    }

    /**
     * Returns the bounding box (in pixel coordinates) of the zoomable area.
     * This implementation returns bounding box covering only a sub-area of
     * this widget area, because space is needed for axis and labels. An extra
     * margin of {@link #getInsets} is also reserved.
     *
     * @param  bounds An optional pre-allocated rectangle, or {@code null}
     *                to create a new one. This argument is useful if the caller
     *                wants to avoid allocating a new object on the heap.
     * @return The bounding box of the zoomable area, in pixel coordinates
     *         relative to this {@code RangeBars} widget.
     */
    protected Rectangle getZoomableBounds(Rectangle bounds) {
        bounds = super.getZoomableBounds(bounds);
        /*
         * 'labelBounds' is the rectangle (in pixels) where legends are going
         * to be displayed.   If this rectangle has not been computed yet, it
         * can be computed now with 'paintComponent(null)'.
         */
        if (labelBounds == null) {
            if (!valid) {
                reset(bounds);
            }
            paintComponent(null, bounds.width  + (left+right),
                                 bounds.height + (top+bottom));
            if (labelBounds == null) {
                return bounds;
            }
        }
        if (horizontal) {
            bounds.x     += labelBounds.width;
            bounds.width -= labelBounds.width;
            bounds.height = labelBounds.height;
            // No changes to bounds.y: align on top.
        } else {
            final int width = getZoomableWidth();
            bounds.y       += labelBounds.height;
            bounds.height  -= labelBounds.height;
            bounds.x       += bounds.width - width; // Align right.
            bounds.width    = width;
        }
        return bounds;
    }

    /**
     * Returns the width of the zoomable area. This method do not trigger
     * zoomable bounds computation if bounds was not readily available.
     */
    private int getZoomableWidth() {
        if (horizontal || verticalLabels) {
            return (labelBounds!=null) ? labelBounds.width : getWidth();
        }
        return (barThickness+lineSpacing)*ranges.size() + XOFFSET_FOR_VERTICAL_BARS;
    }

    /**
     * Returns the height of the zoomable area. This method do not trigger
     * zoomable bounds computation if bounds was not readily available.
     */
    private int getZoomableHeight() {
        return (labelBounds!=null) ? labelBounds.height : getHeight();
    }

    /**
     * Returns the default size for this component.  This is the size
     * returned by {@link #getPreferredSize} if no preferred size has
     * been explicitly set with {@link #setPreferredSize}.
     *
     * @return The default size for this component.
     */
    protected Dimension getDefaultSize() {
        final Insets insets = this.insets = getInsets(this.insets);
        final int top    = insets.top;
        final int left   = insets.left;
        final int bottom = insets.bottom;
        final int right  = insets.right;
        final Dimension size=super.getDefaultSize();
        if (labelBounds==null || axisBounds==null) {
            if (!valid) {
                /*
                 * Force immediate computation of an approximative affine
                 * transform (for the zoom). A more precise affine transform
                 * may be computed later.
                 */
                reset(new Rectangle(left, top,
                                    size.width  - (left+right),
                                    size.height - (top+bottom)));
            }
            paintComponent(null, size.width, size.height);
            if (labelBounds==null || axisBounds==null) {
                size.width  = 280;
                size.height =  60;
                return size;
            }
        }
        if (horizontal) {
            // height = [bottom of axis] - [top of labels] + [margin].
            size.height = (axisBounds.y + axisBounds.height) - labelBounds.y + (bottom + top);
        } else {
            // width = [right of labels] - [left of axis] + [margin].
            size.width = (labelBounds.x + labelBounds.width) - axisBounds.x + (right + left);
        }
        return size;
    }

    /**
     * Invoked when this component must be drawn but no data are available
     * yet. Default implementation paint the text "No data" in the middle
     * of the component.
     *
     * @param graphics The paint context to draw to.
     */
    protected void paintNodata(final Graphics2D graphics) {
        graphics.setColor(getForeground());
        final Resources  resources = Resources.getResources(getLocale());
        final String       message = resources.getString(ResourceKeys.NO_DATA_TO_DISPLAY);
        final FontRenderContext fc = graphics.getFontRenderContext();
        final GlyphVector   glyphs = getFont().createGlyphVector(fc, message);
        final Rectangle2D   bounds = glyphs.getVisualBounds();
        graphics.drawGlyphVector(glyphs, (float) (0.5*(getWidth()-bounds.getWidth())),
                                         (float) (0.5*(getHeight()+bounds.getHeight())));
    }

    /**
     * Draw the bars, labels and their graduation. Bars and labels are drawn in
     * the same order as they were specified to {@link #addRange}.
     *
     * @param graphics The paint context to draw to.
     */
    protected void paintComponent(final Graphics2D graphics) {
        paintComponent(graphics, getWidth(), getHeight());
    }

    /**
     * Implementation of {@link #paintComponent(Graphics2D)}.
     * This special implementation is invoked by {@link #getZoomableBounds})
     * and {@link #getDefaultSize}. It is not too much a problem if this method
     * is not in synchronization with {@link #paintComponent(Graphics2D)} (for
     * example because the user overrided it). The user can fix the problem by
     * overriding {@link #getZoomableBounds}) and {@link #getDefaultSize} too.
     *
     * @param graphics The paint context to draw to.
     * @param componentWidth Width of this component. This information is usually
     *        given by {@link #getWidth}, except when this method is invoked from
     *        a method computing this component's dimension!
     * @param componentHeight Height of this component. This information is usually
     *        given by {@link #getHeight}, except when this method is invoked from
     *        a method computing this component's dimension!
     */
    private void paintComponent(final Graphics2D graphics,
                                final int componentWidth,
                                final int componentHeight)
    {
        final int rangeCount = ranges.size();
        if (rangeCount==0 || !ensureValidGlobalRange()) {
            if (graphics != null) {
                paintNodata(graphics);
            }
            return;
        }
        final Insets borderInsets = (border!=null) ? border.getBorderInsets(this) : null;
        final Insets insets = this.insets = getInsets(this.insets);
        final int    top = insets.top;
        final int   left = insets.left;
        final int bottom = insets.bottom;
        final int  right = insets.right;
        final AbstractGraduation graduation = (AbstractGraduation) axis.getGraduation();
        final GlyphVector[] glyphs = new GlyphVector[rangeCount];
        final double[] labelAscent = new double     [rangeCount];
        final double[]  labelWidth = (!horizontal && !verticalLabels) ? new double[rangeCount] : null;
        final Shape           clip;
        final FontRenderContext fc;
        if (graphics == null) {
            clip = null;
            fc   = new FontRenderContext(null, false, false);
            /*
             * Do not invoke reset() here because this block has probably
             * been executed in order to compute this component's size,
             * i.e. this method has probably been invoked by reset() itself!
             */
        } else {
            if (!valid) {
                reset();
            }
            clip = graphics.getClip();
            fc   = graphics.getFontRenderContext();
        }
        /*
         * Setup an array of "glyph vectors" for labels. Gylph vectors will be
         * drawn later.   Before drawing, we query all glyph vectors for their
         * size, then we compute a typical "slot" size that will be applied to
         * every label.
         */
        double labelSlotWidth;
        double labelSlotHeight;
        if (clip==null || labelBounds==null || clip.intersects(labelBounds)) {
            Font font = getFont();
            if (font == null) {
                font = UIManager.getFont("Panel.font");
                if (font == null) {
                    throw new IllegalStateException();
                }
            }
            if (horizontal) {
                labelSlotWidth  = 0;
                labelSlotHeight = barThickness;
            } else if (verticalLabels) {
                // Rotate font 90°
                font = font.deriveFont(ROTATE_90);
                labelSlotWidth  = barThickness;
                labelSlotHeight = 0;
            } else {
                labelSlotWidth  = 0;
                labelSlotHeight = 0;
            }
            final Iterator<String> it = ranges.keySet().iterator();
            for (int i=0; i<rangeCount; i++) {
                final String label = it.next();
                if (label != null) {
                    glyphs[i] = font.createGlyphVector(fc, label);
                    Rectangle2D rect = glyphs[i].getVisualBounds();
                    double    height = rect.getHeight();
                    double     width = rect.getWidth();
                    if (horizontal) {
                        labelAscent[i] = height;
                    } else if (verticalLabels) {
                        labelAscent[i] = width;
                    } else {
                        labelWidth [i] = width;
                        labelAscent[i] = height;
                        width += i*(barThickness + lineSpacing);
                    }
                    if (width >labelSlotWidth ) labelSlotWidth =width;
                    if (height>labelSlotHeight) labelSlotHeight=height;
                }
            }
            if (it.hasNext()) {
                // Should not happen
                throw new ConcurrentModificationException();
            }
            if (labelBounds == null) {
                labelBounds = new Rectangle();
            }
            if (horizontal) {
                labelSlotWidth  += barOffset;
                labelSlotHeight += lineSpacing;
                labelBounds.setBounds(left, top,
                                      (int)Math.ceil(labelSlotWidth),
                                      (int)Math.ceil(labelSlotHeight*rangeCount));
            } else if (verticalLabels) {
                labelSlotHeight += barOffset;
                labelSlotWidth  += lineSpacing;
                labelBounds.setBounds(componentWidth-right, top,
                                      (int)Math.ceil(labelSlotWidth*rangeCount),
                                      (int)Math.ceil(labelSlotHeight));
                labelBounds.width += XOFFSET_FOR_VERTICAL_BARS; // Empirical adjustement
                labelBounds.x -= labelBounds.width;
            } else {
                labelSlotHeight += lineSpacing/2;
                labelBounds.setBounds(componentWidth-right, top,
                                      (int)Math.ceil(labelSlotWidth),
                                      (int)Math.ceil(labelSlotHeight*rangeCount));
                labelBounds.width += XOFFSET_FOR_VERTICAL_BARS; // Empirical adjustement
                labelBounds.x -= labelBounds.width;
            }
        }
        double barSlotSize;
        labelSlotWidth  = labelBounds.getWidth();
        labelSlotHeight = labelBounds.getHeight();
        if (horizontal) {
            labelSlotHeight /= rangeCount;
            barSlotSize = labelSlotHeight;
        } else if (verticalLabels) {
            labelSlotWidth = (labelSlotWidth-XOFFSET_FOR_VERTICAL_BARS) / rangeCount;
            barSlotSize = labelSlotWidth;
        } else {
            labelSlotHeight /= rangeCount;
            barSlotSize = (barThickness + lineSpacing);
        }
        /*
         * Now, we know the space needed for all labels.  It is time to compute
         * the axis position. This axis will be below horizontal bars or at the
         * right of vertical bars.   We also calibrate the axis for its minimum
         * and maximum values, which are zoom dependent.
         */
        try {
            Point2D.Double point = this.point;
            if (point == null) {
                this.point = point = new Point2D.Double();
            } if (horizontal) {
                double y  = labelBounds.getMaxY();
                double x1 = labelBounds.getMaxX();
                double x2 = componentWidth - right;
                /*
                 * Compute the minimal logical value,
                 * which is at the left of the axis.
                 */
                point.setLocation(x1, y);
                zoom.inverseTransform(point, point);
                graduation.setMinimum(point.x);
                if (point.x < minimum) {
                    graduation.setMinimum(point.x=minimum);
                    zoom.transform(point, point);
                    x1 = point.x;
                }
                /*
                 * Compute the maximal logical value,
                 * which is at the right of the axis.
                 */
                point.setLocation(x2, y);
                zoom.inverseTransform(point, point);
                graduation.setMaximum(point.x);
                if (point.x > maximum) {
                    graduation.setMaximum(point.x=maximum);
                    zoom.transform(point, point);
                    x2 = point.x;
                }
                axis.setLine(x1, y, x2, y);
            } else {
                double x  = verticalLabels ? labelBounds.getMinX() :
                                             labelBounds.getMaxX() - getZoomableWidth();
                double y1 = componentHeight - bottom;
                double y2 = labelBounds.getMaxY();
                if (borderInsets != null) {
                    x -= borderInsets.left;
                }
                /*
                 * Compute the minimal logical value,
                 * which is at the bottom of the axis.
                 */
                point.setLocation(x, y1);
                zoom.inverseTransform(point, point);
                graduation.setMinimum(point.y);
                if (point.y < minimum) {
                    graduation.setMinimum(point.y=minimum);
                    zoom.transform(point, point);
                    y1 = point.y;
                }
                /*
                 * Compute the maximal logical value,
                 * which is at the top of the axis.
                 */
                point.setLocation(x, y2);
                zoom.inverseTransform(point, point);
                graduation.setMaximum(point.y);
                if (point.y > maximum) {
                    graduation.setMaximum(point.y=maximum);
                    zoom.transform(point, point);
                    y2 = point.y;
                }
                axis.setLine(x, y1, x, y2);
            }
        } catch (NoninvertibleTransformException exception) {
            // Should not happen
            ExceptionMonitor.paintStackTrace(graphics, getBounds(), exception);
            return;
        }
        /*
         * Prepare the painting. Paint the border first,
         * then paint all labels. Paint bars next, and
         * paint axis last.
         */
        if (graphics != null) {
            final Color   foreground = getForeground();
            final double clipMinimum = graduation.getMinimum();
            final double clipMaximum = graduation.getMaximum();
            zoomableBounds = getZoomableBounds(zoomableBounds);
            if (border != null) {
                border.paintBorder(this, graphics,
                                   zoomableBounds.x-borderInsets.left,
                                   zoomableBounds.y-borderInsets.top,
                                   zoomableBounds.width+(borderInsets.left+borderInsets.right),
                                   zoomableBounds.height+(borderInsets.top+borderInsets.bottom));
            }
            graphics.setColor(foreground);
            for (int i=0; i<rangeCount; i++) {
                if (glyphs[i] != null) {
                    float x,y;
                    if (horizontal) {
                        x = labelBounds.x;
                        y = (float) (labelBounds.y + i*labelSlotHeight +
                                     0.5*(labelSlotHeight+labelAscent[i]));
                    } else if (verticalLabels) {
                        y = labelBounds.y;
                        x = (float) (labelBounds.x + i*labelSlotWidth +
                                     0.5*labelAscent[i]);
                    } else {
                        x = (float) (labelBounds.x + labelBounds.width - (rangeCount-i)*barSlotSize);
                        y = (float) (labelBounds.y + labelBounds.height - i*labelSlotHeight -
                                     0.5*(labelSlotHeight+labelAscent[i]));
                        final int ox = Math.round((float) (x + 0.5*barSlotSize));
                        final int oy = Math.round((float) (y - 0.5*labelAscent[i]));
                        graphics.drawLine(Math.round(x)+3, oy, ox, oy);
                        graphics.drawLine(ox, oy, ox,
                                Math.round((float) (labelBounds.y + labelBounds.height))-3);
                        x -= labelWidth[i];
                    }
                    graphics.drawGlyphVector(glyphs[i], x, y);
                }
            }
            graphics.setColor(backgbColor);
            graphics.fill    (zoomableBounds);
            graphics.clip    (zoomableBounds);
            graphics.setColor(barColor);
            final Iterator<RangeSet> it=ranges.values().iterator();
            final Rectangle2D.Double bar = new Rectangle2D.Double();
            final double scale, translate;
            if (horizontal) {
                scale      = zoom.getScaleX();
                translate  = zoom.getTranslateX();
                bar.y      = zoomableBounds.y + 0.5*(barSlotSize-barThickness);
                bar.height = barThickness;
            } else {
                scale     = zoom.getScaleY();
                translate = zoom.getTranslateY();
                bar.x     = zoomableBounds.x + 0.5*barThickness;
                bar.width = barThickness;
            }
            for (int i=0; i<rangeCount; i++) {
                final RangeSet rangeSet = it.next();
                final int size = rangeSet.size();
                for (int j=0; j<size; j++) {
                    final double bar_min = rangeSet.getMinValueAsDouble(j);
                    final double bar_max = rangeSet.getMaxValueAsDouble(j);
                    if (bar_min > clipMaximum) break; // Slight optimization
                    if (bar_max > clipMinimum) {
                        if (horizontal) {
                            bar.x      = bar_min;
                            bar.width  = bar_max-bar_min;
                            bar.width *= scale;
                            bar.x     *= scale;
                            bar.x     += translate;
                        } else {
                            bar.y       = bar_max;
                            bar.height  = bar_min-bar_max;
                            bar.height *= scale;
                            bar.y      *= scale;
                            bar.y      += translate;
                        }
                        graphics.fill(bar);
                    }
                }
                if (horizontal) {
                    bar.y += barSlotSize;
                } else {
                    bar.x += barSlotSize;
                }
            }
            if (it.hasNext()) {
                // Should not happen
                throw new ConcurrentModificationException();
            }
            graphics.setClip(clip);
            graphics.setColor(foreground);
            axis.paint(graphics);
            /*
             * The component is now fully painted. If a slider is visible, paint
             * the slider on top of everything else. The slider must always been
             * painted, no matter what 'MouseReshapeTracker.isEmpty()' said.
             */
            if (slider != null) {
                if (swingModel != null) {
                    swingModel.synchronize();
                }
                if (horizontal) {
                    final double ymin = zoomableBounds.getMinY();
                    final double ymax = zoomableBounds.getMaxY();
                    slider.setClipMinMax(clipMinimum, clipMaximum, ymin, ymax);
                    slider.setY         (                          ymin, ymax);
                } else {
                    final double xmin = zoomableBounds.getMinX();
                    final double xmax = zoomableBounds.getMaxX();
                    slider.setClipMinMax(xmin, xmax, clipMinimum, clipMaximum);
                    slider.setX         (xmin, xmax);
                }
                graphics.clip(zoomableBounds);
                graphics.transform(zoom);
                graphics.setColor(selColor);
                graphics.fill(slider);
            }
        }
        /*
         * Recompute axis bounds again. It has already been computed sooner,
         * but bounds may be more precise after painting.  Next, we slightly
         * increase its size to avoid unpainted zones after {@link #repaint}
         * calls.
         */
        axisBounds = axis.getBounds();
        axisBounds.height++;
    }

    /**
     * Apply a transform on the {@linkplain #zoom zoom}. This method override
     * {@link ZoomPane#transform(AffineTransform)} in order to make sure that
     * the supplied transform will not get the bars out of the component.  If
     * the transform would push all bars out, then it will not be applied.
     *
     * @param  change The change to apply, in logical coordinates.
     * @throws UnsupportedOperationException if the transform {@code change}
     *         contains an unsupported transformation, for example a vertical
     *         translation while this component is drawing only horizontal bars.
     */
    public void transform(final AffineTransform change) throws UnsupportedOperationException {
        /*
         * First, make sure that the transformation is a supported one.
         * Shear and rotation are not allowed. Scale is allowed only
         * along the main axis direction.
         */
        if (!(Math.abs(change.getShearX()  )<=EPS &&
              Math.abs(change.getShearY()  )<=EPS && horizontal ?
             (Math.abs(change.getScaleY()-1)<=EPS && Math.abs(change.getTranslateY())<=EPS) :
             (Math.abs(change.getScaleX()-1)<=EPS && Math.abs(change.getTranslateX())<=EPS)))
        {
            throw new UnsupportedOperationException("Unexpected transform");
        }
        /*
         * Check if applying the transform would push all bars out
         * of the component. If so, then exit without applying the
         * transform.
         */
        if (ensureValidGlobalRange() && (zoomableBounds=getZoomableBounds(zoomableBounds))!=null) {
            Point2D.Double point = this.point;
            if (point == null) {
                this.point = point = new Point2D.Double();
            }
            if (horizontal) {
                final int xLeft  = zoomableBounds.x;
                final int xRight = zoomableBounds.width + xLeft;
                final int margin = zoomableBounds.width / 4;
                final double x1, x2, y=zoomableBounds.getCenterY();
                point.x=minimum; point.y=y; change.transform(point,point); zoom.transform(point,point); x1=point.x;
                point.x=maximum; point.y=y; change.transform(point,point); zoom.transform(point,point); x2=point.x;
                if (Math.min(x1,x2)>(xRight-margin) || Math.max(x1,x2)<(xLeft+margin) || Math.abs(x2-x1)<margin) {
                    return;
                }
            } else {
                final int yTop    = zoomableBounds.y;
                final int yBottom = zoomableBounds.height + yTop;
                final int margin  = zoomableBounds.height / 4;
                final double y1, y2, x=zoomableBounds.getCenterX();
                point.y=minimum; point.x=x; change.transform(point,point); zoom.transform(point,point); y1=point.y;
                point.y=maximum; point.x=x; change.transform(point,point); zoom.transform(point,point); y2=point.y;
                if (Math.min(y1,y2)>(yBottom-margin) || Math.max(y1,y2)<(yTop+margin) || Math.abs(y2-y1)<margin) {
                    return;
                }
            }
        }
        /*
         * Applique la transformation, met à jour la transformation
         * de la visière et redessine l'axe en plus du graphique.
         */
        super.transform(change);
        if (slider != null) {
            slider.setTransform(zoom);
        }
        if (axisBounds != null) {
            repaint(axisBounds);
        }
    }

    /**
     * Reset the zoom in such a way that every bars fit in the display area.
     */
    public void reset() {
        reset(zoomableBounds=getZoomableBounds(zoomableBounds));
        if (getWidth()>0 && getHeight()>0) {
            valid = true;
        }
    }

    /**
     * Reset the zoom in such a way that every bars fit in the specified display area.
     */
    private void reset(Rectangle zoomableBounds) {
        if (RESET_MARGIN != 0) {
            zoomableBounds = (Rectangle) zoomableBounds.clone();
            if (horizontal) {
                final int margin = Math.min(RESET_MARGIN, zoomableBounds.width/2);
                zoomableBounds.x     += margin;
                zoomableBounds.width -= margin*2;
            } else {
                final int margin = Math.min(RESET_MARGIN, zoomableBounds.height/2);
                zoomableBounds.y      += margin;
                zoomableBounds.height -= margin*2;
            }
        }
        reset(zoomableBounds, !horizontal);
        if (slider != null) {
            slider.setTransform(zoom);
        }
        if (axisBounds != null) {
            repaint(axisBounds);
        }
    }

    /**
     * Returns logical coordinates for the display area.
     */
    public Rectangle2D getArea() {
        final Insets insets = this.insets = getInsets(this.insets);
        final int top    = insets.top;
        final int left   = insets.left;
        final int bottom = insets.bottom;
        final int right  = insets.right;
        if (ensureValidGlobalRange()) {
            final double min = this.minimum;
            final double max = this.maximum;
            if (horizontal) {
                int height = getHeight();
                if (height==0) {
                    height = getMinimumSize().height;
                    // Height doesn't need to be exact,
                    // since it will be ignored anyway...
                }
                return new Rectangle2D.Double(min, top, max-min,
                                              Math.max(height-(top+bottom),16));
            } else {
                int width = getWidth();
                if (width==0) {
                    width = getMinimumSize().width;
                    // Width doesn't need to be exact,
                    // since it will be ignored anyway...
                }
                return new Rectangle2D.Double(left, min,
                                              Math.max(width-(left+right),16),
                                              max-min);
            }
        }
        /*
         * This block will be run only if logical coordinate of display area
         * can't be computed, because of not having enough informations.  It
         * make a simple guess, which is better than nothing.
         */
        final Rectangle bounds = getBounds();
        bounds.x       = left;
        bounds.y       = top;
        bounds.width  -= (left+right);
        bounds.height -= (top+bottom);
        return bounds;
    }

    /**
     * Retourne un model pouvant décrire la position de la visière dans une
     * plage d'entiers. Ce model est fournit pour faciliter les interactions
     * avec <i>Swing</i>. Ses principales méthodes sont définies comme suit:
     *
     * <p>{@link BoundedRangeModel#getValue}<br>
     *    Retourne la position du bord gauche de la visière, exprimée par
     *    un entier compris entre le minimum et le maximum du model (0 et
     *    100 par défaut).</p>
     *
     * <p>{@link BoundedRangeModel#getExtent}<br>
     *    Retourne la largeur de la visière, exprimée selon les mêmes unités
     *    que {@code getValue()}.</p>
     *
     * <p>{@link BoundedRangeModel#setMinimum} / {@link BoundedRangeModel#setMaximum}<br>
     *    Modifie les valeurs entière minimale ou maximale retournées par {@code getValue()}.
     *    Cette modification n'affecte aucunement l'axe des barres affichées; elle
     *    ne fait que modifier la façon dont la position de la visière est convertie
     *    en valeur entière par {@code getValue()}.</p>
     *
     * <p>{@link BoundedRangeModel#setValue} / {@link BoundedRangeModel#setExtent}<br>
     *    Modifie la position du bord gauche de la visière ou sa largeur.</p>
     */
    public synchronized LogicalBoundedRangeModel getModel() {
        if (swingModel == null) {
            ensureSliderCreated();
            swingModel = new SwingModel();
        }
        return swingModel;
    }

    /**
     * A {@link javax.swing.BoundedRangeModel} for use with {@link RangeBars}. This model can
     * maps integer values (usually in the range 0 to 100) to floating-point "logical" values.
     * The method {@link #fireStateChanged(boolean)} is invoked every time the user moved the
     * slider.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    private final class SwingModel extends DefaultBoundedRangeModel implements LogicalBoundedRangeModel {
        /**
         * Pour compatibilités entre les enregistrements binaires de différentes versions.
         */
        private static final long serialVersionUID = -5691592959010874291L;

        /**
         * Valeur minimale. La valeur {@code NaN} indique qu'il
         * faut puiser le minimum dans les données de {@link RangeBars}.
         */
        private double minimum = Double.NaN;

        /**
         * Valeur maximale. La valeur {@code NaN} indique qu'il
         * faut puiser le maximum dans les données de {@link RangeBars}.
         */
        private double maximum = Double.NaN;

        /**
         * Décalage intervenant dans la conversion de la position
         * de la visière en valeur entière. Le calcul se fait par
         * <code>int_x=(x-offset)*scale</code>.
         */
        private double offset;

        /**
         * Facteur d'échelle intervenant dans la conversion de la position de la visière
         * en valeur entière. Le calcul se fait par <code>int_x=x*scale+offset</code>.
         */
        private double scale;

        /**
         * Indique d'où vient le dernier ajustement
         * de la valeur: du model ou de la visière.
         */
        private boolean lastAdjustFromModel;

        /**
         * La valeur {@code true} indique que {@link #fireStateChanged}
         * ne doit pas prendre en compte le prochain événement. Ce champ est
         * utilisé lors des changements de la position de la visière.
         */
        private transient boolean ignoreEvent;

        /**
         * Construit un model avec par défaut une plage allant de 0 à 100. Les valeurs
         * de cette plage sont toujours indépendantes de celles de {@link RangeBars}.
         */
        public SwingModel() {
            revalidate();
        }

        //////////////////////////////////////////////////////////////////
        ////////                                                  ////////
        ////////        LogicalBoundedRangeModel interface        ////////
        ////////        (not used by this implementation)         ////////
        ////////                                                  ////////
        //////////////////////////////////////////////////////////////////
        /**
         * Spécifie les minimum et maximum des valeurs entières.
         * Une valeur {@link Double#NaN} signifie de prendre une
         * valeur par défaut.
         */
        public void setLogicalRange(final double minimum, final double maximum) {
            this.minimum = minimum;
            this.maximum = maximum;
            revalidate();
        }

        /**
         * Convertit une valeur entière en nombre réel.
         */
        public double toLogical(final int integer) {
            return (integer-offset)/scale;
        }

        /**
         * Convertit un nombre réel en valeur entière.
         */
        public int toInteger(final double logical) {
            return (int) Math.round(logical*scale + offset);
        }

        //////////////////////////////////////////////////////////////////
        ////////                                                  ////////
        ////////        Slider position  <-->  Model value        ////////
        ////////                                                  ////////
        //////////////////////////////////////////////////////////////////
        /**
         * Returns the slider value as an integer in the model range.
         */
        private int getSliderValue() {
            return (int)Math.round((horizontal ? slider.getMinX() : slider.getMinY()) * scale + offset);
        }

        /**
         * Returns the slider extent as an integer.
         */
        private int getSliderExtent() {
            return (int)Math.round((horizontal ? slider.getWidth() : slider.getHeight()) * scale);
        }

        /**
         * Met à jour les champs {@link #offset} et {@link #scale}. Les minimum
         * maximum ainsi que la valeur actuels du model seront réutilisés. C'est
         * de la responsabilité du programmeur de mettre à jour ces propriétés si
         * c'est nécessaire.
         */
        private void revalidate() {
            revalidate(super.getMinimum(), super.getMaximum());
        }

        /**
         * Update {@link #offset} and {@link #scale} according the supplied model's
         * {@code lower} and {@code upper} values. It is the caller's
         * responsability to ensure that {@code lower} and {@code upper} will map the
         * {@link BoundedRangeModel#getMinimum} and {@link BoundedRangeModel#getMaximum} values.
         *
         * @param lower The lower model value.
         * @param upper The upper model value.
         */
        private void revalidate(final int lower, final int upper) {
            double minimum = this.minimum;
            double maximum = this.maximum;
            try {
                if (Double.isNaN(minimum)) {
                    final Number min = ConverterRegistry.toNumber(RangeBars.this.getMinimum());
                    if (min != null) {
                        minimum = min.doubleValue();
                    }
                }
                if (Double.isNaN(maximum)) {
                    final Number max = ConverterRegistry.toNumber(RangeBars.this.getMaximum());
                    if (max != null) {
                        maximum = max.doubleValue();
                    }
                }
            } catch (ClassNotFoundException exception) {
                // The minimum or maximum value is not convertible to a number.
                // Ignore, since the code below will use a default scale.
            }
            if (!Double.isNaN(minimum) && !Double.isNaN(maximum)) {
                scale  = (upper-lower)/(maximum-minimum);
                offset = lower-minimum*scale;
            } else {
                scale  = 1;
                offset = 0;
            }
        }

        /**
         * Synchronize the slider position with this model. If the model has just been adjusted,
         * then the slider position is updated according. Otherwise, the model is updated
         * according the current slider position.
         */
        public void synchronize() {
            if (lastAdjustFromModel) {
                setSliderPosition();
            } else {
                final int value  = getSliderValue();
                final int extent = getSliderExtent();
                if (value!=super.getValue() || extent!=super.getExtent()) {
                    super.setRangeProperties(value, extent, super.getMinimum(), super.getMaximum(), false);
                }
            }
        }

        /**
         * Invoked by {@link RangeBars} when the slider position changed. This method adjust
         * this model according the current slider position and notifies all registered listeners.
         */
        protected void fireStateChanged(final boolean isAdjusting) {
            if (!ignoreEvent) {
                lastAdjustFromModel  = false;
                boolean adjustSlider = false;
                int lower  = super.getMinimum();
                int upper  = super.getMaximum();
                int value  = getSliderValue();
                int extent = getSliderExtent();
                if (value < lower) {
                    final int range = upper-lower;
                    if (extent > range) {
                        extent = range;
                    }
                    value = lower;
                    adjustSlider = true;
                } else if (value > upper-extent) {
                    final int range = upper-lower;
                    if (extent > range) {
                        extent = range;
                    }
                    value = upper-extent;
                    adjustSlider = true;
                }
                super.setRangeProperties(value, extent, lower, upper, isAdjusting);
                if (adjustSlider) {
                    setSliderPosition();
                }
            }
        }

        /**
         * Modifie la position de la visière en fonction des valeurs actuelles du modèle.
         */
        private void setSliderPosition() {
            final double min = (super.getValue()-offset)/scale;
            try {
                ignoreEvent = true;
                final double max = min + super.getExtent()/scale;
                if (horizontal) {
                    slider.setX(min, max);
                } else {
                    slider.setY(min, max);
                }
            } finally {
                ignoreEvent = false;
            }
            repaint();
        }

        /**
         * Modifie l'ensemble des paramètres d'un coups.
         */
        public void setRangeProperties(final int value, final int extent,
                                       final int lower, final int upper,
                                       final boolean isAdjusting)
        {
            revalidate(lower, upper);
            lastAdjustFromModel = true;
            super.setRangeProperties(value, extent, lower, upper, isAdjusting);
            setSliderPosition();
        }

        /**
         * Met à jour les champs internes de ce model et lance un
         * évènement prevenant que la position ou la largeur de la
         * visière a changée.
         */
        private void setRangeProperties(final int lower, final int upper, final boolean isAdjusting) {
            revalidate(lower, upper);
            if (lastAdjustFromModel) {
                super.setRangeProperties(super.getValue(), super.getExtent(), lower, upper, isAdjusting);
                setSliderPosition();
            } else {
                super.setRangeProperties(getSliderValue(), getSliderExtent(), lower, upper, isAdjusting);
            }
        }

        /**
         * Modifie la valeur minimale retournée par {@link #getValue}.
         * La valeur retournée par cette dernière sera modifiée pour
         * qu'elle corresponde à la position de la visière dans les
         * nouvelles limites.
         */
        public void setMinimum(final int minimum) {
            setRangeProperties(minimum, super.getMaximum(), false);
        }

        /**
         * Modifie la valeur maximale retournée par {@link #getValue}.
         * La valeur retournée par cette dernière sera modifiée pour
         * qu'elle corresponde à la position de la visière dans les
         * nouvelles limites.
         */
        public void setMaximum(final int maximum) {
            setRangeProperties(super.getMinimum(), maximum, false);
        }

        /**
         * Retourne la position de la visière.
         */
        public int getValue() {
            if (!lastAdjustFromModel) {
                super.setValue(getSliderValue());
            }
            return super.getValue();
        }

        /**
         * Modifie la position de la visière.
         */
        public void setValue(final int value) {
            lastAdjustFromModel = true;
            super.setValue(value);
            setSliderPosition();
        }

        /**
         * Retourne l'étendu de la visière.
         */
        public int getExtent() {
            if (!lastAdjustFromModel) {
                super.setExtent(getSliderExtent());
            }
            return super.getExtent();
        }

        /**
         * Modifie la largeur de la visière.
         */
        public void setExtent(final int extent) {
            lastAdjustFromModel = true;
            super.setExtent(extent);
            setSliderPosition();
        }
    }

    /**
     * Returns a control panel for this {@code RangeBars}. The control
     * panel may contains buttons, editors and spinners.   It make possible
     * for users to enter exact values in editor fields using the keyboard.
     * The returned control panel do not contains this {@code RangeBars}:
     * caller must layout both the control panel and this {@code RangeBars}
     * (possibly in different windows) if he want to see both of them.
     *
     * @param format  The format to use for formatting the selected value range,
     *                or {@code null} for a default format. If non-null,
     *                then this format is usually a
     *                {@link java.text.NumberFormat} or a
     *                {@link java.text.DateFormat} instance.
     * @param minLabel The label to put in front of the editor for
     *                 minimum value, or {@code null} if none.
     * @param maxLabel The label to put in front of the editor for
     *                 maximum value, or {@code null} if none.
     */
    private JComponent createControlPanel(Format format,
                                          final String minLabel,
                                          final String maxLabel)
    {
        ensureSliderCreated();
        if (format==null)   format = axis.getGraduation().getFormat();
        final JComponent   editor1 = slider.addEditor(format, horizontal ? SwingConstants.WEST : SwingConstants.NORTH, this);
        final JComponent   editor2 = slider.addEditor(format, horizontal ? SwingConstants.EAST : SwingConstants.SOUTH, this);
        final JComponent     panel = new JPanel(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        /*
         * If the caller supplied labels, add
         * labels first. Then add the editors.
         */
        c.gridx=0;
        if (minLabel!=null || maxLabel!=null) {
            c.anchor = c.EAST;
            c.gridy=0; panel.add(new JLabel(horizontal ? minLabel : maxLabel), c);
            c.gridy=1; panel.add(new JLabel(horizontal ? maxLabel : minLabel), c);
            c.gridx=1;
            c.insets.left=3;
            c.anchor = c.CENTER;
        }
        c.weightx=1; c.fill=c.HORIZONTAL;
        c.gridy=0; panel.add(editor1, c);
        c.gridy=1; panel.add(editor2, c);
        /*
         * Adjust focus order.
         * TODO: this code use deprecated API.
         */
        editor1.setNextFocusableComponent(editor2);
        editor2.setNextFocusableComponent(this   );
        this   .setNextFocusableComponent(editor1);
        this   .requestFocus();
        panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEtchedBorder(),
                        BorderFactory.createEmptyBorder(3,3,3,3)));
        final Dimension size = panel.getPreferredSize();
        size.width = 100;
        panel.setPreferredSize(size);
        panel.setMinimumSize  (size);
        return panel;
    }

    /**
     * Returns a new panel with that contains this {@code RangeBars} and
     * control widgets. Control widgets may include buttons, editors, spinners
     * and scroll bar. It make possible for users to enter exact values in
     * editor fields using the keyboard.
     *
     * @param format  The format to use for formatting the selected value range,
     *                or {@code null} for a default format. If non-null,
     *                then this format is usually a
     *                {@link java.text.NumberFormat} or a
     *                {@link java.text.DateFormat} instance.
     * @param minLabel The label to put in front of the editor for
     *                 minimum value, or {@code null} if none.
     * @param maxLabel The label to put in front of the editor for
     *                 maximum value, or {@code null} if none.
     */
    public JComponent createCombinedPanel(final Format format,
                                          final String minLabel,
                                          final String maxLabel)
    {
        final JComponent     panel = new JPanel(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.gridx=0; c.weightx=1;
        c.gridy=0; c.weighty=1;
        c.fill = c.BOTH;
        panel.add(horizontal ? this : createScrollPane(), c);
        if (horizontal) {
            c.gridx  =1;
            c.weightx=0;
            c.insets.right = 6;
        } else {
            c.gridy  =1;
            c.weighty=0;
            c.insets.top = 6;
        }
        c.fill = c.HORIZONTAL;
        panel.add(createControlPanel(format, minLabel, maxLabel), c);
        return panel;
    }

    /**
     * Fait apparaître dans une fenêtre quelques histogrammes
     * calculés au hasard. Cette méthode sert à vérifier le
     * bon fonctionnement de la classe {@code RangeBars}.
     */
    public static void main(final String[] args) {
        int orientation = HORIZONTAL;
        if (args.length != 0) {
            final String arg = args[0];
            if (arg.equalsIgnoreCase("horizontal")) {
                orientation = HORIZONTAL;
            } else if (arg.equalsIgnoreCase("vertical")) {
                orientation = VERTICAL;
            } else if (arg.equalsIgnoreCase("vertical2")) {
                orientation = VERTICAL_EXCEPT_LABELS;
            } else {
                System.err.print("Unknow argument: ");
                System.err.println(arg);
                return;
            }
        }
        final JFrame frame = new JFrame("RangeBars");
        final RangeBars ranges = new RangeBars((Unit)null, orientation);
        for (int série=1; série<=4; série++) {
            final String clé="Série #"+série;
            for (int i=0; i<100; i++) {
                final double x = 1000*Math.random();
                final double w =   30*Math.random();
                ranges.addRange(clé, new Double(x), new Double(x+w));
            }
        }
        ranges.setSelectedRange(12, 38);
        ranges.setRangeAdjustable(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(ranges.createCombinedPanel(null, "Min:", "Max:"));
        if (ranges.horizontal) {
            frame.setSize(500, 150);
        } else {
            frame.pack(); // For an unknow reason, application freeze here for horizontal bars.
        }
        frame.setVisible(true);
    }
}
