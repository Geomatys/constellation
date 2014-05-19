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

// Interface utilisateur
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;
import javax.swing.event.EventListenerList;

// Formattage
import java.util.Date;
import java.util.Locale;

// Entrés/sorties
import java.io.File;
import java.io.Writer;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.IIOImage;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.ImageWriteParam;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.event.IIOReadWarningListener;

// Collections
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

// OpenGIS
import org.opengis.referencing.crs.CoordinateReferenceSystem;

// Geotools
import org.geotoolkit.referencing.crs.DefaultCompoundCRS;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.processing.Operations;
import org.geotoolkit.image.io.IIOListeners;
import org.geotoolkit.gui.swing.ProgressWindow;
import org.geotoolkit.gui.swing.ExceptionMonitor;
import org.geotools.coverage.io.MetadataBuilder;
import org.geotools.util.ProgressListener;
import org.geotoolkit.util.Utilities;
import org.geotools.resources.SwingUtilities;
import org.geotoolkit.internal.referencing.CRSUtilities;

// Constellation
import org.constellation.coverage.catalog.CoverageReference;
import org.constellation.resources.i18n.Resources;
import org.constellation.resources.i18n.ResourceKeys;


/**
 * Boîte de dialogue invitant l'utilisateur à sélectionner un répertoire
 * de destination et un format d'image.
 *
 * <p>&nbsp;</p>
 * <p align="center"><img src="doc-files/CoverageExportChooser.png"></p>
 * <p>&nbsp;</p>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class CoverageExportChooser extends JPanel {
    /**
     * Objet à utiliser pour sélectionner un répertoire de destination.
     */
    private final JFileChooser chooser;

    /**
     * Ensemble des images à écrire. L'ordre des éléments doit être préservés.
     */
    private final Set<CoverageReference> entries = new LinkedHashSet<CoverageReference>(256);

    /**
     * Etiquette indiquant le nombre d'images à exporter.
     */
    private final JLabel count = new JLabel();

    /**
     * Resources pour la construction des étiquettes.
     */
    private final Resources resources = Resources.getResources(getLocale());

    /**
     * Construit une boîte de dialogue.
     *
     * @param directory Répertoire de destination par défaut, ou {@code null}
     *                  pour utiliser le répertoire du compte de l'utilisateur.
     */
    public CoverageExportChooser(final File directory) {
        super(new GridBagLayout());
        count.setOpaque(true);
        count.setBackground(Color.BLACK);
        count.setForeground(Color.YELLOW);
        count.setHorizontalAlignment(SwingConstants.CENTER);
        ///
        /// Configure le paneau servant à choisir un répertoire.
        ///
        chooser = new JFileChooser(directory);
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setDialogTitle(resources.getString(ResourceKeys.OUT_DIRECTORY));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setControlButtonsAreShown(false);
        chooser.setAcceptAllFileFilterUsed(false);
        ///
        /// Ajoute les filtres de fichiers
        ///
        final ImageFileFilter[] fileFilters = ImageFileFilter.getWriterFilters(null);
        for (int i=0; i<fileFilters.length; i++) {
            chooser.addChoosableFileFilter(fileFilters[i]);
        }
        ///
        /// Construit le paneau d'options
        ///
        final JPanel options = new JPanel(new GridBagLayout());
        final GridBagConstraints c=new GridBagConstraints();
        options.setBorder(BorderFactory.createCompoundBorder(
                          BorderFactory.createTitledBorder(resources.getString(ResourceKeys.OPTIONS)),
                          BorderFactory.createEmptyBorder(/*top*/6,/*left*/9,/*bottom*/6,/*right*/9)));
        c.gridx=0; c.fill=c.BOTH; c.insets.right=6;
        c.gridy=0; options.add(new JLabel(resources.getString(ResourceKeys.NOT_AVAILABLE)), c);
        ///
        /// Place les composantes
        ///
        c.gridx=0; c.weightx=1;
        c.gridy=0;              c.insets.top= 6; add(count,   c);
        c.gridy=1; c.weighty=1; c.insets.top=15; add(chooser, c);
        c.gridy=2; c.weighty=0; c.insets.top=12; add(options, c);
        updateCount();
    }

    /**
     * Met à jour l'étiquette qui indique le nombre d'images à exporter.
     */
    private void updateCount() {
        count.setText(resources.getString(ResourceKeys.COVERAGES_TO_EXPORT_COUNT_$1,
                                          new Integer(entries.size())));
    }

    /**
     * Ajoute les entrées spécifiées à la liste des images à écrire. Les
     * images seront écrites dans l'ordre qu'elles apparaissent dans le
     * tableau {@code entries}. Toutefois, les doublons seront ignorés.
     */
    public synchronized void addEntries(final CoverageReference[] entries)  {
        for (int i=0; i<entries.length; i++) {
            this.entries.add(entries[i]);
        }
        updateCount();
    }

    /**
     * Retire les entrées spécifiées de la liste des images à écrire.
     */
    public synchronized void removeEntries(final CoverageReference[] entries) {
        for (int i=entries.length; --i>=0;) {
            this.entries.remove(entries[i]);
        }
        updateCount();
    }

    /**
     * Retire toutes les entrées de la liste des images à écrire.
     */
    public synchronized void removeAllEntries() {
        entries.clear();
        updateCount();
    }

    /**
     * Retourne les entrées des images qui seront à écrire.
     */
    public synchronized CoverageReference[] getEntries() {
        return entries.toArray(new CoverageReference[entries.size()]);
    }

    /**
     * Retourne le répertoire dans lequel écrire les images. Ce répertoire a
     * été spécifiée lors de la construction de cet objet, mais peut avoir été
     * modifié par l'utilisateur.
     */
    public File getDestinationDirectory() {
        return chooser.getSelectedFile();
    }

    /**
     * Fait apparaître la boîte de dialogue. Si l'utilisateur n'a pas annulé
     * l'opération en cours de route, l'exportation des images sera lancée
     * dans un thread en arrière-plan. Cette méthode peut donc retourner pendant
     * que les exportations sont en cours. Les progrès seront affichées dans
     * une fenêtre.
     *
     * @param  owner Composante parente dans laquelle faire apparaître la
     *         boîte de dialogue, ou {@code null} s'il n'y en a pas.
     * @param  threadGroup Groupe de threads dans lequel placer celui qu'on
     *         va lancer.
     * @return {@code true} si l'utilisateur a lancé les exportations,
     *         ou {@code false} s'il a annulé l'opération.
     */
    public boolean showDialogAndStart(final Component owner, final ThreadGroup threadGroup) {
        while (SwingUtilities.showOptionDialog(owner, this, chooser.getDialogTitle())) {
            final Worker worker=new Worker(this);
            if (worker.getUserConfirmation(owner)) {
                try {
                    worker.start(threadGroup, owner);
                    return true;
                } catch (IOException exception) {
                    ExceptionMonitor.show(owner, exception);
                }
            }
        }
        return false;
    }

    /**
     * Classe ayant la charge d'exporter les images en arrière plan.  Le constructeur de cette
     * classe fait une copie de tous les paramètres pertinents de {@link CoverageExportChooser},
     * tels qu'ils étaient au moment de la construction. Par la suite, aucune référence vers
     * {@link CoverageExportChooser} n'est conservée.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    private static final class Worker implements Runnable, IIOReadWarningListener {
        /**
         * Fenêtre dans laquelle écrire les progrès de l'opération.
         * Cette fenêtre ne sera créée que la première fois où elle
         * sera nécessaire.
         */
        private ProgressListener progress;

        /**
         * Encodeur à utiliser pour écrire les images. Cet encodeur
         * ne sera créé que lorsque les écritures d'images démarreront.
         */
        private ImageWriter writer;

        /**
         * Entré en cours de lecture, ou {@code null}
         * s'il n'y en a pas encore.
         */
        private CoverageReference current;

        /**
         * Liste des images à écrire.
         */
        private final CoverageReference[] entries;

        /**
         * Répertoire de destination dans lequel
         * seront écrites les images.
         */
        private final File directory;

        /**
         * Extension des fichiers d'images. Cette extension remplacera l'extension des
         * fichiers d'images sources. La chaîne de caractères {@code extension}
         * ne doit pas commencer par un point. Ce champ peut être {@code null}
         * s'il n'y a pas d'extension connue pour le type de fichier à écrire.
         */
        private final String extension;

        /**
         * Objet qui avait la charge de filtrer les fichiers à afficher dans la
         * boîte de dialogue. Cet objet connaît le format choisit par l'utilisateur
         * et est capable de construire l'encodeur {@link ImageWriter} approprié.
         */
        private final ImageFileFilter filter;

        /**
         * Buffer temporaire. Ce buffer est utilisé pour construire
         * chacun des noms de fichier de destination des images.
         */
        private final StringBuilder buffer = new StringBuilder();

        /**
         * Objet {@link MetadataBuilder} à utiliser pour écrire les propriétés d'un {@link GridCoverage2D}.
         */
        private transient MetadataBuilder propertyParser;

        /**
         * Construit un objet qui procèdera aux écritures des images en arrière plan.
         * Ce constructeur fera une copie des paramètres de la boîte de dialogue
         * {@link CoverageExportChooser} spécifiée.
         *
         * @param chooser Boîte de dialogue qui demandait à l'utilisateur
         *        de choisir un répertoire de destination ainsi qu'un format.
         */
        public Worker(final CoverageExportChooser chooser) {
            synchronized (chooser) {
                this.filter    = (ImageFileFilter) chooser.chooser.getFileFilter();
                this.entries   = chooser.getEntries();
                this.directory = chooser.getDestinationDirectory();
                this.extension =  filter.getExtension();
            }
        }

        /**
         * Retourne le nom et le chemin du fichier de destination pour l'image spécifiée.
         */
        private File getDestinationFile(final int index) {
            return getDestinationFile(index, extension);
        }

        /**
         * Retourne le nom et le chemin du fichier de destination pour l'image spécifiée.
         */
        private File getDestinationFile(final int index, final String extension) {
            File file = entries[index].getFile();
            if (file == null) {
                file = new File(entries[index].getURL().getPath());
            }
            final String filename = file.getName();
            buffer.setLength(0);
            buffer.append(filename);
            final int extPos = filename.lastIndexOf('.');
            if (extPos >= 0) {
                buffer.setLength(extPos);
            }
            if (extension!=null && extension.length() != 0) {
                buffer.append('.');
                buffer.append(extension);
            }
            return new File(directory, buffer.toString());
        }

        /**
         * Vérifie si les images peuvent être écrites dans le répertoire choisi. Cette méthode
         * vérifie d'abord si le répertoire est valide. Elle vérifie ensuite si le répertoire
         * contient déjà des images qui risquent d'être écrasées. Si c'est le cas, alors cette
         * méthode fait apparaître une boîte de dialogue qui demande à l'utilisateur de confirmer
         * les écrasements. Cette méthode devrait toujours être appelée avant de lancer les exportations
         * des fichiers.
         *
         * @param  owner Composante parente dans laquelle faire apparaître les éventuelles boîtes de dialogue.
         * @return {@code true} si on peut procéder aux écritures des images, ou {@code false} si
         *         l'utilisateur a demandé à arrêter l'opération.
         */
        public boolean getUserConfirmation(final Component owner) {
            final Resources resources = Resources.getResources(owner.getLocale());
            if (directory==null || !directory.isDirectory()) {
                SwingUtilities.showMessageDialog(owner, resources.getString(ResourceKeys.ERROR_BAD_DIRECTORY),
                                 resources.getString(ResourceKeys.ERROR_BAD_ENTRY), JOptionPane.ERROR_MESSAGE);
                return false;
            }
            int existing = 0;
            for (int i=0; i<entries.length; i++) {
                if (getDestinationFile(i).exists()) {
                    existing++;
                }
            }
            if (existing != 0) {
                if (!SwingUtilities.showConfirmDialog(owner, resources.getString(ResourceKeys.CONFIRM_OVERWRITE_ALL_$2, new Integer(existing),
                                    new Integer(entries.length)), resources.getString(ResourceKeys.CONFIRM_OVERWRITE), JOptionPane.WARNING_MESSAGE))
                {
                    return false;
                }
            }
            return true;
        }

        /**
         * Démarre les exportations d'images. Cette méthode fait apparaître une fenêtre
         * dans laquelle seront affichées les progrès de l'opération. Elle appèle ensuite
         * {@link #run} dans un thread séparé, afin de faire les écritures en arrière plan.
         * <strong>Plus aucune autre méthode de {@code Worker} ne devrait être appelée
         * après {@code start}.</strong>
         *
         * @param  threadGroup Groupe de threads dans lequel placer celui qu'on va lancer.
         * @param  owner Composante parente dans laquelle faire apparaître la fenêtre des progrès.
         * @throws IOException si une erreur a empêché le démarrage des exportations.
         */
        public void start(final ThreadGroup threadGroup, final Component owner) throws IOException {
            final Resources resources = Resources.getResources(owner.getLocale());
            writer   = filter.getImageWriter();
            progress = new ProgressWindow(owner);
            final Thread thread=new Thread(threadGroup, this, resources.getString(ResourceKeys.EXPORT));
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }

        /**
         * Procède aux exportations d'images. Si une erreur survient en cours de route,
         * un avertissement sera écrit dans la fenêtre des progrès. N'appelez pas cette
         * méthode directement. Appelez plutôt {@link #start}, qui se chargera d'appeller
         * {@code run()} dans un thread en arrière-plan.
         */
        public void run() {
            final IIOListeners listeners = new IIOListeners();
            listeners.addIIOReadWarningListener(this);
            progress.started();
            for (int i=0; i<entries.length; i++) {
                final CoverageReference entry = entries[i];
                String name = "";
                try {
                    name = entry.getName();
                    progress.setDescription(Resources.format(ResourceKeys.EXPORTING_$1, name));
                    progress.progress((i*100f)/entries.length);
                    GridCoverage2D image = entry.getCoverage(listeners).geophysics(false);
                    CoordinateReferenceSystem sourceCRS = image.getCoordinateReferenceSystem();
                    CoordinateReferenceSystem targetCRS = DefaultGeographicCRS.WGS84;
                    final int sourceDim = sourceCRS.getCoordinateSystem().getDimension();
                    final int targetDim = targetCRS.getCoordinateSystem().getDimension();
                    if (sourceDim > targetDim) {
                        final CoordinateReferenceSystem tailCRS = CRSUtilities.getSubCRS(sourceCRS, targetDim, sourceDim);
                        if (tailCRS != null) {
                            targetCRS = new DefaultCompoundCRS(targetCRS.getName().getCode(), targetCRS, tailCRS);
                        }
                    }
                    image = (GridCoverage2D) Operations.DEFAULT.resample(image, targetCRS);
                    final ImageOutputStream output = ImageIO.createImageOutputStream(getDestinationFile(i));
                    writer.setOutput(output);
                    writer.write(image.getRenderedImage());
                    output.close();
                    writeProperties(image, getDestinationFile(i, "txt"));
                } catch (Exception exception) {
                    String message = exception.getLocalizedMessage();
                    if (message == null) {
                        message = Utilities.getShortClassName(exception);
                    }
                    progress.warningOccurred(name, null, message);
                }
                writer.reset();
            }
            progress.complete();
            writer.dispose();
        }
        
        /**
         * Méthode appelée automatiquement lorsqu'un avertissement
         * est survenu pendant la lecture d'une image.
         */
        public void warningOccurred(final ImageReader source, final String warning) {
            String name = null;
            final ProgressListener progress = this.progress;
            if (progress != null) {
                final CoverageReference entry = current;
                if (entry != null) {
                    name = entry.getName();
                }
                progress.warningOccurred(name, null, warning);
            }
        }

        /**
         * Ecrit les propriétés de l'image spécifiée. L'implémentation par défaut écrit les coordonnées
         * géographiques des quatres coins de l'image, sa taille, nombre de bandes, etc.
         *
         * @param coverage L'image pour laquelle écrire les propriétés.
         * @param file Le fichier de destination. Ca sera généralement un fichier avec l'extension
         *             <code>".txt"</code>.
         */
        protected void writeProperties(final GridCoverage2D coverage, final File file) throws IOException {
            if (propertyParser == null) {
                propertyParser = new MetadataBuilder();
                propertyParser.setFormatPattern(Date.class, "yyyy/MM/dd HH:mm zz");
                propertyParser.setFormatPattern(Number.class, "#0.######");
                propertyParser.addAlias(MetadataBuilder.Z_MINIMUM,    "Date de début");
                propertyParser.addAlias(MetadataBuilder.Z_MAXIMUM,    "Date de fin");
                propertyParser.addAlias(MetadataBuilder.PROJECTION,   "Projection");
                propertyParser.addAlias(MetadataBuilder.ELLIPSOID,    "Ellipsoïde");
                propertyParser.addAlias(MetadataBuilder.Y_MAXIMUM,    "Limite Nord");
                propertyParser.addAlias(MetadataBuilder.Y_MINIMUM,    "Limite Sud");
                propertyParser.addAlias(MetadataBuilder.X_MAXIMUM,    "Limite Est");
                propertyParser.addAlias(MetadataBuilder.X_MINIMUM,    "Limite Ouest");
                propertyParser.addAlias(MetadataBuilder.Y_RESOLUTION, "Résolution en latitude");
                propertyParser.addAlias(MetadataBuilder.X_RESOLUTION, "Résolution en longitude");
                propertyParser.addAlias(MetadataBuilder.WIDTH,        "Largeur (en pixels)");
                propertyParser.addAlias(MetadataBuilder.HEIGHT,       "Hauteur (en pixels)");
            }
            final Locale locale        = Locale.getDefault();
            final String lineSeparator = System.getProperty("line.separator", "\n");
            final Writer out           = new BufferedWriter(new FileWriter(file));
            out.write('#'); out.write(lineSeparator);
            out.write("# Description du format de l'image \"");
            out.write(coverage.getName().toString(locale));
            out.write('"');
            out.write(lineSeparator);
            out.write('#'); out.write(lineSeparator);
            out.write(lineSeparator);
            propertyParser.clear();
            propertyParser.add(coverage);
            propertyParser.listMetadata(out);
            propertyParser.clear();
            out.close();
        }
    }
}
