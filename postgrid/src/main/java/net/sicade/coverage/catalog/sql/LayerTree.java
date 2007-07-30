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
package net.sicade.coverage.catalog.sql;

// J2SE dependencies
import java.util.Locale;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.DefaultTreeModel;

// Geotools dependencies
import org.geotools.gui.swing.tree.NamedTreeNode;
import org.geotools.gui.swing.tree.DefaultMutableTreeNode;

// Seagis dependencies
import net.sicade.catalog.ConfigurationKey;
import net.sicade.catalog.CatalogException;
import net.sicade.catalog.Element;
import net.sicade.observation.Procedure;          // Pour javadoc
import net.sicade.catalog.Table;
import net.sicade.catalog.Database;
import net.sicade.catalog.SingletonTable;
import net.sicade.observation.sql.ProcedureTable;
import net.sicade.coverage.catalog.Format;
import net.sicade.coverage.catalog.Thematic;  // Pour javadoc
import net.sicade.resources.i18n.Resources;
import net.sicade.resources.i18n.ResourceKeys;


/**
 * Construction d'une arborescence des {@linkplain Layer couches}. Cette classe possède une méthode
 * {@link #getTree} capable de retrouver les {@linkplain Thematic thématiques} et les {@linkplain
 * Procedure procédures} qui constituent les couches, et de placer ces informations dans une arborescence.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@Deprecated
public class LayerTree extends Table {
    /**
     * Requête SQL utilisée pour obtenir l'arborescence des couches. L'ordre des colonnes est
     * essentiel. Ces colonnes sont référencées par les constantes {@link #LAYER_NAME},
     * {@link #SERIES_NAME} et compagnie.
     */
    private static final ConfigurationKey SELECT = null; // new ConfigurationKey("Layer:TREE",
//            "SELECT t.name,"                                     +
//                  " p.name,"                                     +
//                  " l.name,"                                     +
//                  " s.identifier,"                               +
//                    " format\n"                                  +
//            "  FROM \"Series\"     AS s\n"                       +
//            "  JOIN \"Layers\"     AS l ON l.name=layers\n"      +
//            "  JOIN \"Procedures\" AS p ON p.name=procedure\n"   +
//            "  JOIN \"Thematics\"  AS t ON t.name=phenomenon\n"  +
//            " WHERE visible=TRUE\n"                              +
//            " ORDER BY t.name,"                                  +
//                     " p.name,"                                  +
//                     " l.name,"                                  +
//                     " s.identifier");


    /** Numéro de colonne.  */ static final int THEMATIC  =  1;
    /** Numéro de colonne.  */ static final int PROCEDURE =  2;
    /** Numéro de colonne.  */ static final int LAYER     =  3;
    /** Numéro de colonne.  */ static final int SERIES    =  4;
    /** Numéro de colonne.  */ static final int FORMAT    =  5;

    /**
     * Les types de table pour chacune des colonnes identifiées par les constantes
     * {@link #THEMATIC}, {@link #PROCEDURE}, etc.
     */
    @SuppressWarnings("unchecked")
    private static final Class<? extends SingletonTable>[] TYPES = new Class[FORMAT + 1];
    static {
        TYPES[THEMATIC ] =  ThematicTable.class;
        TYPES[PROCEDURE] = ProcedureTable.class;
        TYPES[LAYER    ] =     LayerTable.class;
        TYPES[SERIES   ] =    SeriesTable.class;
        TYPES[FORMAT   ] =    FormatTable.class;
    }

    /**
     * Les tables qui ont déjà été créées.
     */
    private final SingletonTable[] tables = new SingletonTable[TYPES.length];

    /**
     * Construit une table qui interrogera la base de données spécifiée.
     *
     * @param database  Connexion vers la base de données d'observations.
     */
    public LayerTree(final Database database) {
        super(new net.sicade.catalog.Query(database)); // TODO
    }

    /**
     * Retourne une arborescence qui pourra être affichée dans une composante {@link javax.swing.JTree}.
     * Cette méthode peut construire les {@linkplain Element éléments} correspondant à chaque noeud,
     * (ce qui a un certain coût), ou simplement stocker les noms de chaque noeuds. Ce comportement
     * est contrôlé par le paramètre {@code createEntries}. S'il a la valeur {@code true}, alors
     * l'{@linkplain org.geotools.gui.swing.tree.TreeNode#getUserObject objet utilisateur} de chaque
     * noeud sera un {@linkplain Element élément} complètement formé.
     *
     * @param  depth La profondeur de l'arborescence.
     * @param  createEntries Indique s'il faut contruire les {@linkplain Element éléments}
     *         pour chaque noeud.
     * @return Arborescence des couches de la base de données.
     * @throws CatalogException si un enregitrement est invalide.
     * @throws SQLException si l'interrogation du catalogue a échoué pour une autre raison.
     */
    public synchronized TreeModel getTree(final TreeDepth depth, final boolean createEntries)
            throws CatalogException, SQLException
    {
        final Database database = getDatabase();
        final Locale     locale = database.getLocale();
        final ResultSet  result = getStatement(getProperty(SELECT)).executeQuery();
        final int   branchCount = Math.min(FORMAT, depth.rank);
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode(
                Resources.getResources(locale).getString(ResourceKeys.SERIES));
        /*
         * Balaye la liste de tous les groupes, et place ces groupes
         * dans une arborescence au fur et à mesure qu'ils sont trouvés.
         */
        while (result.next()) {
            DefaultMutableTreeNode branch = root;
      scan: for (int i=1; i<=branchCount; i++) {
                /*
                 * Vérifie s'il existe déjà une branche pour la thématique, procédure où la
                 * couche de l'enregistrement courant. Si une de ces branches n'existe pas,
                 * elle sera créée au passage.
                 */
                final String name = result.getString(i).trim();
                for (int j=branch.getChildCount(); --j>=0;) {
                    final TreeNode node = branch.getChildAt(j);
                    if (name.equals(node.toString())) {
                        branch = (DefaultMutableTreeNode) node;
                        continue scan;
                    }
                }
                /*
                 * Construit l'entrée, si elle a été demandée. Sinon, on ne retiendra que le nom.
                 */
                final boolean continueAfter = (i == branchCount) && (depth.rank > branchCount);
                final Object entry;
                if (createEntries || continueAfter) {
                    SingletonTable table = tables[i];
                    if (table == null) {
                        tables[i] = table = database.getTable(TYPES[i]);
                    }
                    entry = table.getEntry(name);
                    assert name.equals(((Element) entry).getName()) : entry;
                } else {
                    entry = name;
                }
                /*
                 * Construit le noeud. Si les catégories ont été demandées, elles seront
                 * ajoutées après le dernier noeud qui est du ressort de cette table.
                 */
                final DefaultMutableTreeNode node = new NamedTreeNode(name, entry, i!=depth.rank);
                if (continueAfter) {
                    node.add(((Format) entry).getTree(locale));
                }
                branch.add(node);
                branch = node;
            }
        }
        result.close();
        return new DefaultTreeModel(root, true);
    }
}
