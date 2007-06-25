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
package net.sicade.observation.coverage.sql;

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
import net.sicade.observation.ConfigurationKey;
import net.sicade.observation.CatalogException;
import net.sicade.observation.Element;
import net.sicade.observation.Procedure;          // Pour javadoc
import net.sicade.observation.sql.Table;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.Shareable;
import net.sicade.observation.sql.SingletonTable;
import net.sicade.observation.sql.ProcedureTable;
import net.sicade.observation.coverage.Format;
import net.sicade.observation.coverage.Thematic;  // Pour javadoc
import net.sicade.resources.seagis.Resources;
import net.sicade.resources.seagis.ResourceKeys;


/**
 * Construction d'une arborescence des {@linkplain Series s�ries}. Cette classe poss�de une m�thode
 * {@link #getTree} capable de retrouver les {@linkplain Thematic th�matiques} et les {@linkplain
 * Procedure proc�dures} qui constituent les s�ries, et de placer ces informations dans une arborescence.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class SeriesTree extends Table implements Shareable {
    /**
     * Requ�te SQL utilis�e pour obtenir l'arborescence des s�ries. L'ordre des colonnes est
     * essentiel. Ces colonnes sont r�f�renc�es par les constantes {@link #SERIES_NAME},
     * {@link #SUBSERIES_NAME} et compagnie.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("Series:TREE",
            "SELECT t.name,"                                     +
                  " p.name,"                                     +
                  " s.name,"                                     +
                  " g.identifier,"                               +
                    " format\n"                                  +
            "  FROM \"Series\"     AS g\n"                       +
            "  JOIN \"Layers\"     AS s ON s.name=layers\n"      +
            "  JOIN \"Procedures\" AS p ON p.name=procedure\n"   +
            "  JOIN \"Thematics\"  AS t ON t.name=phenomenon\n"  +
            " WHERE visible=TRUE\n"                              +
            " ORDER BY t.name,"                                  +
                     " p.name,"                                  +
                     " s.name,"                                  +
                     " g.identifier");


    /** Num�ro de colonne.  */ static final int THEMATIC   =  1;
    /** Num�ro de colonne.  */ static final int PROCEDURE  =  2;
    /** Num�ro de colonne.  */ static final int SERIES     =  3;
    /** Num�ro de colonne.  */ static final int SUBSERIES  =  4;
    /** Num�ro de colonne.  */ static final int FORMAT     =  5;

    /**
     * Les types de table pour chacune des colonnes identifi�es par les constantes
     * {@link #THEMATIC}, {@link #PROCEDURE}, etc.
     */
    @SuppressWarnings("unchecked")
    private static final Class<? extends SingletonTable>[] TYPES = new Class[FORMAT + 1];
    static {
        TYPES[THEMATIC ] =  ThematicTable.class;
        TYPES[PROCEDURE] = ProcedureTable.class;
        TYPES[SERIES   ] =    SeriesTable.class;
        TYPES[SUBSERIES] = SubSeriesTable.class;
        TYPES[FORMAT   ] =    FormatTable.class;
    }

    /**
     * Les tables qui ont d�j� �t� cr��es.
     */
    private final SingletonTable[] tables = new SingletonTable[TYPES.length];

    /**
     * Construit une table qui interrogera la base de donn�es sp�cifi�e.
     *
     * @param database  Connexion vers la base de donn�es d'observations.
     */
    public SeriesTree(final Database database) {
        super(database);
    }

    /**
     * Retourne une arborescence qui pourra �tre affich�e dans une composante {@link javax.swing.JTree}.
     * Cette m�thode peut construire les {@linkplain Element �l�ments} correspondant � chaque noeud,
     * (ce qui a un certain co�t), ou simplement stocker les noms de chaque noeuds. Ce comportement
     * est contr�l� par le param�tre {@code createEntries}. S'il a la valeur {@code true}, alors
     * l'{@linkplain org.geotools.gui.swing.tree.TreeNode#getUserObject objet utilisateur} de chaque
     * noeud sera un {@linkplain Element �l�ment} compl�tement form�.
     *
     * @param  depth La profondeur de l'arborescence.
     * @param  createEntries Indique s'il faut contruire les {@linkplain Element �l�ments}
     *         pour chaque noeud.
     * @return Arborescence des s�ries de la base de donn�es.
     * @throws CatalogException si un enregitrement est invalide.
     * @throws SQLException si l'interrogation du catalogue a �chou� pour une autre raison.
     */
    public synchronized TreeModel getTree(final TreeDepth depth, final boolean createEntries)
            throws CatalogException, SQLException
    {
        final Locale    locale = database.getLocale();
        final ResultSet result = getStatement(SELECT).executeQuery();
        final int  branchCount = Math.min(FORMAT, depth.rank);
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode(
                Resources.getResources(locale).getString(ResourceKeys.SERIES));
        /*
         * Balaye la liste de tous les groupes, et place ces groupes
         * dans une arborescence au fur et � mesure qu'ils sont trouv�s.
         */
        while (result.next()) {
            DefaultMutableTreeNode branch = root;
      scan: for (int i=1; i<=branchCount; i++) {
                /*
                 * V�rifie s'il existe d�j� une branche pour la th�matique, proc�dure o� la
                 * s�rie de l'enregistrement courant. Si une de ces branches n'existe pas,
                 * elle sera cr��e au passage.
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
                 * Construit l'entr�e, si elle a �t� demand�e. Sinon, on ne retiendra que le nom.
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
                 * Construit le noeud. Si les cat�gories ont �t� demand�es, elles seront
                 * ajout�es apr�s le dernier noeud qui est du ressort de cette table.
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
