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
import java.net.URL;
import java.awt.Color;
import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.io.IOException;
import java.text.ParseException;

// OpenGIS dependencies
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform1D;
import org.opengis.referencing.operation.MathTransformFactory;

// Geotools dependencies
import org.geotools.util.NumberRange;
import org.geotools.coverage.Category;
import org.geotools.referencing.FactoryFinder;

// Sicade dependencies
import net.sicade.observation.ConfigurationKey;
import net.sicade.observation.CatalogException;
import net.sicade.observation.ServerException;
import net.sicade.observation.IllegalRecordException;
import net.sicade.observation.sql.Shareable;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.Table;
import net.sicade.observation.sql.UsedBy;
import net.sicade.observation.sql.Use;
import net.sicade.image.Utilities;


/**
 * Connexion vers une table des {@linkplain Category cat�gories}. Cette table construit des objets
 * {@link Category} pour une bande individuelle. Les categories sont une des composantes d'un objet
 * {@link org.geotools.coverage.grid.GridCoverage2D}, mais ne correspondent pas directement � un
 * {@linkplain net.sicade.observation.Element �l�ment} du paquet des observations.
 * <p>
 * Cette table est utilis�e par {@link SampleDimensionTable}, qui construit des objets de
 * plus haut niveau.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
@UsedBy(SampleDimensionTable.class)
public class CategoryTable extends Table implements Shareable {
    /**
     * Requ�te SQL utilis�e par cette classe pour obtenir la table des cat�gories.
     * L'ordre des colonnes est essentiel. Ces colonnes sont r�f�renc�es par les
     * constantes {@link #NAME}, {@link #UPPER} et compagnie.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("Categories:SELECT",
            "SELECT name, "           +   // [01] NAME
                   "lower, "          +   // [02] LOWER
                   "upper, "          +   // [03] UPPER
                   "c0, "             +   // [04] C0
                   "c1, "             +   // [05] C1
                   "log, "            +   // [06] LOG
                   "colors\n"         +   // [07] COLORS
            "  FROM \"Categories\"\n" +
            " WHERE band=? ORDER BY lower");

    /** Num�ro d'argument. */ private static final int ARGUMENT_BAND = 1;
    /** Num�ro de colonne. */ private static final int NAME          = 1;
    /** Num�ro de colonne. */ private static final int LOWER         = 2;
    /** Num�ro de colonne. */ private static final int UPPER         = 3;
    /** Num�ro de colonne. */ private static final int C0            = 4;
    /** Num�ro de colonne. */ private static final int C1            = 5;
    /** Num�ro de colonne. */ private static final int LOG           = 6;
    /** Num�ro de colonne. */ private static final int COLORS        = 7;

    /**
     * Transformation <code>f(x) = 10<sup>x</sup></code>. Utilis�e pour le d�codage des images de
     * concentrations en chlorophylle-a. Ne sera construite que la premi�re fois o� elle sera
     * n�cessaire.
     */
    private transient MathTransform1D exponential;

    /**
     * Construit une table en utilisant la connexion sp�cifi�e.
     *
     * @param database  Connexion vers la base de donn�es d'observations.
     */
    public CategoryTable(final Database database) {
        super(database);
    }

    /**
     * Retourne la liste des cat�gories qui appartiennent � la bande sp�cifi�e.
     *
     * @param  band Identificateur de la bande pour lequel on veut les cat�gories.
     * @return Les cat�gories de la bande demand�e.
     * @throws IllegalRecordException si une incoh�rence a �t� trouv�e dans les enregistrements.
     * @throws SQLException si l'interrogation de la table a �chou� pour une autre raison.
     */
    public synchronized Category[] getCategories(final String band) throws CatalogException, SQLException {
        final PreparedStatement statement = getStatement(SELECT);
        statement.setString(ARGUMENT_BAND, band);
        final List<Category> categories = new ArrayList<Category>();
        final ResultSet result = statement.executeQuery();
        while (result.next()) {
            boolean isQuantifiable = true;
            final String    name = result.getString (NAME);
            final int      lower = result.getInt    (LOWER);
            final int      upper = result.getInt    (UPPER);
            final double      c0 = result.getDouble (C0); isQuantifiable &= !result.wasNull();
            final double      c1 = result.getDouble (C1); isQuantifiable &= !result.wasNull();
            final boolean    log = result.getBoolean(LOG);
            final String colorID = result.getString (COLORS);
            /*
             * Proc�de maintenant au d�codage du champ "colors". Ce champ contient
             * une cha�ne de caract�re qui indique soit le code RGB d'une couleur
             * uniforme, ou soit l'adresse URL d'une palette de couleurs.
             */
            Color[] colors = null;
            if (colorID != null) try {
                colors = decode(colorID);
            } catch (IOException exception) {
                throw new IllegalRecordException(result.getMetaData().getTableName(COLORS), exception);
            } catch (ParseException exception) {
                throw new IllegalRecordException(result.getMetaData().getTableName(COLORS), exception);
            }
            /*
             * Construit une cat�gorie correspondant � l'enregistrement qui vient d'�tre lu.
             * Une cat�gorie peut �tre qualitative (premier cas), quantitative mais lin�aire
             * (deuxi�me cas), ou quantitative et logarithmique (troisi�me cas).
             */
            Category category;
            final NumberRange range = new NumberRange(lower, upper);
            if (!isQuantifiable) {
                // Cat�gorie qualitative.
                category = new Category(name, colors, range, (MathTransform1D)null);
            } else {
                // Cat�gorie quantitative
                category = new Category(name, colors, range, c1, c0);
                if (log) try {
                    // Cat�gorie quantitative et logarithmique.
                    final MathTransformFactory factory = FactoryFinder.getMathTransformFactory(FACTORY_HINTS);
                    if (exponential == null) {
                        final ParameterValueGroup param = factory.getDefaultParameters("Exponential");
                        param.parameter("base").setValue(10.0); // Must be a 'double'
                        exponential = (MathTransform1D) factory.createParameterizedTransform(param);
                    }
                    MathTransform1D tr = category.getSampleToGeophysics();
                    tr = (MathTransform1D) factory.createConcatenatedTransform(tr, exponential);
                    category = new Category(name, colors, range, tr);
                } catch (FactoryException exception) {
                    throw new ServerException(exception);
                }
            }
            categories.add(category);
        }
        result.close();
        return categories.toArray(new Category[categories.size()]);
    }

    /**
     * Optient une couleur uniforme ou une palette de couleur. L'argument {@code colors}
     * peut �tre un code RGB d'une seule couleur (par exemple {@code "#D2C8A0"}), ou un
     * lien URL vers une palette de couleurs (par exemple {@code "SST-Nasa.pal"}).
     *
     * @param  colors Identificateur de la ou les couleurs d�sir�es.
     * @return Palette de couleurs demand�e.
     * @throws IOException si les couleurs n'ont pas pu �tre lues.
     * @throws ParseException si le fichier de la palette de couleurs a �t� ouvert,
     *         mais qu'elle contient des caract�res qui n'ont pas pus �tre interpr�t�s.
     */
    private static Color[] decode(String colors) throws IOException, ParseException {
        /*
         * Retire les guillements au d�but et � la fin de la cha�ne, s'il y en a.
         * Cette op�ration vise � �viter des probl�mes de compatibilit�s lorsque
         * l'importation des th�mes dans la base des donn�es s'est senti oblig�e
         * de placer des guillemets partout.
         */
        if (true) {
            colors = colors.trim();
            final int length = colors.length();
            if (length>=2 && colors.charAt(0)=='"' && colors.charAt(length-1)=='"') {
                colors = colors.substring(1, length-1);
            }
        }
        /*
         * V�rifie si la cha�ne de caract�re repr�sente un code de couleurs
         * unique, comme par exemple "#D2C8A0". Si oui, ce code sera retourn�
         * dans un tableau de longueur 1.
         */
        try {
            return new Color[] {Color.decode(colors)};
        } catch (NumberFormatException exception) {
            /*
             * Le d�codage de la cha�ne a �chou�. C'est peut-�tre
             * parce qu'il s'agit d'un nom de fichier.  On ignore
             * l'erreur et on continue en essayant de d�coder l'URL.
             */
        }
        final URL url = new URL(colors);
        return Utilities.getPaletteFactory().getColors(url.getPath());
    }
}
