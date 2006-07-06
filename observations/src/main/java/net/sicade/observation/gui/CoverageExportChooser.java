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
package net.sicade.observation.gui;

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

// Entr�s/sorties
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
import org.geotools.referencing.crs.DefaultCompoundCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.Operations;
import org.geotools.image.io.IIOListeners;
import org.geotools.gui.swing.ProgressWindow;
import org.geotools.gui.swing.ExceptionMonitor;
import org.geotools.coverage.io.MetadataBuilder;
import org.geotools.util.ProgressListener;
import org.geotools.resources.Utilities;
import org.geotools.resources.SwingUtilities;
import org.geotools.resources.CRSUtilities;

// Sicade
import net.sicade.observation.coverage.CoverageReference;
import net.sicade.resources.seagis.Resources;
import net.sicade.resources.seagis.ResourceKeys;


/**
 * Bo�te de dialogue invitant l'utilisateur � s�lectionner un r�pertoire
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
     * Objet � utiliser pour s�lectionner un r�pertoire de destination.
     */
    private final JFileChooser chooser;

    /**
     * Ensemble des images � �crire. L'ordre des �l�ments doit �tre pr�serv�s.
     */
    private final Set<CoverageReference> entries = new LinkedHashSet<CoverageReference>(256);

    /**
     * Etiquette indiquant le nombre d'images � exporter.
     */
    private final JLabel count = new JLabel();

    /**
     * Resources pour la construction des �tiquettes.
     */
    private final Resources resources = Resources.getResources(getLocale());

    /**
     * Construit une bo�te de dialogue.
     *
     * @param directory R�pertoire de destination par d�faut, ou {@code null}
     *                  pour utiliser le r�pertoire du compte de l'utilisateur.
     */
    public CoverageExportChooser(final File directory) {
        super(new GridBagLayout());
        count.setOpaque(true);
        count.setBackground(Color.BLACK);
        count.setForeground(Color.YELLOW);
        count.setHorizontalAlignment(SwingConstants.CENTER);
        ///
        /// Configure le paneau servant � choisir un r�pertoire.
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
     * Met � jour l'�tiquette qui indique le nombre d'images � exporter.
     */
    private void updateCount() {
        count.setText(resources.getString(ResourceKeys.COVERAGES_TO_EXPORT_COUNT_$1,
                                          new Integer(entries.size())));
    }

    /**
     * Ajoute les entr�es sp�cifi�es � la liste des images � �crire. Les
     * images seront �crites dans l'ordre qu'elles apparaissent dans le
     * tableau {@code entries}. Toutefois, les doublons seront ignor�s.
     */
    public synchronized void addEntries(final CoverageReference[] entries)  {
        for (int i=0; i<entries.length; i++) {
            this.entries.add(entries[i]);
        }
        updateCount();
    }

    /**
     * Retire les entr�es sp�cifi�es de la liste des images � �crire.
     */
    public synchronized void removeEntries(final CoverageReference[] entries) {
        for (int i=entries.length; --i>=0;) {
            this.entries.remove(entries[i]);
        }
        updateCount();
    }

    /**
     * Retire toutes les entr�es de la liste des images � �crire.
     */
    public synchronized void removeAllEntries() {
        entries.clear();
        updateCount();
    }

    /**
     * Retourne les entr�es des images qui seront � �crire.
     */
    public synchronized CoverageReference[] getEntries() {
        return entries.toArray(new CoverageReference[entries.size()]);
    }

    /**
     * Retourne le r�pertoire dans lequel �crire les images. Ce r�pertoire a
     * �t� sp�cifi�e lors de la construction de cet objet, mais peut avoir �t�
     * modifi� par l'utilisateur.
     */
    public File getDestinationDirectory() {
        return chooser.getSelectedFile();
    }

    /**
     * Fait appara�tre la bo�te de dialogue. Si l'utilisateur n'a pas annul�
     * l'op�ration en cours de route, l'exportation des images sera lanc�e
     * dans un thread en arri�re-plan. Cette m�thode peut donc retourner pendant
     * que les exportations sont en cours. Les progr�s seront affich�es dans
     * une fen�tre.
     *
     * @param  owner Composante parente dans laquelle faire appara�tre la
     *         bo�te de dialogue, ou {@code null} s'il n'y en a pas.
     * @param  threadGroup Groupe de threads dans lequel placer celui qu'on
     *         va lancer.
     * @return {@code true} si l'utilisateur a lanc� les exportations,
     *         ou {@code false} s'il a annul� l'op�ration.
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
     * Classe ayant la charge d'exporter les images en arri�re plan.  Le constructeur de cette
     * classe fait une copie de tous les param�tres pertinents de {@link CoverageExportChooser},
     * tels qu'ils �taient au moment de la construction. Par la suite, aucune r�f�rence vers
     * {@link CoverageExportChooser} n'est conserv�e.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    private static final class Worker implements Runnable, IIOReadWarningListener {
        /**
         * Fen�tre dans laquelle �crire les progr�s de l'op�ration.
         * Cette fen�tre ne sera cr��e que la premi�re fois o� elle
         * sera n�cessaire.
         */
        private ProgressListener progress;

        /**
         * Encodeur � utiliser pour �crire les images. Cet encodeur
         * ne sera cr�� que lorsque les �critures d'images d�marreront.
         */
        private ImageWriter writer;

        /**
         * Entr� en cours de lecture, ou {@code null}
         * s'il n'y en a pas encore.
         */
        private CoverageReference current;

        /**
         * Liste des images � �crire.
         */
        private final CoverageReference[] entries;

        /**
         * R�pertoire de destination dans lequel
         * seront �crites les images.
         */
        private final File directory;

        /**
         * Extension des fichiers d'images. Cette extension remplacera l'extension des
         * fichiers d'images sources. La cha�ne de caract�res {@code extension}
         * ne doit pas commencer par un point. Ce champ peut �tre {@code null}
         * s'il n'y a pas d'extension connue pour le type de fichier � �crire.
         */
        private final String extension;

        /**
         * Objet qui avait la charge de filtrer les fichiers � afficher dans la
         * bo�te de dialogue. Cet objet conna�t le format choisit par l'utilisateur
         * et est capable de construire l'encodeur {@link ImageWriter} appropri�.
         */
        private final ImageFileFilter filter;

        /**
         * Buffer temporaire. Ce buffer est utilis� pour construire
         * chacun des noms de fichier de destination des images.
         */
        private final StringBuilder buffer = new StringBuilder();

        /**
         * Objet {@link MetadataBuilder} � utiliser pour �crire les propri�t�s d'un {@link GridCoverage2D}.
         */
        private transient MetadataBuilder propertyParser;

        /**
         * Construit un objet qui proc�dera aux �critures des images en arri�re plan.
         * Ce constructeur fera une copie des param�tres de la bo�te de dialogue
         * {@link CoverageExportChooser} sp�cifi�e.
         *
         * @param chooser Bo�te de dialogue qui demandait � l'utilisateur
         *        de choisir un r�pertoire de destination ainsi qu'un format.
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
         * Retourne le nom et le chemin du fichier de destination pour l'image sp�cifi�e.
         */
        private File getDestinationFile(final int index) {
            return getDestinationFile(index, extension);
        }

        /**
         * Retourne le nom et le chemin du fichier de destination pour l'image sp�cifi�e.
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
         * V�rifie si les images peuvent �tre �crites dans le r�pertoire choisi. Cette m�thode
         * v�rifie d'abord si le r�pertoire est valide. Elle v�rifie ensuite si le r�pertoire
         * contient d�j� des images qui risquent d'�tre �cras�es. Si c'est le cas, alors cette
         * m�thode fait appara�tre une bo�te de dialogue qui demande � l'utilisateur de confirmer
         * les �crasements. Cette m�thode devrait toujours �tre appel�e avant de lancer les exportations
         * des fichiers.
         *
         * @param  owner Composante parente dans laquelle faire appara�tre les �ventuelles bo�tes de dialogue.
         * @return {@code true} si on peut proc�der aux �critures des images, ou {@code false} si
         *         l'utilisateur a demand� � arr�ter l'op�ration.
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
         * D�marre les exportations d'images. Cette m�thode fait appara�tre une fen�tre
         * dans laquelle seront affich�es les progr�s de l'op�ration. Elle app�le ensuite
         * {@link #run} dans un thread s�par�, afin de faire les �critures en arri�re plan.
         * <strong>Plus aucune autre m�thode de {@code Worker} ne devrait �tre appel�e
         * apr�s {@code start}.</strong>
         *
         * @param  threadGroup Groupe de threads dans lequel placer celui qu'on va lancer.
         * @param  owner Composante parente dans laquelle faire appara�tre la fen�tre des progr�s.
         * @throws IOException si une erreur a emp�ch� le d�marrage des exportations.
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
         * Proc�de aux exportations d'images. Si une erreur survient en cours de route,
         * un avertissement sera �crit dans la fen�tre des progr�s. N'appelez pas cette
         * m�thode directement. Appelez plut�t {@link #start}, qui se chargera d'appeller
         * {@code run()} dans un thread en arri�re-plan.
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
         * M�thode appel�e automatiquement lorsqu'un avertissement
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
         * Ecrit les propri�t�s de l'image sp�cifi�e. L'impl�mentation par d�faut �crit les coordonn�es
         * g�ographiques des quatres coins de l'image, sa taille, nombre de bandes, etc.
         *
         * @param coverage L'image pour laquelle �crire les propri�t�s.
         * @param file Le fichier de destination. Ca sera g�n�ralement un fichier avec l'extension
         *             <code>".txt"</code>.
         */
        protected void writeProperties(final GridCoverage2D coverage, final File file) throws IOException {
            if (propertyParser == null) {
                propertyParser = new MetadataBuilder();
                propertyParser.setFormatPattern(Date.class, "yyyy/MM/dd HH:mm zz");
                propertyParser.setFormatPattern(Number.class, "#0.######");
                propertyParser.addAlias(MetadataBuilder.Z_MINIMUM,    "Date de d�but");
                propertyParser.addAlias(MetadataBuilder.Z_MAXIMUM,    "Date de fin");
                propertyParser.addAlias(MetadataBuilder.PROJECTION,   "Projection");
                propertyParser.addAlias(MetadataBuilder.ELLIPSOID,    "Ellipso�de");
                propertyParser.addAlias(MetadataBuilder.Y_MAXIMUM,    "Limite Nord");
                propertyParser.addAlias(MetadataBuilder.Y_MINIMUM,    "Limite Sud");
                propertyParser.addAlias(MetadataBuilder.X_MAXIMUM,    "Limite Est");
                propertyParser.addAlias(MetadataBuilder.X_MINIMUM,    "Limite Ouest");
                propertyParser.addAlias(MetadataBuilder.Y_RESOLUTION, "R�solution en latitude");
                propertyParser.addAlias(MetadataBuilder.X_RESOLUTION, "R�solution en longitude");
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
