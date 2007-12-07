/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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
 */
package net.seagis.console;

import java.util.Date;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.JTree;
import javax.swing.JTable;
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.tree.TreeModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.geotools.resources.Arguments;
import org.geotools.resources.Classes;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gui.swing.tree.Trees;
import org.geotools.gui.swing.tree.DefaultMutableTreeNode;
import org.geotools.util.logging.Logging;

import net.seagis.catalog.Database;
import net.seagis.catalog.CatalogException;
import net.seagis.coverage.catalog.CoverageReference;
import net.seagis.coverage.catalog.Format;
import net.seagis.coverage.catalog.FormatTable;
import net.seagis.coverage.catalog.GridCoverageTable;
import net.seagis.coverage.catalog.Layer;
import net.seagis.coverage.catalog.LayerTable;
import net.seagis.coverage.catalog.LayerTree;
import net.seagis.coverage.catalog.TreeDepth;
import net.seagis.coverage.model.Model;
import net.seagis.coverage.model.LinearModel;
import net.seagis.widget.CoverageTableModel;
import net.seagis.resources.i18n.Resources;
import net.seagis.resources.i18n.ResourceKeys;


/**
 * Fournit des outils de lignes de commande pour l'administration de la base de données d'images.
 * Cette classe peut être exécutée à partir de la ligne de commande:
 * <p>
 * <blockquote><pre>
 * java net.seagis.observation.coverage.Console <var>options</var>
 * </pre></blockquote>
 * <p>
 * Lorsque cette classe est exécutée avec l'argument {@code -config}, elle fait apparaître une boite
 * de dialogue permettant de configurer les requêtes SQL utilisées par la base de données. Les requêtes
 * modifiées seront sauvegardées dans un fichier de configuration.
 * <p>
 * Cette méthode peut aussi être utilisée pour afficher des informations vers le
 * {@linkplain System#out périphérique de sortie standard}. Les arguments valides sont:
 * <p>
 * <blockquote><pre>
 *  <b>-print thematics</b> <i></i>  Affiche l'arborescence des thèmes
 *  <b>-print layers</b> <i></i>     Affiche l'arborescence des couches
 *  <b>-print series</b> <i></i>     Affiche l'arborescence des séries
 *  <b>-print formats</b> <i></i>    Affiche l'arborescence des formats
 *  <b>-print categories</b> <i></i> Affiche l'arborescence des catégories
 *  <b>-print decoders</b> <i></i>   Affiche l'arborescence des catégories à partir des formats
 *  <b>-browse</b> <i></i>           Affiche le contenu de toute la base de données (interface graphique)
 *  <b>-verify</b> <i></i>           Vérifie l'existence d'un fichier pour chaque image de chaque couche
 *  <b>-config</b> <i></i>           Configure la base de données (interface graphique)
 *  <b>-locale</b> <i>name</i>       Langue et conventions d'affichage (exemple: "fr_CA")
 *  <b>-encoding</b> <i>name</i>     Page de code pour les sorties     (exemple: "cp850")
 *  <b>-Xout</b> <i>filename</i>     Fichier de destination (le périphérique standard par défaut)
 * </pre></blockquote>
 * <p>
 * L'argument {@code -encoding} est surtout utile lorsque ce programme est lancé à partir de la ligne
 * de commande MS-DOS: ce dernier n'utilise pas la même page de code que le reste du système Windows.
 * Il est alors nécessaire de préciser la page de code (souvent 850 ou 437) si on veut obtenir un
 * affichage correct des caractères étendus. La page de code en cours peut être obtenu en tappant
 * {@code chcp} sur la ligne de commande.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@Deprecated
public final class Console extends Arguments {
    /**
     * Connexion vers la base de données. Cette connexion ne sera établie que la première
     * fois où elle sera nécessaire.
     */
    private transient Database database;

    /**
     * Construit une console.
     *
     * @param args Arguments transmis sur la ligne de commande.
     */
    protected Console(final String[] args) {
        super(args);
    }

    /**
     * Retourne la connexion vers la base de données.
     *
     * @throws SQLException si la connexion n'a pas pu être établie.
     */
    private Database getDatabase() throws SQLException, IOException {
        if (database == null) {
            database = new Database();
        }
        return database;
    }

    /**
     * Affiche l'écran d'aide.
     */
    private void help() {
        out.println();
        out.println("Outils de ligne de commande pour la base de données d'images\n"+
                    "1999-2005, Institut de Recherche pour le Développement\n"+
                    "\n"+
                    "Options disponibles:\n"+
                    "  -print thematics    Affiche l'arborescence des thèmes\n"+
                    "  -print layers       Affiche l'arborescence des couches\n"+
                    "  -print series       Affiche l'arborescence des séries\n"+
                    "  -print formats      Affiche l'arborescence des formats\n"+
                    "  -print categories   Affiche l'arborescence des catégories\n"+
                    "  -print decoders     Affiche l'arborescence des catégories à partir des formats\n"+
                    "  -browse             Affiche le contenu de toute la base de données (interface graphique)\n"+
                    "  -verify             Vérifie l'existence d'un fichier pour chaque image de chaque couche\n"+
                    "  -config             Configure la base de données (interface graphique)\n"+
                    "  -locale <name>      Langue et conventions d'affichage (exemple: \"fr_CA\")\n"+
                    "  -encoding <name>    Page de code pour les sorties     (exemple: \"cp850\")\n"+
                    "  -Xout <filename>    Fichier de destination (le périphérique standard par défaut)");
    }

    /**
     * Affiche l'arborescence des couches  qui se trouvent dans la base
     * de données. Cette méthode sert à vérifier le contenu de la base
     * de données  ainsi que le bon fonctionnement de l'implémentation
     * de {@link LayerTable}.
     */
    private void layers(final TreeDepth depth) throws CatalogException, SQLException, IOException {
        boolean createSeries = false;
        assert (createSeries = true); // Intentional side-effect
        final LayerTree layers = getDatabase().getTable(LayerTree.class);
        final TreeModel model  = layers.getTree(depth, createSeries);
        out.println();
        out.println(Trees.toString(model));
        out.flush();
    }

    /**
     * Affiche la liste de tous les formats trouvés dans la base de données.
     * Cette méthode sert à vérifier le contenu de la base de données, ainsi
     * que le bon fonctionnement des classes d'interrogation.
     */
    private void decoders() throws CatalogException, SQLException, IOException {
        final FormatTable formatTable = getDatabase().getTable(FormatTable.class);
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode(
              Resources.getResources(locale).getString(ResourceKeys.FORMATS));
        for (final Format entry : formatTable.getEntries()) {
            root.add(entry.getTree(locale));
        }
        out.println();
        out.println(Trees.toString(new DefaultTreeModel(root)));
        out.flush();
    }

    /**
     * Affiche dans une fenêtre <cite>Swing</cite> le contenu de toute la base de données.
     */
    private void browse() throws CatalogException, SQLException, IOException {
        final Database          database = getDatabase();
        final LayerTree        treeTable = database.getTable(LayerTree.class);
        final TreeModel        treeModel = treeTable.getTree(TreeDepth.CATEGORY, true);
        final JComponent        treePane = new JScrollPane(new JTree(treeModel));
        final JTabbedPane     tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
        final JSplitPane       splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, treePane, tabbedPane);
        final TableCellRenderer renderer = new CoverageTableModel.CellRenderer();
        final LayerTable      layerTable = database.getTable(LayerTable.class);
        for (final Layer layer : layerTable.getEntries()) {
            final TableModel model = new CoverageTableModel(layer);
            if (model.getRowCount() != 0) {
                final JTable table = new JTable(model);
                table.setDefaultRenderer(String.class, renderer);
                table.setDefaultRenderer(  Date.class, renderer);
                tabbedPane.addTab(layer.getName(), new JScrollPane(table));
            }
        }
        final JFrame frame = new JFrame(Resources.format(ResourceKeys.DATABASE));
        frame.setContentPane(splitPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Vérifie la validité de chacune des images déclarées dans la base de données.
     */
    private void verify() throws CatalogException, SQLException, IOException {
        final Database database = getDatabase();
        final GridCoverageTable coverages = database.getTable(GridCoverageTable.class);
        for (final Layer layer : database.getTable(LayerTable.class).getEntries()) {
            if (layer.getModel() != null) {
                /*
                 * Si les images sont obtenus à partir de modèles, ne vérifie pas ces images.
                 * Elles peuvent être manquantes puisque elle sont parfois à recalculer.
                 */
                continue;
            }
            if (layer.getName().startsWith("Potentiel de pêche")) {
                // TODO: bricolage temporaire.
                continue;
            }
            coverages.setLayer(layer);
            out.println(layer);
            for (final CoverageReference coverage : coverages.getEntries()) {
                final File file = coverage.getFile();
                if (file != null) {
                    String message;
                    if (file.isFile()) try {
                        if (coverage.getSeries().getFormat().getName().startsWith("HDF")) {
                            // TODO: Lecture de fichier HDF désactivée pour l'instant.
                            continue;
                        }
                        final GridCoverage2D c = coverage.getCoverage(null);
                        if (c.geophysics(true) != c.geophysics(false)) {
                            continue;
                        }
                        message = "Pas de vue géophysique.";
                    } catch (Exception e) {
                        message = Classes.getShortClassName(e);
                        final String m = e.getLocalizedMessage();
                        if (m != null) {
                            message = message + ": " + m;
                        }
                    } else {
                        message = "Fichier inexistant.";
                    }
                    out.print("  ");
                    out.println(coverage);
                    out.print("    ");
                    out.println(message);
                }
            }
        }
    }

    /**
     * Affiche le modèle linéaire spécifié.
     *
     * @throws CatalogException Si la base de données contient des enregistrements invalides.
     * @throws SQLException si une requête SQL a échoué.
     * @throws IOException si la lecture du fichier de configuration ou le formattage du modèle
     *         a échoué.
     */
    private void model(final String layer) throws CatalogException, SQLException, IOException {
        final Model model = getDatabase().getTable(LayerTable.class).getEntry(layer).getModel();
        if (model == null) {
            out.print("Aucun modèle n'est défini.");
        } else if (model instanceof LinearModel) {
            ((LinearModel) model).print(out, null);
        } else {
            out.print("Le modèle n'est pas linéaire.");
        }
    }

    /**
     * Exécute les instructions qui ont été passées au constructeur.
     *
     * @throws CatalogException Si la base de données contient des enregistrements invalides.
     * @throws SQLException si une requête SQL a échoué.
     * @throws IOException si la lecture du fichier de configuration a échoué.
     *
     * @todo L'option {@code -config} est temporairement désactivée.
     */
    protected void run() throws CatalogException, SQLException, IOException {
        final boolean config = getFlag("-config");
        final boolean browse = getFlag("-browse");
        final boolean verify = getFlag("-verify");
        final String   print = getOptionalString("-print");
        final String   model = getOptionalString("-model");
        getRemainingArguments(0);
        if (config) {
            out.println("Option non-diponible pour l'instant.");
//TODO      getDatabase().getSQLEditor().showDialog(null);
        }
        if (print != null) {
            TreeDepth depth = null;
            if (print.equalsIgnoreCase("decoders")) {
                decoders();
            }
            else if (print.equalsIgnoreCase("categories")) depth = TreeDepth.CATEGORY;
            else if (print.equalsIgnoreCase("formats"   )) depth = TreeDepth.FORMAT;
            else if (print.equalsIgnoreCase("series"    )) depth = TreeDepth.SERIES;
            else if (print.equalsIgnoreCase("layers"    )) depth = TreeDepth.LAYER;
            else if (print.equalsIgnoreCase("thematics" )) depth = TreeDepth.THEMATIC;
            else {
                out.print("Option inconnue: ");
                out.println(print);
            }
            if (depth != null) {
                layers(depth);
            }
        }
        if (model != null) {
            model(model);
        }
        if (browse) browse();
        if (verify) verify();
        if (database == null) {
            help();
        } else {
            database.close();
            database = null;
        }
        out.flush();
    }

    /**
     * Affiche l'arborescence des couches qui se trouvent dans la base
     * de données. Cette méthode sert à vérifier le contenu de la base
     * de données ainsi que le bon fonctionnement du paquet.
     */
    public static void main(String[] args) {
        Logging.ALL.forceMonolineConsoleOutput();
        final Console console = new Console(args);
        try {
            console.run();
        } catch (Exception e) {
            e.printStackTrace(console.err);
        }
    }
}
